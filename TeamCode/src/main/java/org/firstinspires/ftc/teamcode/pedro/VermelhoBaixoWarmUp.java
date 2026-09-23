package org.firstinspires.ftc.teamcode.pedro;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Autonomous(name = "Autonomo WarmUp - VERMELHO (Midtake Fix)", group = "Autonomous")
public class VermelhoBaixoWarmUp extends LinearOpMode {

    private DcMotor leftFront  = null;
    private DcMotor rightFront = null;
    private DcMotor leftBack   = null;
    private DcMotor rightBack  = null;

    private DcMotor leftShooter  = null;
    private DcMotor rightShooter = null;
    private DcMotor midTake      = null;
    private DcMotor feeder       = null;

    private IMU imu = null;
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {

        // Mapeamento
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");

        leftShooter  = hardwareMap.get(DcMotor.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotor.class, "rightShooter");
        midTake      = hardwareMap.get(DcMotor.class, "midTake");
        feeder       = hardwareMap.get(DcMotor.class, "feeder");

        // IMU
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(parameters);

        // Sentido dos motores de tração
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        rightBack.setDirection(DcMotorSimple.Direction.FORWARD);

        // Sentido dos mecanismos
        leftShooter.setDirection(DcMotorSimple.Direction.FORWARD);
        rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

        midTake.setDirection(DcMotorSimple.Direction.REVERSE);
        feeder.setDirection(DcMotorSimple.Direction.FORWARD);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Aguardando início (PLAY)...");
        telemetry.update();

        waitForStart();
        runtime.reset();

        imu.resetYaw();

        if (opModeIsActive()) {



            leftShooter.setPower(0.85);
            rightShooter.setPower(0.85);
            midTake.setPower(0.9);
            sleep(2000);

            leftShooter.setPower(0);
            rightShooter.setPower(0);
            midTake.setPower(0);

            // 2. VIRA PARA A ESQUERDA (90 GRAUS)

            girarParaAngulo(-90.0, 2.0);

            // 3. ANDA RETO
            telemetry.addData("Passo", "3. Andando para frente");
            telemetry.update();
            driveMecanum(0, 0.5, 0);
            sleep(1500);
            pararTracao();

            // 4. ATIVA O FEEDER POR 2 SEGUNDOS
            telemetry.addData("Passo", "4. Ativando feeder");
            telemetry.update();
            feeder.setPower(0.7);
            sleep(2000);
            feeder.setPower(0);

            // 5. VOLTA PARA A DIREÇÃO INICIAL (0 GRAUS)
            telemetry.addData("Passo", "5. Retornando ao ângulo inicial (0°)");
            telemetry.update();
            girarParaAngulo(0.0, 2.0);

            // 6. ANDA PARA FRENTE POR 1.5 SEGUNDOS
            telemetry.addData("Passo", "6. Avançando por 1.5 segundos");
            telemetry.update();
            driveMecanum(0, 0.5, 0);
            sleep(1500);

            pararTracao();
            telemetry.addData("Status", "Autônomo Finalizado!");
            telemetry.update();
        }
    }

    private void girarParaAngulo(double anguloAlvo, double timeoutSegundos) {
        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        double kp = 0.012;
        double margemErro = 2.5;
        double minPower = 0.15;

        while (opModeIsActive() && timer.seconds() < timeoutSegundos) {
            double anguloAtual = obterAnguloAtual();
            double erro = anguloAlvo - anguloAtual;

            while (erro > 180)  erro -= 360;
            while (erro <= -180) erro += 360;

            if (Math.abs(erro) <= margemErro) {
                break;
            }

            double potenciaTurn = -erro * kp;
            potenciaTurn = Math.max(-0.5, Math.min(0.5, potenciaTurn));

            if (Math.abs(potenciaTurn) < minPower) {
                potenciaTurn = minPower * Math.signum(potenciaTurn);
            }

            driveMecanum(0, 0, potenciaTurn);

            telemetry.addData("Giro IMU", "Alvo: %.1f | Atual: %.1f | Erro: %.1f", anguloAlvo, anguloAtual, erro);
            telemetry.update();
        }

        pararTracao();
    }

    private double obterAnguloAtual() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }

    private void driveMecanum(double strafe, double drive, double turn) {
        double fl = drive + strafe + turn;
        double fr = drive - strafe - turn;
        double bl = drive - strafe + turn;
        double br = drive + strafe - turn;

        double max = Math.max(Math.abs(fl), Math.max(Math.abs(fr), Math.max(Math.abs(bl), Math.abs(br))));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        leftFront.setPower(fl);
        rightFront.setPower(fr);
        leftBack.setPower(bl);
        rightBack.setPower(br);
    }

    private void pararTracao() {
        driveMecanum(0, 0, 0);
        sleep(200);
    }
}