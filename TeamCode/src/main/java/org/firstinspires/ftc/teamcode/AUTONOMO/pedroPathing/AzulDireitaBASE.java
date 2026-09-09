package org.firstinspires.ftc.teamcode.AUTONOMO.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Disabled
@Autonomous(name = "azulDireita", group = "Autonomous")
public class AzulDireitaBASE extends LinearOpMode {

    private Follower follower;

    public void runOpMode(){

        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose(21.40, 128.87));

        if (isStopRequested()) return;

        PedroPath thepath = new PedroPath(follower);

                //==> MainChain <==
        follower.followPath(thepath.MainChain);

        waitForStart();

        while(follower.isBusy() && opModeIsActive()){

            follower.update();

            //Falta a implementação dos mecânismos
            // Feeder, shooter, spindexer e etc...

            telemetry.addData("X", follower.getPose().getX());
            telemetry.addData("Y", follower.getPose().getY());
            telemetry.update();

        }

    }
}

class PedroPath {
    public PathChain MainChain;

    public PedroPath(Follower follower) {
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
    }
}
