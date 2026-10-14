package org.firstinspires.ftc.teamcode.Autonomo.pedro.Ivy.RedDown;


import static com.pedropathing.api.Paths.*;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.Scheduler.schedule;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;


class RedFlowerPath {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(56.3624, 10.5365, 180);
    private final Pose path1 = poseFactory.of(9.0, 8.5, 180);
    private final Pose point2 = poseFactory.of(56.1, 14, 180);
    private final Pose point3 = poseFactory.of(24, 47, 134.2079);
    private final Pose point4 = poseFactory.of(12, 47, 180);
    private final Pose point5 = poseFactory.of(21, 47, 180);
    private final Pose point6 = poseFactory.of(12.2, 91.8, 180);

    public Path path1() {
        return line(start, path1).constant(path1);
    }

    public Path path2() {
        return line(path1, point2).constant(point2);
    }

    public Path path3() {
        return line(point2, point3).tangent();
    }

    public Path path4() {
        return line(point3, point4).tangent();
    }

    public Path path5() {
        return line(point4, point5).reverseTangent();
    }

    public Path path6() {
        return line(point5, point6).constant(point6);
    }
}

@Disabled
@Autonomous(name = "RedDownTakeGoFlower", group = "Autonomous")
public class RedFlower extends OpMode {

    private Follower follower;
    private final RedFlowerPath paths = new RedFlowerPath();

    @Override
    public void init(){
        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(paths.start);
    }

    @Override
    public void start(){
        schedule(sequential(
                follow(follower, paths.path1()),
                waitMs(350),

                follow(follower, paths.path2()),
                waitMs(2000),

                follow(follower, paths.path3()),
                waitMs(350),

                follow(follower, paths.path4()),
                waitMs(3000),

                follow(follower, paths.path5()),
                waitMs(350),

                follow(follower, paths.path6()),
                waitMs(350)
                ));
    }

    @Override
    public void loop(){
        follower.update();
        Scheduler.execute();
    }
}
