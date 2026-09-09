package org.firstinspires.ftc.teamcode.AUTONOMO.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


@Disabled
class PedroPathing1 {
    public PathChain MainChain;

    public PedroPathing1 (Follower follower) {
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
public class AzulEsquerda extends LinearOpMode{

    private Follower follower;

    public void runOpMode(){

        follower = Constants.createFollower(hardwareMap);

        follower.setStartingPose(new Pose(56.000, 9.00));


        if (isStopRequested()) return;

                // ==> MainChain <==
        PedroPath thePath = new PedroPath(follower);
        follower.followPath(thePath.MainChain);
        //==========================================


        while (opModeIsActive() && follower.isBusy()){

            follower.update();

            telemetry.addData("X", follower.getPose().getX());
            telemetry.addData("Y", follower.getPose().getY());

        }
    }
}
