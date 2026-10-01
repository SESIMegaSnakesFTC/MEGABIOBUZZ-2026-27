package org.firstinspires.ftc.teamcode.Mech.Turret;


public class TurretConfig {

    // >>> Mecanica <<<
    public static double ticksPerRev = 28.0;
    public static double gearRatio = 18.88;  // reducao 4 e 5

    // >>> Variaveis <<<
    public static double kP = 0.013;
    public static double kD = 0.0004;
    public static double kS = 0.05;
    public static double kV = 0.0;

    // ---------- Limites e zona morta ----------

    public static double minDeg = -400, maxDeg = 400;

    public static double deadbandDeg = 0.4;
    public static double maxPower = 0.9;
    public static double nominalV = 12.0;

    // ---------- Visao / tracking ----------
    public static double alpha = 0.6;
    public static double sweepDegPerSec = 120;
    public static double lockToleranceDeg = 1.0;
    public static long maxStaleMs = 110;
    public static long lostTimeoutMs = 300;
    public static int confirmFrames = 3;
    public static int txSign = -1;

    /** Ticks do encoder por grau de giro da TORRETA. Ex.: 28 * 18.88 / 360 = 1.47 */
    public static double ticksPerDeg() {
        return ticksPerRev * gearRatio / 360.0;
    }
}