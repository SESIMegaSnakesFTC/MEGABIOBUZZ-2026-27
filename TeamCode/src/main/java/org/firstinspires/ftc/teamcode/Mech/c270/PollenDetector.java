package org.firstinspires.ftc.teamcode.Mech.c270;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processador de visão (OpenCV) que detecta PÓLENS amarelos com a webcam Logitech C270.
 *
 * Pipeline por frame:
 *   1. RGB -> HSV e máscara de amarelo (Core.inRange)
 *   2. Morfologia (abrir/fechar) para limpar ruído
 *   3. Contornos -> mancha redonda vira 1 Pollen; mancha não redonda é
 *      separada em bolas (distanceTransform) e só vale se todas forem redondas
 *   4. Agrupa pólens próximos em clusters (Union-Find)
 *   5. Elege o maior cluster (empate: o que tem o maior pólen = mais perto)
 *   6. Calcula o Tx (desvio horizontal) do cluster dominante
 *
 * Como usar: adicionar ao VisionPortal com .addProcessor(detector) e ler
 * detector.getResult() no loop do OpMode. Ver TestPollenTracking.
 *
 * THREADS: processFrame() roda na thread da câmera; o OpMode roda em outra.
 * Por isso todo resultado é publicado como UM objeto imutável (Result) numa
 * variável volatile -> o OpMode sempre lê um frame inteiro e consistente,
 * nunca metade de um frame e metade de outro.
 */
public class PollenDetector implements VisionProcessor {

    // =====================================================================
    // PARÂMETROS AJUSTÁVEIS
    // São "public static" para poderem ser alterados em tempo real (pelo
    // TestPollenTracking ou por um painel de tuning) sem recompilar.
    // Atenção: static sobrevive entre execuções de OpModes, só volta ao
    // padrão quando o app do Robot Controller reinicia. Depois de achar bons
    // valores no teste, copie-os para cá.
    // =====================================================================

    // Faixa HSV do amarelo. No OpenCV: H vai de 0 a 179 (metade dos 360°),
    // S e V vão de 0 a 255. Amarelo puro fica em H ≈ 30.
    // S mínimo alto descarta brancos/reflexos; V mínimo descarta sombras.
    // Valores medidos no campo (S 195 descarta a madeira clara)
    public static Scalar lowerYellow = new Scalar(15, 195, 100);
    public static Scalar upperYellow = new Scalar(35, 255, 255);

    // Área (em pixels²) aceita para um contorno ser considerado um pólen.
    // Abaixo de MIN_AREA = ruído; acima de MAX_AREA = algo grande demais.
    // MAX_AREA vale POR PÓLEN, depois de separar bolas coladas.
    public static double MIN_AREA = 150;
    public static double MAX_AREA = 15000;

    // Redondeza = área / área do círculo mínimo que envolve a mancha.
    // Círculo ≈ 1.0, quadrado ≈ 0.64, retângulo comprido bem menos.
    public static double MIN_FILL = 0.7;

    // Separação de bolas coladas: mantém só o "miolo" da mancha (pixels com
    // distância até a borda >= SPLIT_RATIO × maior distância). Cada miolo = 1 bola.
    // Subir se bolas muito encostadas não separam; descer se 1 bola vira 2.
    public static double SPLIT_RATIO = 0.6;

    // Distância máxima (pixels) entre os CENTROS de dois pólens para eles
    // serem do mesmo grupo. É encadeada: se A está perto de B e B perto de C,
    // A, B e C ficam juntos mesmo que A esteja longe de C.
    // shortcut: valor fixo em pixels, mas pólens longe da câmera ficam menores
    // e mais juntos na imagem; se agrupar errado em distâncias diferentes,
    // trocar por um limite proporcional ao tamanho médio dos pólens.
    public static double CLUSTER_DIST_PX = 80;

    // FOV horizontal (graus) usado SÓ se não houver calibração da câmera.
    // O plano original sugeria 60°, mas a calibração embutida no SDK para a
    // C270 em 640x480 tem fx = 822.3 px -> FOV = 2*atan(320/822.3) ≈ 42.5°.
    // (60° é o FOV diagonal de marketing de outras webcams; usar 60 aqui
    // deixaria o Tx ~40% maior que o real nas bordas.)
    public static double FOV_H_DEG = 42.5;

