package org.firstinspires.ftc.teamcode.Mech.Turret;


public class TurretConfig {

    // >>> Mecanica <<<
    public static double ticksPerRev = 28.0;
    public static double gearRatio = 18.88;  // reducao 4 e 5

    // >>> Variaveis de Ajuste <<<
    public static double kP = 0.0145;     // subir ate oscilar e voltar ~20%
    public static double kD = 0.00011;   // amortece o erro de velocidade (alvo - turret)
    public static double kS = 0.047;     // medir com a TurretTunning (potencia minima que move)
    public static double kV = 0.0008;   // ~ 1 / velocidade maxima da turret (graus/s)

    // Filtro da velocidade do alvo (0..1). Maior = responde rapido, porem com mais ruido.
    public static double targetVelAlpha = 0.35167;

    // ---------- Limites e zona morta ----------

    public static double minDeg = -400, maxDeg = 400;

    // 1 tick do encoder = ~0.68 graus; zona morta menor que isso faz a turret ficar "cacando"
    public static double deadbandDeg = 1.0;
    public static double maxPower = 0.9;
    public static double nominalV = 12.0;

    // ---------- Visao / tracking ----------
    public static double alpha = 0.6;
    public static double lockToleranceDeg = 2;
    public static long maxStaleMs = 110;
    public static long lostTimeoutMs = 300;
    public static int confirmFrames = 3;
    // tx da Limelight e positivo quando a tag esta a DIREITA da mira. Como o angulo da
    // turret e ANTI-HORARIO positivo, tag a direita = turret precisa DIMINUIR o angulo -> -1.
    // (So vale +1 se a camera estiver montada de cabeca para baixo / espelhada.)
    public static int txSign = -1;
    // Sinal do giro do chassi (so para getChassisVelocity/telemetria).
    // O feedforward agora usa a velocidade do alvo (TurretTracking.getTargetVelocity).
    public static int headingSign = 1;

    // A HiveTracking assume angulo da turret ANTI-HORARIO positivo (visto de cima).
    // Teste: tracking desligado, aperte o gatilho DIREITO -> "Angulo" na telemetria sobe.
    // Se a turret girar no sentido HORARIO, deixe true. Inverte potencia E encoder juntos.
    public static boolean turretReversed = true;

    /** Ticks do encoder por grau de giro da TORRETA. Ex.: 28 * 18.88 / 360 = 1.47 */
    public static double ticksPerDeg() {
        return ticksPerRev * gearRatio / 360.0;
    }

    /**
     * Correcao da pose do Pinpoint pela Limelight (VisionPoseCorrector).
     * Unidades: polegadas, graus, ms (mesmo padrao da HiveTracking / Pinpoint).
     * As POSICOES das tags ficam no field map (.fmap) da Limelight, nao aqui.
     */
    public static class PoseCorrection {

        // ---------- Agendamento ----------
        /** Intervalo minimo entre duas correcoes aplicadas (ms). */
        public static long correctionIntervalMs = 4000;
        /** Medidas boas na janela necessarias para corrigir. */
        public static int minSamplesPerCorrection = 3;
        /** Medidas mais velhas que isso saem da janela (ms). */
        public static long sampleMaxAgeMs = 2000;

        // ---------- Quanto corrigir ----------
        /** Fracao do erro medio de posicao aplicada por correcao (0..1). 1 = pula direto. */
        public static double positionGain = 0.7;
        /** Corrigir heading pela visao? Comecar false: o IMU do Pinpoint costuma ser melhor. */
        public static boolean correctHeading = false;
        public static double headingGain = 0.3;

        // ---------- Criterios de aceitacao ----------
        public static long maxStaleMs = 110;
        /** Minimo de tags usadas no botpose. */
        public static int minTagCount = 1;
        /** Distancia media maxima ate as tags (in). Longe = pose ruidosa. */
        public static double maxTagDistIn = 100;
        /** So mede com robo quase parado (motion blur + latencia). */
        public static double maxRobotSpeedInPerSec = 15;
        public static double maxRobotTurnDegPerSec = 45;
        /** Camera na torreta: torreta tambem precisa estar quase parada. */
        public static double maxTurretSpeedDegPerSec = 60;
        /** Diferenca maxima entre yaw da camera medido e esperado (heading + torreta). */
        public static double maxYawErrorDeg = 10;
        /** Erro de posicao acima disso e suspeito (outlier). */
        public static double maxJumpIn = 8;
        /** Outliers seguidos e coerentes que fazem aceitar (odometria muito derivada). */
        public static int outlierRecoveryCount = 6;
        /** "Coerentes" = cada outlier a menos disso da media dos anteriores (in). */
        public static double outlierConsistencyIn = 3;
        /** Limites do campo no referencial do Pinpoint/Pedro (in). */
        public static double fieldMinIn = 0, fieldMaxIn = 144;

        // ---------- Montagem da camera ----------
        /** true = Limelight presa na TORRETA. false = fixa no chassi. */
        public static boolean cameraOnTurret = true;
        /** Lente em relacao ao EIXO da torreta (com torreta em 0 graus). Medido: 0,5 cm frente. */
        public static double camFwdIn = 0.5 / 2.54;
        public static double camLeftIn = 0.0;
        /** Yaw da camera em relacao a direcao 0 da torreta. TODO calibrar. */
        public static double camYawOffsetDeg = 0.0;

        // ---------- Referencial ----------
        /**
         * Limelight (FTC: centro do campo, metros) -> Pinpoint/Pedro (canto, polegadas):
         *   pedro = Rot(ftcToPedroRotDeg) * ftc + (fieldCenterIn, fieldCenterIn)
         * CONFIRMAR com X desligado comparando "Pose visao" x "Pose odo".
         */
        public static double ftcToPedroRotDeg = -90.0;
        public static double fieldCenterIn = 72.0;
    }
}