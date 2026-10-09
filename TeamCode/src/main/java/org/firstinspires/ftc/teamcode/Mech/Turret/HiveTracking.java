package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.pedropathing.math.Pose;

import java.util.HashMap;
import java.util.Map;


public class HiveTracking {

    public enum Alliance { RED, BLUE }

    public enum StartPose {

        RED_DOWN (Alliance.RED,   60.0,   9.0,   90.0),
        RED_UP   (Alliance.RED,   60.0, 132.5,  -90.0),
        BLUE_DOWN(Alliance.BLUE,  82.0,   9.0,   90.0),
        BLUE_UP  (Alliance.BLUE,  84.0, 132.5,  -90.0);

        public final Alliance alliance;
        public final double x, y, headingDeg;

        StartPose(Alliance alliance, double x, double y, double headingDeg) {
            this.alliance = alliance;
            this.x = x;
            this.y = y;
            this.headingDeg = headingDeg;
        }

        /** Pose para passar no pimpas.setPose(...). O heading vai em radianos. */
        public Pose toPose() {
            return new Pose(x, y, Math.toRadians(headingDeg));
        }
    }

    // ---------- Hives (X, Y) = PONTO DE MIRA (nao a posicao das tags) ----------
    // Confirmado: a boca fica ~3 in a frente do painel das tags (por isso Y difere do TAG_POS).
    private static final double RED_DOWN_X = 58.0,  RED_DOWN_Y = 56.5;
    private static final double RED_UP_X   = 58.0,  RED_UP_Y   = 82.0;
    private static final double BLUE_DOWN_X = 84.0, BLUE_DOWN_Y = 59.0;
    private static final double BLUE_UP_X   = 84.0, BLUE_UP_Y   = 84.0;

    // ---------- IDs das AprilTags ----------
    public static final int[] RED_DOWN_TAGS = {30, 31, 32, 33};
    public static final int[] RED_UP_TAGS   = {34, 35, 36, 37};
    public static final int[] BLUE_DOWN_TAGS = {};  // TODO IDs das tags azuis
    public static final int[] BLUE_UP_TAGS   = {};  // TODO IDs das tags azuis

    // ---------- Posicao (X, Y) de cada AprilTag no campo ----------
    // Mesmo sistema de coordenadas das hives/Pinpoint (polegadas). Medir o CENTRO de cada tag.
    // Cada tag fica num ponto diferente da hive, entao o tx dela NAO aponta para o centro
    // da hive -> sem isso, ver a tag da esquerda puxa a mira para a esquerda e vice-versa.
    // Enquanto estiver NaN, a tag e comparada com o centro da hive (comportamento antigo).
    private static final Map<Integer, double[]> TAG_POS = new HashMap<>();
    static {
        // Medido com a Limelight (Tag Size 80,3 mm). Y = face das tags; X centralizado
        // na hive (tags simetricas na boca), mantendo o espacamento medido.
        // Mesmos valores do Limelight/hives_vermelhas.fmap -> mudar os dois juntos.
        // RED_DOWN (face aponta para -y)
        tag(30, 51.5, 60.1);
        tag(31, 55.4, 60.1);
        tag(32, 60.6, 60.1);
        tag(33, 64.5, 60.1);
        // RED_UP (face aponta para +y)
        tag(34, 64.3, 78.9);
        tag(35, 60.8, 78.9);
        tag(36, 55.2, 78.9);
        tag(37, 51.7, 78.9);
    }

    private static void tag(int id, double x, double y) {
        TAG_POS.put(id, new double[]{x, y});
    }

    /** true se a posicao da tag ja foi preenchida em TAG_POS. */
    public static boolean hasTagPosition(int id) {
        double[] p = TAG_POS.get(id);
        return p != null && !Double.isNaN(p[0]) && !Double.isNaN(p[1]);
    }

    // ---------- Ajustes ----------
    /** Folga (polegadas) em volta da linha divisória, evita ficar trocando de lado. */
    private static final double HYSTERESIS_IN = 3.0;

    /** Onde a turret aponta quando o motor está em 0° (0 = para a frente do robô). */
    private static final double TURRET_ZERO_OFFSET_DEG = 0.0;

    /**
     * Posição do EIXO de giro da turret em relação ao centro do robô (polegadas).
     * X = para a FRENTE (+) / tras (-), Y = para a ESQUERDA (+) / direita (-).
     * "Centro do robô" = o mesmo ponto usado nos pod offsets do PinpointConf.
     * Com 0 aqui e a turret fora do centro, a mira erra de um jeito que muda com a
     * posicao e o heading do robo.
     * Medido: eixo 24,8 cm para a FRENTE do centro, alinhado no centro na lateral.
     * (public: o VisionPoseCorrector usa para ir da camera ao centro do robo)
     */
    public static final double TURRET_OFFSET_X_IN = 24.8 / 2.54;  // 24,8 cm = 9,76 in
    public static final double TURRET_OFFSET_Y_IN = 0.0;