    // =====================================================================
    // TIPOS DE DADOS
    // =====================================================================

    /** Um pólen individual encontrado no frame (coordenadas em pixels). */
    public static class Pollen {
        public final double x, y;   // centróide
        public final double area;   // área do contorno em pixels²
        public final Rect rect;     // caixa delimitadora (para desenhar)

        Pollen(double x, double y, double area, Rect rect) {
            this.x = x;
            this.y = y;
            this.area = area;
            this.rect = rect;
        }
    }

    /** Um grupo (cluster) de pólens próximos. Imutável depois de criado. */
    public static class PollenGroup {
        public final List<Pollen> pollens;
        public final double centerX, centerY; // média dos centróides
        public final Rect bounds;             // caixa que envolve o grupo todo
        public final double maxArea;          // área do maior pólen (maior = mais perto)

        PollenGroup(List<Pollen> pollens) {
            this.pollens = Collections.unmodifiableList(pollens);

            double sumX = 0, sumY = 0, biggest = 0;
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Pollen p : pollens) {
                sumX += p.x;
                sumY += p.y;
                biggest = Math.max(biggest, p.area);
                minX = Math.min(minX, p.rect.x);
                minY = Math.min(minY, p.rect.y);
                maxX = Math.max(maxX, p.rect.x + p.rect.width);
                maxY = Math.max(maxY, p.rect.y + p.rect.height);
            }
            // Centro médio: (1/N)·Σx e (1/N)·Σy
            this.centerX = sumX / pollens.size();
            this.centerY = sumY / pollens.size();
            this.bounds = new Rect(minX, minY, maxX - minX, maxY - minY);
            this.maxArea = biggest;
        }

