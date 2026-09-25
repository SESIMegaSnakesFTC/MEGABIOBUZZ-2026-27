package org.firstinspires.ftc.teamcode.pedro.WarmUp_WithoutPedro;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Disabled
@Autonomous(name = "Model", group = "Autonomous")
public class Modelo extends LinearOpMode {

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


        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");

        leftShooter  = hardwareMap.get(DcMotor.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotor.class, "rightShooter");
        midTake      = hardwareMap.get(DcMotor.class, "midTake");
        feeder       = hardwareMap.get(DcMotor.class, "feeder");


        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
        ));
        imu.initialize(parameters);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.FORWARD);
        rightBack.setDirection(DcMotorSimple.Direction.FORWARD);

        leftShooter.setDirection(DcMotorSimple.Direction.FORWARD);
        rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

        midTake.setDirection(DcMotorSimple.Direction.REVERSE);
        feeder.setDirection(DcMotorSimple.Direction.FORWARD);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        waitForStart();
        runtime.reset();

        imu.resetYaw();

        if (opModeIsActive()) {

            //FUNCTIONS
            // {...}


        }
    }

    private void girarParaAngulo(double anguloAlvo, double timeoutSegundos) {
        ElapsedTime timer = new ElapsedTime();
        timer.reset();

        double kp = 0.012;
        double margemErro = 2.5;
        double minPower = 0.15;

        while (opModeIsActive() && timer.seconds() < timeoutSegundos) {
            double anguloAtual = getCurrentAngle();
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

            driveMech(0, 0, potenciaTurn);

        }

        StopMech();
    }

    private double getCurrentAngle() {
        YawPitchRollAngles orientation = imu.getRobotYawPitchRollAngles();
        return orientation.getYaw(AngleUnit.DEGREES);
    }

    private void driveMech(double x, double y, double rx) {
        double fl = y + x + rx;
        double bl = y - x + rx;
        double fr = y - x - rx;
        double br = y + x - rx;

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

    private void StopMech() {
        driveMech(0, 0, 0);
        sleep(200);
    }
}