package org.firstinspires.ftc.teamcode.Autonomo.pedro;

import static com.pedropathing.api.Paths.line;
import static com.pedropathing.api.Paths.path;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Leva robô da pose atual até um dos 8 locais fixos, no TeleOp.
 * Botão apertado -> menor rota pelos cantos sem cruzar o meio, heading travado no do clique.
 * Quem chama decide o override (stick).
 */
public class AutoPath {

    private final Follower follower;
    private static final PoseFactory p = PoseFactory.degrees();

    // 8 locais. null = sem coordenada ainda, botão ignorado
    private final Pose[] locais = {
            HiveTracking.StartPose.RED_PARK.toPose(),  // L1
            HiveTracking.StartPose.BLUE_PARK.toPose(), // L2
            HiveTracking.StartPose.RED_MIDDLE.toPose(), // L3
            HiveTracking.StartPose.BLUE_MIDDLE.toPose(), // L4
            null, // L5
            null, // L6
            null, // L7
            null  // L8
    };

    // cantos da arena (contorna pela borda, evita o meio)
    private static final Pose[] CORNERS = {
            p.of(20.5, 21, 0),    // inferior esquerdo
            p.of(117.5, 21, 0),   // inferior direito
            p.of(24, 117.5, 0),   // superior esquerdo
            p.of(117.5, 118, 0)   // superior direito
    };

    // zona proibida no meio (estruturas + margem do robô)
    // shortcut: retângulo chutado por dentro dos cantos, trocar pelas medidas reais
    static final double X_MIN = 30, X_MAX = 112, Y_MIN = 30, Y_MAX = 112;

    // saídas do meio: robô passa por baixo da hive entre elas
    private static final Pose RED_MID = HiveTracking.StartPose.RED_MIDDLE.toPose();
    private static final Pose BLUE_MID = HiveTracking.StartPose.BLUE_MIDDLE.toPose();
    static final double MARGEM_MID = 5; // polegadas

    // estado anterior de cada botão (borda de subida)
    private final boolean[] antes = new boolean[8];

    public AutoPath(Follower follower) {
        this.follower = follower;
    }

    public void GoToPosition(boolean b1, boolean b2, boolean b3, boolean b4,
                             boolean b5, boolean b6, boolean b7, boolean b8) {
        boolean[] agora = {b1, b2, b3, b4, b5, b6, b7, b8};
        for (int i = 0; i < 8; i++) {
            // só dispara no instante que aperta, não enquanto segura
            if (agora[i] && !antes[i] && locais[i] != null) {

                Pose atual = follower.pose();

                // mid -> outro mid: reta direta por baixo da hive
                boolean cruzaMid = (perto(atual, RED_MID) && perto(locais[i], BLUE_MID))
                        || (perto(atual, BLUE_MID) && perto(locais[i], RED_MID));

                List<Pose> r = cruzaMid ? new ArrayList<>(Arrays.asList(atual, locais[i])) : rota(atual, locais[i]);
                Path[] trechos = new Path[r.size() - 1];

                for (int k = 0; k < trechos.length; k++) trechos[k] = line(r.get(k), r.get(k + 1));

                // tudo num path só: não para nos cantos
                follower.follow(path(trechos).constant(atual));
            }
            antes[i] = agora[i];
        }
    }

    /** Menor rota inicio -> (cantos) -> destino só com retas que não cruzam o meio (Dijkstra). */
    static List<Pose> rota(Pose inicio, Pose destino) {
        // no meio: sai pelo MIDDLE mais próximo, depois segue pelos cantos
        if (dentro(inicio)) {
            Pose mid = dist(inicio, RED_MID) < dist(inicio, BLUE_MID) ? RED_MID : BLUE_MID;
            // destino já é o MIDDLE: evita trecho de comprimento zero
            if (perto(destino, mid)) return new ArrayList<>(Arrays.asList(inicio, destino));
            List<Pose> r = rota(mid, destino);
            r.add(0, inicio);
            return r;
        }
        // nós: 0 = início, 1..4 = cantos, 5 = destino
        Pose[] nos = new Pose[CORNERS.length + 2];
        nos[0] = inicio;
        System.arraycopy(CORNERS, 0, nos, 1, CORNERS.length);
        nos[nos.length - 1] = destino;
        int n = nos.length;

        double[] custo = new double[n];
        int[] veio = new int[n];
        boolean[] feito = new boolean[n];
        Arrays.fill(custo, Double.MAX_VALUE);
        Arrays.fill(veio, -1);
        custo[0] = 0;

        for (int it = 0; it < n; it++) {
            int u = -1;
            for (int k = 0; k < n; k++)
                if (!feito[k] && custo[k] < Double.MAX_VALUE && (u < 0 || custo[k] < custo[u])) u = k;
            if (u < 0) break;
            feito[u] = true;
            for (int v = 0; v < n; v++) {
                if (feito[v] || !livre(nos[u], nos[v])) continue;
                double c = custo[u] + Math.hypot(nos[u].x() - nos[v].x(), nos[u].y() - nos[v].y());
                if (c < custo[v]) { custo[v] = c; veio[v] = u; }
            }
        }

        List<Pose> r = new ArrayList<>();
        // sem rota (não deve acontecer): vai reto
        if (veio[n - 1] < 0) { r.add(inicio); r.add(destino); return r; }
        for (int k = n - 1; k >= 0; k = veio[k]) r.add(0, nos[k]);
        return r;
    }

    // reta não cruza a zona proibida. Ponta dentro da zona libera (precisa entrar/sair)
    static boolean livre(Pose a, Pose b) {
        if (dentro(a) || dentro(b)) return true;
        // Liang-Barsky: recorta a reta no retângulo
        double dx = b.x() - a.x(), dy = b.y() - a.y();
        double[] pp = {-dx, dx, -dy, dy};
        double[] qq = {a.x() - X_MIN, X_MAX - a.x(), a.y() - Y_MIN, Y_MAX - a.y()};
        double t0 = 0, t1 = 1;
        for (int k = 0; k < 4; k++) {
            if (pp[k] == 0) {
                if (qq[k] < 0) return true; // paralela e fora
            } else {
                double t = qq[k] / pp[k];
                if (pp[k] < 0) t0 = Math.max(t0, t);
                else t1 = Math.min(t1, t);
            }
        }
        return t0 > t1;
    }
    public void HoldPosition(Follower follower, Pose currentPos, double Xtarget, double Ytarget, double X, double Y){


        if (Math.abs(Xtarget-X) > 3 && Math.abs(Ytarget-Y) > 3){
            follower.follow(pathzika(currentPos, follower));
        }
        else {
            follower.hold(currentPos);
        }

    }

    private Path pathzika(Pose pose, Follower follower){
        return line(follower.pose(), pose);
    }

    private static double dist(Pose a, Pose b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }

    // a até MARGEM_MID de b, em qualquer direção
    private static boolean perto(Pose a, Pose b) {
        return dist(a, b) <= MARGEM_MID;
    }

    private static boolean dentro(Pose q) {
        return q.x() > X_MIN && q.x() < X_MAX && q.y() > Y_MIN && q.y() < Y_MAX;
    }
}