        public int size() {
            return pollens.size();
        }
    }

    /** Foto completa do resultado de UM frame. Imutável -> seguro entre threads. */
    public static class Result {
        public final List<Pollen> pollens;    // todos os pólens válidos
        public final List<PollenGroup> groups; // todos os grupos
        public final PollenGroup dominant;    // maior grupo (null se nenhum)
        public final double txDeg;            // desvio em graus (+ = direita)
        public final double txNorm;           // desvio normalizado [-1, 1]
        public final long timeNanos;          // System.nanoTime() do processamento

        static final Result EMPTY = new Result(
                new ArrayList<Pollen>(), new ArrayList<PollenGroup>(), null, 0, 0, 0);

        Result(List<Pollen> pollens, List<PollenGroup> groups, PollenGroup dominant,
               double txDeg, double txNorm, long timeNanos) {
            this.pollens = Collections.unmodifiableList(pollens);
            this.groups = Collections.unmodifiableList(groups);
            this.dominant = dominant;
            this.txDeg = txDeg;
            this.txNorm = txNorm;
            this.timeNanos = timeNanos;
        }

        public boolean hasTarget() {
            return dominant != null;
        }

        public int largestGroupCount() {
            return dominant == null ? 0 : dominant.size();
        }

        /** Idade deste resultado em ms (grande se a câmera travou ou nunca rodou). */
        public double ageMs() {
            return timeNanos == 0 ? Double.MAX_VALUE : (System.nanoTime() - timeNanos) / 1e6;
        }
    }

    // =====================================================================
    // ESTADO INTERNO
    // =====================================================================

    // Mats reaproveitados a cada frame: alocar Mat novo por frame é lento e
    // gera lixo de memória nativa no Control Hub.
    private final Mat hsv = new Mat();
    private final Mat mask = new Mat();
    private final Mat hierarchy = new Mat();
    // Elemento estruturante 5x5 usado na morfologia (o "pincel" que erode/dilata)
    private final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
    // Usados na separação de bolas coladas (blob = máscara de 1 mancha)
    private final Mat blob = new Mat();
    private final Mat dist = new Mat();
    private final Mat core = new Mat();
    private final Mat noHierarchy = new Mat();

    private int width = 640;

    // Intrínsecos da câmera (lente). fx = distância focal em pixels;
    // cx = centro óptico real (não é exatamente width/2).
    // volatile porque o OpMode lê isUsingCalibration() de outra thread
    private volatile boolean useCalibration = false;
    private double fx = 0, cx = 0;

    // Último resultado publicado. volatile = a escrita de uma thread fica
    // visível imediatamente para a outra.
    private volatile Result latest = Result.EMPTY;

    // Tintas para desenhar no stream (criadas uma vez só)
    private final Paint paintPollen = stroke(Color.YELLOW, 3);
    private final Paint paintGroup = stroke(Color.CYAN, 2);
    private final Paint paintDominant = stroke(Color.GREEN, 5);
    private final Paint paintCenter = stroke(Color.RED, 2);
    private final Paint paintText = new Paint();

    public PollenDetector() {
        paintText.setColor(Color.WHITE);
        paintText.setAntiAlias(true);
    }

    private static Paint stroke(int color, float width) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(width);
        return p;
    }

    // =====================================================================
    // VisionProcessor
    // =====================================================================

    /** Chamado uma vez pelo VisionPortal quando a câmera abre. */
    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        this.width = width;

        // Mesma checagem que o AprilTagProcessor do SDK faz: a calibração pode
        // vir null ou "degenerada" (toda zero) quando não existe para a câmera.
        // A C270 TEM calibração embutida no SDK para 640x480.
        useCalibration = calibration != null && !calibration.isDegenerate();
        if (useCalibration) {
            fx = calibration.focalLengthX;
            cx = calibration.principalPointX;
        }
    }

    /** Chamado a cada frame, na thread da câmera. */
    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        // 1. Cor -> máscara.
        // O VisionPortal entrega frames em RGB (não BGR como o OpenCV puro),
        // por isso COLOR_RGB2HSV. Usar BGR2HSV trocaria vermelho com azul e o
        // amarelo viraria ciano -> nada seria detectado.
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_RGB2HSV);
        // Pixel dentro da faixa -> 255 (branco), fora -> 0 (preto)
        Core.inRange(hsv, lowerYellow, upperYellow, mask);

        // 2. Morfologia.
        // OPEN (erode depois dilata): apaga pontinhos isolados (ruído/reflexo).
        // CLOSE (dilata depois erode): tampa buracos dentro do pólen (brilho).
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(mask, mask, Imgproc.MORPH_CLOSE, kernel);

        // 3. Contornos -> pólens.
        // RETR_EXTERNAL: só o contorno de fora de cada mancha (ignora buracos).
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Pollen> pollens = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area >= MIN_AREA) {
                if (fill(contour, area) >= MIN_FILL) {
                    // Redonda -> 1 pólen
                    if (area <= MAX_AREA) {
                        Moments m = Imgproc.moments(contour);
                        if (m.m00 > 0) {
                            // Centróide = (m10/m00, m01/m00) = "centro de massa" da mancha
                            pollens.add(new Pollen(m.m10 / m.m00, m.m01 / m.m00, area, Imgproc.boundingRect(contour)));
                        }
                    }
                } else {
                    // Não redonda -> pode ser várias bolas coladas
                    splitBlob(contour, area, pollens);
                }
            }
            contour.release(); // memória nativa do OpenCV não é liberada pelo GC
        }

        // 4. Agrupamento.
        List<PollenGroup> groups = cluster(pollens, CLUSTER_DIST_PX);

        // 5. Maior grupo; empate -> o que tem o maior pólen (mais perto do robô).
        double imageCenterX = width / 2.0;
        PollenGroup dominant = null;
        for (PollenGroup g : groups) {
            if (dominant == null
                    || g.size() > dominant.size()
                    || (g.size() == dominant.size() && g.maxArea > dominant.maxArea)) {
                dominant = g;
            }
        }

        // 6. Tx do grupo dominante.
        double txDeg = 0, txNorm = 0;
        if (dominant != null) {
            // Normalizado: -1 = borda esquerda, 0 = centro, +1 = borda direita
            txNorm = (dominant.centerX - imageCenterX) / imageCenterX;

            // Graus pelo modelo "pinhole" da câmera: tan(ângulo) = deslocamento / focal.
            // Mais correto que txNorm × FOV/2 (que é linear e erra no meio da imagem).
            // Sem calibração, deriva a focal do FOV: focal = (width/2) / tan(FOV/2).
            double focal = useCalibration ? fx : imageCenterX / Math.tan(Math.toRadians(FOV_H_DEG / 2.0));
            double center = useCalibration ? cx : imageCenterX;
            txDeg = Math.toDegrees(Math.atan((dominant.centerX - center) / focal));
        }

        // Publica tudo de uma vez (uma única escrita atômica da referência)
        Result result = new Result(pollens, groups, dominant, txDeg, txNorm, System.nanoTime());
        latest = result;

        // O que retornamos aqui chega como "userContext" no onDrawFrame
        return result;
    }

    /** Redondeza: área / área do círculo mínimo que envolve o contorno (0 a 1). */
    private static double fill(MatOfPoint contour, double area) {
        MatOfPoint2f pts = new MatOfPoint2f(contour.toArray());
        float[] r = new float[1];
        Imgproc.minEnclosingCircle(pts, new Point(), r);
        pts.release();
        return r[0] > 0 ? area / (Math.PI * r[0] * r[0]) : 0;
    }

    /**
     * Tenta separar uma mancha não redonda em bolas.
     * distanceTransform: cada pixel recebe a distância até a borda; o centro
     * de cada bola vira um pico. Cortando em SPLIT_RATIO do pico, bolas coladas
     * viram "miolos" separados. Aceita só se houver 2+ miolos e TODOS redondos
     * (madeira/retângulo gera miolo comprido -> descartada).
     */
    private void splitBlob(MatOfPoint contour, double area, List<Pollen> out) {
        Rect box = Imgproc.boundingRect(contour);

        // Máscara só desta mancha, recortada na caixa dela
        blob.create(box.height, box.width, CvType.CV_8UC1);
        blob.setTo(new Scalar(0));
        Imgproc.drawContours(blob, Collections.singletonList(contour), 0, new Scalar(255),
                -1, Imgproc.LINE_8, noHierarchy, 0, new Point(-box.x, -box.y));

        Imgproc.distanceTransform(blob, dist, Imgproc.DIST_L2, 3);
        double peak = Core.minMaxLoc(dist).maxVal;
        Imgproc.threshold(dist, dist, peak * SPLIT_RATIO, 255, Imgproc.THRESH_BINARY);
        dist.convertTo(core, CvType.CV_8U);

        List<MatOfPoint> cores = new ArrayList<>();
        Imgproc.findContours(core, cores, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Área da mancha dividida entre as bolas (tamanho usado no desempate)
        double each = cores.isEmpty() ? 0 : area / cores.size();
        List<Pollen> found = new ArrayList<>();
        boolean ok = cores.size() >= 2 && each <= MAX_AREA;
        for (MatOfPoint c : cores) {
            if (ok) {
                Moments m = Imgproc.moments(c);
                if (m.m00 > 0 && fill(c, m.m00) >= MIN_FILL) {
                    Rect r = Imgproc.boundingRect(c);
                    r.x += box.x;
                    r.y += box.y;
                    found.add(new Pollen(m.m10 / m.m00 + box.x, m.m01 / m.m00 + box.y, each, r));
                } else {
                    ok = false; // um miolo não redondo invalida a mancha toda
                }
            }
            c.release();
        }
        if (ok) out.addAll(found);
    }

    /**
     * Agrupa pólens por proximidade usando Union-Find (conjuntos disjuntos).
     *
     * Ideia: cada pólen começa sendo seu próprio grupo. Para cada par (i, j)
     * com distância <= maxDist, unimos os grupos de i e j. No final, pólens
     * com a mesma "raiz" estão no mesmo grupo. Com poucos pólens na tela,
     * testar todos os pares (O(n²)) é barato.
     */
    static List<PollenGroup> cluster(List<Pollen> pollens, double maxDist) {
        int n = pollens.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        // Compara distância² com maxDist² -> evita sqrt (mais rápido, mesmo resultado)
        double maxDistSq = maxDist * maxDist;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dx = pollens.get(i).x - pollens.get(j).x;
                double dy = pollens.get(i).y - pollens.get(j).y;
                if (dx * dx + dy * dy <= maxDistSq) {
                    parent[find(parent, i)] = find(parent, j); // une os dois grupos
                }
            }
        }

        // Junta os pólens por raiz
        Map<Integer, List<Pollen>> byRoot = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = find(parent, i);
            List<Pollen> list = byRoot.get(root);
            if (list == null) {
                list = new ArrayList<>();
                byRoot.put(root, list);
            }
            list.add(pollens.get(i));
        }

        List<PollenGroup> groups = new ArrayList<>();
        for (List<Pollen> list : byRoot.values()) groups.add(new PollenGroup(list));
        return groups;
    }

    /** Sobe pela árvore até a raiz do grupo, encurtando o caminho no trajeto. */
    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]]; // "path halving": deixa as próximas buscas mais rápidas
            i = parent[i];
        }
        return i;
    }

    /**
     * Desenha as anotações por cima do stream (thread de UI).
     * Coordenadas do frame são multiplicadas por scaleBmpPxToCanvasPx para
     * casar com o tamanho da tela onde o stream é mostrado.
     */
    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight,
                            float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        if (!(userContext instanceof Result)) return;
        Result r = (Result) userContext;
        float s = scaleBmpPxToCanvasPx;
        paintText.setTextSize(20 * scaleCanvasDensity);

        // Linha vermelha = centro da imagem (Tx = 0 quando o grupo está nela)
        float midX = (width / 2f) * s;
        canvas.drawLine(midX, 0, midX, onscreenHeight, paintCenter);

        // Amarelo = cada pólen
        for (Pollen p : r.pollens) drawRect(canvas, p.rect, s, paintPollen);

        // Ciano = outros grupos; verde = grupo dominante
        for (PollenGroup g : r.groups) {
            drawRect(canvas, g.bounds, s, g == r.dominant ? paintDominant : paintGroup);
        }

        if (r.dominant != null) {
            canvas.drawCircle((float) r.dominant.centerX * s, (float) r.dominant.centerY * s, 8 * scaleCanvasDensity, paintDominant);
            canvas.drawText(String.format("%d polens | Tx %.1f°", r.dominant.size(), r.txDeg),
                    r.dominant.bounds.x * s, Math.max(paintText.getTextSize(), r.dominant.bounds.y * s - 8), paintText);
        }

        canvas.drawText(String.format("Total: %d | Grupos: %d", r.pollens.size(), r.groups.size()),
                10, paintText.getTextSize() + 10, paintText);
    }

    private static void drawRect(Canvas canvas, Rect rect, float s, Paint paint) {
        canvas.drawRect(rect.x * s, rect.y * s, (rect.x + rect.width) * s, (rect.y + rect.height) * s, paint);
    }

    // =====================================================================
    // API PÚBLICA (thread-safe)
    // Dica: no loop, chame getResult() UMA vez e use os campos dele. Chamar
    // vários getters separados pode misturar dois frames diferentes.
    // =====================================================================

    /** Resultado completo do último frame processado. */
    public Result getResult() {
        return latest;
    }

    /** Total de pólens vistos pela câmera. */
    public int getTotalPollenCount() {
        return latest.pollens.size();
    }

    /** Quantidade de pólens no maior grupo. */
    public int getLargestGroupCount() {
        return latest.largestGroupCount();
    }

    /** Quantidade de grupos formados. */
    public int getGroupCount() {
        return latest.groups.size();
    }

    /** Desvio horizontal do maior grupo em graus. Negativo = esquerda, positivo = direita. */
    public double getTx() {
        return latest.txDeg;
    }

    /** Desvio horizontal normalizado do maior grupo, de -1.0 (esquerda) a +1.0 (direita). */
    public double getTxNormalized() {
        return latest.txNorm;
    }

    /** Se existe ao menos um grupo detectado no último frame. */
    public boolean hasTarget() {
        return latest.hasTarget();
    }

    /** true = Tx calculado com a calibração da lente; false = usando FOV_H_DEG. */
    public boolean isUsingCalibration() {
        return useCalibration;
    }
}
