package org.firstinspires.ftc.teamcode.pedro.Others.Ivy.RedDown;


import static com.pedropathing.api.Paths.*;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import org.firstinspires.ftc.teamcode.pedro.Constants;




class PathsRedDown {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(57, 8, 90);
    private final Pose path1 = poseFactory.of(57, 26.5, 90);
    private final Pose point2 = poseFactory.of(15.3095, 59.1003, 90);
    private final Pose point2Control1 = poseFactory.of(16.4699, 27.3406, 0);
    private final Pose point3 = poseFactory.of(11.8672, 98.7782, 90);

    public Path path1() {
        return line(start, path1).linear(start, path1);
    }

    public Path path2() {
        return curve(path1, point2Control1, point2).constant(point2);
    }

    public Path path3() {
        return line(point2, point3).constant(point3);
    }
}

@Autonomous(name = "RedDown", group = "Autonomous")
public class RedDown extends OpMode {

    private Follower follower;
    private final PathsRedDown paths = new PathsRedDown();

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

                waitMs(600),

                follow(follower, paths.path2()),
                follow(follower, paths.path3())
        ));
    }

    @Override
    public void loop(){
        follower.update();
        Scheduler.execute();
    }


}