    private final Alliance alliance;
    private final double hiveUpX, hiveUpY, hiveDownX, hiveDownY;
    private final double divisorY;
    private boolean sideUp;

    public HiveTracking(StartPose start) {
        this.alliance = start.alliance;

        if (alliance == Alliance.RED) {
            hiveUpX = RED_UP_X;     hiveUpY = RED_UP_Y;
            hiveDownX = RED_DOWN_X; hiveDownY = RED_DOWN_Y;
        } else {
            hiveUpX = BLUE_UP_X;     hiveUpY = BLUE_UP_Y;
            hiveDownX = BLUE_DOWN_X; hiveDownY = BLUE_DOWN_Y;
        }

        // Linha divisória = ponto médio (em Y) entre as duas hives da aliança
        divisorY = (hiveUpY + hiveDownY) / 2.0;
        sideUp = start.y > divisorY;
    }

    /** Chame todo loop com a posição do Pinpoint. Retorna o SideToTrack (true = cima). */
    public boolean update(double x, double y) {
        if (sideUp) {
            if (y < divisorY - HYSTERESIS_IN) sideUp = false;
        } else {
            if (y > divisorY + HYSTERESIS_IN) sideUp = true;
        }
        return sideUp;
    }

    public boolean isSideUp() { return sideUp; }
    public Alliance getAlliance() { return alliance; }
    public double getHiveX() { return sideUp ? hiveUpX : hiveDownX; }
    public double getHiveY() { return sideUp ? hiveUpY : hiveDownY; }

    /** IDs das AprilTags da hive que está sendo rastreada agora. */
    public int[] getTagIds() {
        if (alliance == Alliance.RED) return sideUp ? RED_UP_TAGS : RED_DOWN_TAGS;
        return sideUp ? BLUE_UP_TAGS : BLUE_DOWN_TAGS;
    }

    public boolean isTrackedTag(int id) {
        for (int t : getTagIds()) if (t == id) return true;
        return false;
    }

    /** Posicao (x, y) do EIXO da turret no campo, a partir da pose do robo. */
    public static double[] turretAxis(double x, double y, double headingFieldDeg) {
        double h = Math.toRadians(headingFieldDeg);
        return new double[]{
                x + TURRET_OFFSET_X_IN * Math.cos(h) - TURRET_OFFSET_Y_IN * Math.sin(h),
                y + TURRET_OFFSET_X_IN * Math.sin(h) + TURRET_OFFSET_Y_IN * Math.cos(h)
        };
    }

    /**
     * Ângulo (graus) que a turret deve ter para apontar para a hive atual,
     * usando só a odometria. 0 = frente do robô, anti-horário positivo.
     */
    public double turretAngleDeg(double x, double y, double headingFieldDeg) {
        return turretAngleToPointDeg(getHiveX(), getHiveY(), x, y, headingFieldDeg);
    }

    /**
     * Ângulo (graus) da turret para apontar para uma AprilTag especifica.
     * Se a posicao da tag nao foi preenchida, retorna o angulo para o centro da hive.
     */
    public double turretAngleToTagDeg(int id, double x, double y, double headingFieldDeg) {
        if (!hasTagPosition(id)) return turretAngleDeg(x, y, headingFieldDeg);
        double[] p = TAG_POS.get(id);
        return turretAngleToPointDeg(p[0], p[1], x, y, headingFieldDeg);
    }

    private double turretAngleToPointDeg(double px, double py,
                                         double x, double y, double headingFieldDeg) {
        double[] axis = turretAxis(x, y, headingFieldDeg);
        double paraPonto = Math.toDegrees(Math.atan2(py - axis[1], px - axis[0]));
        return normalize180(paraPonto - headingFieldDeg - TURRET_ZERO_OFFSET_DEG);
    }

    /** Distância (polegadas) do CENTRO do robô até a hive atual. */
    public double distanceToHive(double x, double y) {
        return Math.hypot(getHiveX() - x, getHiveY() - y);
    }

    /** Distância (polegadas) do EIXO DA TURRET até a hive atual. Use esta para o shooter. */
    public double distanceFromTurretToHive(double x, double y, double headingFieldDeg) {
        double[] axis = turretAxis(x, y, headingFieldDeg);
        return Math.hypot(getHiveX() - axis[0], getHiveY() - axis[1]);
    }

    private static double normalize180(double deg) {
        return ((deg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }
}