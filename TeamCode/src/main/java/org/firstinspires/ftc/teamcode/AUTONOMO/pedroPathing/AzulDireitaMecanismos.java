package org.firstinspires.ftc.teamcode.AUTONOMO.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Disabled
@Autonomous(name = "azulDireitaMecanismos", group = "Autonomous")
public class AzulDireitaMecanismos extends LinearOpMode {

    private Follower follower;

    private DcMotor spindexer, feeder, shooter;
    private Servo servoLeft, servoRight;

    private double posZeroEsquerda = 0.0;
    private double posZeroDireita = 0.1754;
    private final double SERVO_ATIVO = 0.48;
    private final double TEMPO_ESPERA = 1.9;

    @Override
    public void runOpMode() {

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(21.40, 128.87));

        spindexer  = hardwareMap.get(DcMotor.class, "Spindexer");
        feeder     = hardwareMap.get(DcMotor.class, "feeder");
        shooter    = hardwareMap.get(DcMotor.class, "shooter");
        servoLeft  = hardwareMap.get(Servo.class, "servoLeft");
        servoRight = hardwareMap.get(Servo.class, "servoRight");

        servoLeft.setDirection(Servo.Direction.FORWARD);
        servoRight.setDirection(Servo.Direction.FORWARD);

        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        servoLeft.setPosition(posZeroEsquerda);
        servoRight.setPosition(posZeroDireita);

        if (isStopRequested()) return;

        Path002 thePath = new Path002(follower);

        waitForStart();
        if (isStopRequested()) return;

        follower.followPath(thePath.MainChain);
        seguirPathComTelemetria();

        executarSequenciaDeTiro(true, 3.0);

        follower.followPath(thePath.SecondChain);
        seguirPathComTelemetria();

        executarSequenciaDeTiro(false, 3.0);

        follower.followPath(thePath.ThirdChain);
        seguirPathComTelemetria();

        pararMecanismos();
    }

    private void seguirPathComTelemetria() {
        while (opModeIsActive() && follower.isBusy()) {
            follower.update();

            pararMecanismos();

            telemetry.addData("X", follower.getPose().getX());
            telemetry.addData("Y", follower.getPose().getY());
            telemetry.addData("Heading", Math.toDegrees(follower.getPose().getHeading()));
            telemetry.update();
        }
    }

    private void executarSequenciaDeTiro(boolean ladoEsquerdo, double duracaoTiro) {
        double shooterPower = ladoEsquerdo ? 1.0 : -1.0;
        ElapsedTime timer = new ElapsedTime();

        shooter.setPower(shooterPower);
        timer.reset();

        while (opModeIsActive() && timer.seconds() < TEMPO_ESPERA) {
            follower.update();
            spindexer.setPower(0);
            servoLeft.setPosition(posZeroEsquerda);
            servoRight.setPosition(posZeroDireita);
            feeder.setPower(0);

            telemetry.addData("Status", "ACELERANDO...");
            telemetry.addData("Timer", "%.2f s", timer.seconds());
            telemetry.update();
        }

        double inicioTiro = timer.seconds();
        while (opModeIsActive() && (timer.seconds() - inicioTiro) < duracaoTiro) {
            follower.update();

            spindexer.setPower(shooterPower * 0.8);

            if (ladoEsquerdo) {
                servoLeft.setPosition(Range.clip(posZeroEsquerda + SERVO_ATIVO, 0.0, 1.0));
                servoRight.setPosition(posZeroDireita);
            } else {
                servoRight.setPosition(Range.clip(posZeroDireita + SERVO_ATIVO, 0.0, 1.0));
                servoLeft.setPosition(posZeroEsquerda);
            }

            feeder.setPower(1.0);

            telemetry.addData("Status", "ATIRANDO");
            telemetry.addData("Timer", "%.2f s", timer.seconds());
            telemetry.update();
        }

        pararMecanismos();
    }

    private void pararMecanismos() {
        shooter.setPower(0);
        spindexer.setPower(0);
        feeder.setPower(0);
        servoLeft.setPosition(posZeroEsquerda);
        servoRight.setPosition(posZeroDireita);
    }
}

class Path002 {
    public PathChain MainChain;
    public PathChain SecondChain;
    public PathChain ThirdChain;

    public Path002(Follower follower) {

        MainChain = follower.pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(21.400, 120.870),
                                new Pose(27.000, 103.340),
                                new Pose(23.308, 81.802)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(-38), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(23.308, 81.802),
                                new Pose(22.903, 50.193)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierCurve(
                                new Pose(22.903, 50.193),
                                new Pose(25.900, 79.975),
                                new Pose(44.510, 98.193)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierCurve(
                                new Pose(44.510, 98.193),
                                new Pose(39.900, 58.400),
                                new Pose(122.118, 58.044)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierCurve(
                                new Pose(122.118, 58.044),
                                new Pose(75.800, 53.600),
                                new Pose(44.646, 98.049)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierCurve(
                                new Pose(44.646, 98.049),
                                new Pose(78.000, 106.200),
                                new Pose(76.840, 78.344)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierCurve(
                                new Pose(76.840, 78.344),
                                new Pose(79.400, 58.134),
                                new Pose(102.854, 32.931)
                        )
                )
                .setTangentHeadingInterpolation()
                .build();

        SecondChain = follower.pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(102.854, 32.931),
                                new Pose(90.000, 45.000),
                                new Pose(60.000, 40.000)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierLine(
                                new Pose(60.000, 40.000),
                                new Pose(30.000, 36.000)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(0))
                .build();

        ThirdChain = follower.pathBuilder()
                .addPath(
                        new BezierCurve(
                                new Pose(30.000, 36.000),
                                new Pose(50.000, 55.000),
                                new Pose(23.308, 81.802)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(-38))
                .build();
    }
}