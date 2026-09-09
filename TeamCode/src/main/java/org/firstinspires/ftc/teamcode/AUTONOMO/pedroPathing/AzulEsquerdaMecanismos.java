package org.firstinspires.ftc.teamcode.AUTONOMO.pedroPathing;

import com.pedropathing.follower.Follower;
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
@Autonomous(name = "azulEsquerdaMecanismos", group = "Autonomous")
public class AzulEsquerdaMecanismos extends LinearOpMode {

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
        follower.setStartingPose(new Pose(56.000, 9.000));

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

        PathAzulEsquerda thePath = new PathAzulEsquerda(follower);

        waitForStart();
        if (isStopRequested()) return;

        follower.followPath(thePath.MainChain);
        seguirPathComTelemetria();

        executarSequenciaDeTiro(true, 3.0);

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

class PathAzulEsquerda {
    public PathChain MainChain;

    public PathAzulEsquerda(Follower follower) {
        MainChain = follower.pathBuilder()
                .addPath(
                        new BezierLine(
                                new Pose(56.000, 9.000),
                                new Pose(39.008, 33.293)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(180))
                .addPath(
                        new BezierLine(
                                new Pose(39.008, 33.293),
                                new Pose(13.310, 35.614)
                        )
                )
                .setTangentHeadingInterpolation()
                .addPath(
                        new BezierLine(
                                new Pose(13.310, 35.614),
                                new Pose(70.650, 70.682)
                        )
                )
                .setLinearHeadingInterpolation(Math.toRadians(19), Math.toRadians(90))
                .addPath(
                        new BezierLine(
                                new Pose(70.650, 70.682),
                                new Pose(70.700, 124.600)
                        )
                )
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();
    }
}