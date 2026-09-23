package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp(name = "Shooter PIDF Tuning", group = "Tuning")
public class PIDFTTUNING extends LinearOpMode {

    private DcMotorEx shooter;
    private DcMotorEx intake;
    private DcMotorEx leftshoot, rightShooter, midTake;

    // =========================
    // VALORES INICIAIS
    // =========================

    double targetVelocity = 1175;

    double P = 50;
    double F = 15;

    // =========================
    // PASSOS DO TUNING
    // =========================

    double velocityStep = 50;
    double pStep = 1.0;
    double fStep = 0.5;

    @Override
    public void runOpMode() {

        // =========================
        // HARDWARE
        // =========================

        leftshoot = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");

        intake = hardwareMap.get(DcMotorEx.class, "feeder");
        midTake = hardwareMap.get(DcMotorEx.class, "midTake");


        leftshoot.setDirection(DcMotor.Direction.REVERSE);
        rightShooter.setDirection(DcMotor.Direction.FORWARD);
        intake.setDirection(DcMotor.Direction.FORWARD);
        midTake.setDirection(DcMotor.Direction.FORWARD);

        leftshoot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Intake e transfer usando potência
        intake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        midTake.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // Começam desligados
        intake.setPower(0);
        leftshoot.setPower(0);

        // =========================
        // PIDF
        // =========================

        shooter.setVelocityPIDFCoefficients(
                P,
                0,
                0,
                F
        );

        telemetry.addLine("SHOOTER PIDF TUNING");
        telemetry.addLine("-------------------");
        telemetry.addLine("D-PAD UP/DOWN = Velocidade");
        telemetry.addLine("A/B = Aumentar/Diminuir P");
        telemetry.addLine("X/Y = Aumentar/Diminuir F");
        telemetry.addLine("START = Resetar valores");
        telemetry.addLine("RIGHT BUMPER = Intake + Transfer");
        telemetry.update();

        waitForStart();

        ElapsedTime timer = new ElapsedTime();

        boolean lastUp = false;
        boolean lastDown = false;
        boolean lastA = false;
        boolean lastB = false;
        boolean lastX = false;
        boolean lastY = false;
        boolean lastStart = false;

        while (opModeIsActive()) {

            // ==========================================
            // VELOCIDADE
            // ==========================================

            boolean up = gamepad1.dpad_up;
            boolean down = gamepad1.dpad_down;

            if (up && !lastUp) {
                targetVelocity += velocityStep;
            }

            if (down && !lastDown) {
                targetVelocity -= velocityStep;
            }

            // ==========================================
            // P
            // ==========================================

            boolean a = gamepad1.a;
            boolean b = gamepad1.b;

            if (a && !lastA) {
                P += pStep;
            }

            if (b && !lastB) {
                P -= pStep;
            }

            // ==========================================
            // F
            // ==========================================

            boolean x = gamepad1.x;
            boolean y = gamepad1.y;

            if (x && !lastX) {
                F += fStep;
            }

            if (y && !lastY) {
                F -= fStep;
            }

            // ==========================================
            // RESET
            // ==========================================

            boolean start = gamepad1.start;

            if (start && !lastStart) {
                targetVelocity = 1000;
                P = 10.0;
                F = 0.0;
            }

            // Evita valores negativos
            if (targetVelocity < 0)
                targetVelocity = 0;

            if (P < 0)
                P = 0;

            if (F < 0)
                F = 0;

            // ==========================================
            // APLICA PIDF
            // ==========================================

            rightShooter.setVelocityPIDFCoefficients(
                    P,
                    0,
                    0,
                    F
            );

            leftshoot.setVelocityPIDFCoefficients(
                    P,
                    0,
                    0,
                    F
            );

            // ==========================================
            // RODA O SHOOTER
            // ==========================================

            leftshoot.setVelocity(targetVelocity);
            rightShooter.setVelocity(targetVelocity);

            // ==========================================
            // INTAKE + TRANSFER
            // RIGHT BUMPER
            // ==========================================

            if (gamepad1.right_bumper) {

                intake.setPower(1.0);
                midTake.setPower(0.35);

            } else {

                intake.setPower(0);
                midTake.setPower(0);
            }

            // ==========================================
            // TELEMETRY
            // ==========================================

            double currentVelocity = shooter.getVelocity();

            telemetry.addLine("===== SHOOTER TUNING =====");

            telemetry.addData(
                    "Target Velocity",
                    "%.0f",
                    targetVelocity
            );

            telemetry.addData(
                    "Current Velocity",
                    "%.0f",
                    currentVelocity
            );

            telemetry.addLine("");

            telemetry.addData("P", "%.3f", P);
            telemetry.addData("I", "0");
            telemetry.addData("D", "0");
            telemetry.addData("F", "%.3f", F);

            telemetry.addLine("");

            telemetry.addData(
                    "Erro",
                    "%.0f",
                    targetVelocity - currentVelocity
            );

            telemetry.addLine("");

            telemetry.addData(
                    "Intake/Transfer",
                    gamepad1.right_bumper ? "ON" : "OFF"
            );

            telemetry.addLine("");
            telemetry.addLine("D-PAD UP/DOWN = Velocity");
            telemetry.addLine("A/B = P +/-");
            telemetry.addLine("X/Y = F +/-");
            telemetry.addLine("START = Reset");
            telemetry.addLine("RB = Intake + Transfer");

            telemetry.update();

            // ==========================================
            // ATUALIZA ESTADOS DOS BOTÕES
            // ==========================================

            lastUp = up;
            lastDown = down;

            lastA = a;
            lastB = b;

            lastX = x;
            lastY = y;

            lastStart = start;

            // ==========================================
            // DELAY
            // ==========================================

            timer.reset();

            while (timer.milliseconds() < 20 && opModeIsActive()) {
                idle();
            }
        }

        // ==========================================
        // DESLIGA TUDO AO FINAL
        // ==========================================

        leftshoot.setVelocity(0);
        rightShooter.setVelocity(0);
        intake.setPower(0);
        midTake.setPower(0);
    }
}