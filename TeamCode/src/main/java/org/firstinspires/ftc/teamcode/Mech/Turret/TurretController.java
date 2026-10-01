package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;


/*
 * TurretController: leva a torreta ate um ANGULO ALVO (em graus). Nao sabe nada de Limelight.
 * Controle: P no erro + kS (atrito) - kD na velocidade medida - kV no giro do chassi.
 * O zero do encoder e onde a torreta estiver quando esta classe e criada (INIT) => deve ser o CENTRO.
 */
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



    public void update(double ffDegPerSec, double battV) {
        // >>>Leituras<<<
        double tpd = TurretConfig.ticksPerDeg();
        angleDeg = motor.getCurrentPosition() / tpd;
        double rawVel = motor.getVelocity() / tpd;
        velDegPerSec = 0.7 * velDegPerSec + 0.3 * rawVel; // filtro do encoder de 28 ticks e ruidoso

        if (!enabled) { write(manualPower); return; }


        double err = getError();
        double p = (Math.abs(err) < TurretConfig.deadbandDeg)
                ? 0.0
                : TurretConfig.kP * err + TurretConfig.kS * Math.signum(err);

        // freio velocidade e compensacao do giro do chassi
        double power = p - TurretConfig.kD * velDegPerSec - TurretConfig.kV * ffDegPerSec;

        // compensa de bateria, limita e protege os limites
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