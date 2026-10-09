package org.firstinspires.ftc.teamcode.pedro.Others.Ivy;

import static com.pedropathing.api.Paths.*;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.*;
import static com.pedropathing.ivy.groups.Groups.*;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;

@Disabled
@Autonomous(name = "Auto Exemplo Ivy", group = "Pedro")
public class Model extends OpMode {

    private Follower follower;
    private final PoseFactory p = PoseFactory.degrees();

    private final Pose startPose = p.of(24, 24, 0);
    private final Pose scorePose = p.of(48, 48, 90);
    private final Pose parkPose  = p.of(60, 20, 0);

    private Path toScore() {
        return line(startPose, scorePose).linear(startPose, scorePose);
    }

    private Path toPark() {
        return line(scorePose, parkPose).linear(scorePose, parkPose);
    }

    @Override
    public void init() {
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(startPose);
    }

    @Override
    public void start() {
        schedule(sequential(
                follow(follower, toScore()),
                waitMs(500),
                follow(follower, toPark())
        ));
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();
    }
}