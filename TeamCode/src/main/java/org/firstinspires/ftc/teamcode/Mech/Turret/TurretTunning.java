package org.firstinspires.ftc.teamcode.turret;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Mech.Turret.TurretConfig;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;



@TeleOp(name = "TurretTuning", group = "Turret")
public class TurretTuning extends LinearOpMode {

    private DcMotorEx turret;
    @Override
    public void runOpMode() {
        TurretController turret = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        VoltageSensor vs = hardwareMap.voltageSensor.iterator().next();

        double ramp = 0, ksPos = 0, ksNeg = 0, lastT = 0;
        boolean found = false, pUp = false, pDn = false, dUp = false, dDn = false;
        ElapsedTime t = new ElapsedTime();

        telemetry.addLine("Torreta no CENTRO (0 graus)? Confira a marca antes do Start.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            double now = t.seconds(), dt = now - lastT;
            lastT = now;

            // Etapa 1: achar kS (segure A para +, B para -)
            if (gamepad1.a || gamepad1.b) {
                if (!found) {
                    ramp += 0.02 * dt;                                  // +0.02 de potencia por segundo
                    turret.manualPower = (gamepad1.a ? 1 : -1) * ramp;
                    if (Math.abs(turret.velDegPerSec) > 3) {            // comecou a andar
                        found = true;
                        if (gamepad1.a) ksPos = ramp; else ksNeg = ramp;
                        turret.manualPower = 0;
                    }
                }
            } else {
                ramp = 0; found = false; turret.manualPower = 0;
            }
            if (gamepad1.back && ksPos > 0 && ksNeg > 0) TurretConfig.kS = 0.9 * (ksPos + ksNeg) / 2;

            // Etapas 2 e 3: controle de posicao com degraus
            if (gamepad1.right_bumper) turret.enabled = true;
            if (gamepad1.left_bumper) turret.enabled = false;
            if (gamepad1.x) turret.setTarget(30);
            if (gamepad1.y) turret.setTarget(-30);
            if (gamepad1.right_trigger > 0.5) turret.setTarget(0);

            // Ajuste ao vivo (um clique = um passo, por causa da deteccao de borda)
            if (gamepad1.dpad_up && !pUp) TurretConfig.kP += 0.001;
            if (gamepad1.dpad_down && !pDn) TurretConfig.kP = Math.max(0, TurretConfig.kP - 0.001);
            if (gamepad1.dpad_right && !dUp) TurretConfig.kD += 0.0001;
            if (gamepad1.dpad_left && !dDn) TurretConfig.kD = Math.max(0, TurretConfig.kD - 0.0001);
            pUp = gamepad1.dpad_up;   pDn = gamepad1.dpad_down;
            dUp = gamepad1.dpad_right; dDn = gamepad1.dpad_left;

            turret.update(0, vs.getVoltage());

            telemetry.addData("angulo", "%.1f", turret.angleDeg);
            telemetry.addData("alvo", "%.1f", turret.getTarget());
            telemetry.addData("erro", "%.2f", turret.getError());
            telemetry.addData("vel (graus/s)", "%.1f", turret.velDegPerSec);
            telemetry.addData("potencia", "%.3f", turret.getLastPower());
            telemetry.addData("kP / kD / kS", "%.4f / %.5f / %.3f", TurretConfig.kP, TurretConfig.kD, TurretConfig.kS);
            telemetry.addData("kS achado (+ / -)", "%.3f / %.3f", ksPos, ksNeg);
            telemetry.update();
        }

        turret.enabled = false;
        turret.manualPower = 0;
        turret.update(0, vs.getVoltage());
    }
}