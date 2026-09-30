package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class TurretServo {

    public enum LimeStatus {CATCH, LEFT, RIGHT, NONE}

    // ================= TUNING =================
    public static double kP = 0.006113;
    public static double kD = 0;
    public static double kS = 0.055;         // potência mínima pra vencer o atrito
    public static double MAX_POWER = 0.8;
    public static double MAX_ACCEL = 6.0;

    public static double ENTER_DB = 1.5;    // graus: trava
    public static double EXIT_DB  = 3;    // graus: destrava (histerese)
    public static double KS_FADE  = 4.0;    // graus: abaixo disso o kS diminui

    public static double LOST_HOLD = 0.10;
    public static double SEARCH_MIN_TX = 6.0;
    public static double SEARCH_POWER = 0.189;
    public static double SEARCH_TIMEOUT = 1;

    public static double MANUAL_POWER = 0.2867;
    public static int DIR = -1;
    // ==========================================

    private CRServo servo;
    private final ElapsedTime loopTimer = new ElapsedTime();
    private final ElapsedTime lostTimer = new ElapsedTime();
    private final ElapsedTime searchTimer = new ElapsedTime();

    private LimeStatus act = LimeStatus.NONE;
    private LimeStatus lastAct = LimeStatus.NONE;

    private double lastTX = 0;
    private double prevTx = 0, prevTxTime = 0, dTx = 0;
    private boolean havePrev = false;

    private boolean locked = false;
    private boolean searching = false;
    private double searchDir = 0;
    private double lastPower = 0;

    public void init(HardwareMap hardwareMap) {
        servo = hardwareMap.get(CRServo.class, "TurretServo");
        loopTimer.reset();
        lostTimer.reset();
    }

    public void TurretTracking(Gamepad gamepad2, boolean RTp1, boolean LTp1,
                               boolean tagVisible, double tx, boolean Y_ON) {

        double dt = Math.max(loopTimer.seconds(), 1e-3);
        loopTimer.reset();
        double now = System.nanoTime() * 1e-9;

        // ---------- MANUAL ----------
        if (!Y_ON) {
            resetState();
            if (RTp1)      output(MANUAL_POWER, dt, false);
            else if (LTp1) output(-MANUAL_POWER, dt, false);
            else           output(0, dt, false);
            return;
        }

        // ---------- TAG PERDIDA ----------
        if (!tagVisible) {
            havePrev = false;
            locked = false;
            act = LimeStatus.NONE;

            boolean lostFarFromCenter = Math.abs(lastTX) >= SEARCH_MIN_TX;

            if (lostTimer.seconds() < LOST_HOLD || !lostFarFromCenter) {
                searching = false;
                output(0, dt, false);
                return;
            }

            if (!searching) {
                searching = true;
                searchDir = Math.signum(lastTX);
                searchTimer.reset();
            }
            if (searchTimer.seconds() > SEARCH_TIMEOUT) {
                searchDir = -searchDir;
                searchTimer.reset();
            }
            output(searchDir * SEARCH_POWER, dt, true);
            return;
        }

        // ---------- TAG VISÍVEL ----------
        searching = false;
        lostTimer.reset();
        lastTX = tx;


        if (havePrev && tx != prevTx) {
            double raw = (tx - prevTx) / Math.max(now - prevTxTime, 1e-3);
            dTx = 0.5 * dTx + 0.5 * raw;   
            prevTx = tx;
            prevTxTime = now;
        } else if (!havePrev) {
            dTx = 0;
            prevTx = tx;
            prevTxTime = now;
            havePrev = true;
        }


        double absTx = Math.abs(tx);
        if (locked && absTx > EXIT_DB) locked = false;
        if (!locked && absTx < ENTER_DB) {
            locked = true;
            gamepad2.rumble(0.5, 0.5, 500);
        }

        if (locked) {
            act = LimeStatus.CATCH;
            output(0, dt, false);
        } else {
            act = tx < 0 ? LimeStatus.LEFT : LimeStatus.RIGHT;

            //double ffScale = Math.min(1.0, absTx / KS_FADE);
            double power = kP * tx + kD * dTx + Math.signum(tx) * kS /* * ffScale*/;
            power = clamp(power, -MAX_POWER, MAX_POWER);
            output(power, dt, true);
        }

        lastAct = act;
    }


    private void output(double target, double dt, boolean ramp) {
        double p = target;
        if (ramp && Math.abs(target) > Math.abs(lastPower)) {
            double maxDelta = MAX_ACCEL * dt;
            p = lastPower + clamp(target - lastPower, -maxDelta, maxDelta);
        }
        lastPower = p;
        servo.setPower(DIR * p);
    }

    private void resetState() {
        locked = false;
        searching = false;
        havePrev = false;
        act = LimeStatus.NONE;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public LimeStatus getAct()     { return act; }
    public LimeStatus getLastAct() { return lastAct; }
    public double getLastTx()      { return lastTX; }
    public double getPower()       { return lastPower; }
}