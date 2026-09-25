package org.firstinspires.ftc.teamcode.pedro.WarmUp_WithoutPedro;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import org.firstinspires.ftc.teamcode.Another_Codes.UsualFunctions;

import java.util.List;


@Autonomous(name = "HiveDownOneTip", group = "Autonomous")
public class HiveDownOneTip extends LinearOpMode {

    private DcMotor leftFront, leftBack, rightFront, rightBack;

        //MECH

    private DcMotor feeder, midTake;
    private Servo RampServo;
    private IMU imu;
    private DcMotorEx leftShooter, rightShooter;

    UsualFunctions func = new UsualFunctions();


    public void runOpMode(){

        RampServo.setPosition(func.RampCatchPos);

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        INIT();


        while (opModeIsActive()){

            feeder.setPower(0.9);
            leftShooter.setVelocityPIDFCoefficients(51, 0, 0, 19.87);
            rightShooter.setVelocityPIDFCoefficients(51, 0,0, 16.54);
            leftShooter.setVelocity(1590);
            rightShooter.setVelocity(1590);
            sleep(500);
            midTake.setPower(-0.7);
            sleep(3200);
            leftShooter.setVelocity(0);
            rightShooter.setVelocity(0);
            midTake.setPower(0);

            driveMecanum(0, 0.5, 0);
            sleep(700);
            StopAll();
            driveMecanum(0.9, 0, 0);
            sleep(1300);

        }
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

    private void INIT(){

        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");
        leftShooter = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");
        rightShooter.setDirection(DcMotor.Direction.REVERSE);

        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        feeder  = hardwareMap.get(DcMotor.class, "feeder");
        midTake = hardwareMap.get(DcMotor.class, "midTake");


        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        midTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        RampServo = hardwareMap.get(Servo.class, "rightRampServo");

        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
        );

        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();

    }

    private void girarParaAngulo(double anguloAlvo, double timeoutSegundos) {
        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        double kp = 0.012;
        double margemErro = 2.5;
        double minPower = 0.15;

        while (opModeIsActive() && timer.seconds() < timeoutSegundos) {
            double anguloAtual = CurrentAngle();
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

        StopAll();
    }

    private double CurrentAngle() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }

    private void StopAll(){
        driveMecanum(0, 0, 0);
    }


}
