package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;



public class TurretController {
    private final DcMotorEx motor;
    private double target = 0;
    private double lastPower = 0;

    public boolean enabled = false;   //para Controle manual
    public double manualPower = 0;
    public double angleDeg = 0;
    public double velDegPerSec = 0;   //Velocidade com filtro

    public TurretController(DcMotorEx motor) {
        this.motor = motor;
        motor.setDirection(TurretConfig.turretReversed
                ? DcMotorSimple.Direction.REVERSE
                : DcMotorSimple.Direction.FORWARD);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }


    public void setTarget(double deg) {
        double best = Double.NaN;
        for (int k = -3; k <= 3; k++) {
            double c = deg + 360.0 * k;
            if (c < TurretConfig.minDeg || c > TurretConfig.maxDeg) continue;
            if (Double.isNaN(best) || Math.abs(c - angleDeg) < Math.abs(best - angleDeg)) best = c;
        }
        target = Double.isNaN(best) ? clamp(deg, TurretConfig.minDeg, TurretConfig.maxDeg) : best;
    }

    public double getTarget() { return target; }
    public double getError() { return target - angleDeg; }
    public boolean atTarget(double tolDeg) { return Math.abs(getError()) < tolDeg; }
    public double getLastPower() { return lastPower; }



    /** @param targetVelDegPerSec velocidade que o ALVO esta andando (relativa ao chassi), em graus/s */
    public void update(double targetVelDegPerSec, double battV) {
        // >>>Leituras<<<

        double tpd = TurretConfig.ticksPerDeg();
        angleDeg = motor.getCurrentPosition() / tpd;
        double rawVel = motor.getVelocity() / tpd;
        velDegPerSec = 0.7 * velDegPerSec + 0.3 * rawVel; // filtro do encoder de 28 ticks

        if (!enabled) {
            double mp = manualPower;
            if ((angleDeg >= TurretConfig.maxDeg && mp > 0) ||
                    (angleDeg <= TurretConfig.minDeg && mp < 0)) mp = 0;  // não passa dos 400
            write(mp);
            return;
        }


        double err = getError();
        double p = (Math.abs(err) < TurretConfig.deadbandDeg)
                ? 0.0
                : TurretConfig.kP * err + TurretConfig.kS * Math.signum(err);


        // kV empurra na velocidade do alvo; kD amortece so a DIFERENCA de velocidade
        // (antes o D freava a turret justamente quando ela precisava acompanhar o giro do chassi)
        double velErr = targetVelDegPerSec - velDegPerSec;
        double power = p + TurretConfig.kV * targetVelDegPerSec + TurretConfig.kD * velErr;

        // compensação de bateria
        if (battV > 1.0) power *= TurretConfig.nominalV / battV;
        power = clamp(power, -TurretConfig.maxPower, TurretConfig.maxPower);
        if ((angleDeg >= TurretConfig.maxDeg && power > 0) ||
                (angleDeg <= TurretConfig.minDeg && power < 0)) power = 0;

        write(power);
    }


    private void write(double p) {
        if (p == 0 || Math.abs(p - lastPower) > 0.005) {
            motor.setPower(p);
            lastPower = p;
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}