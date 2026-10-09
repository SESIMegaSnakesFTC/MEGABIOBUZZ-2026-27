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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretConfig;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.midTake;
import org.firstinspires.ftc.teamcode.pedro.Constants;




class SoloRedDownMechPath {


    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(56, 9, 180);
    private final Pose path1Start = poseFactory.of(56, 9, 180);
    private final Pose path1 = poseFactory.of(11.6, 9, 180);
    private final Pose point2 = poseFactory.of(57.0845, 128.4008, 90);
    private final Pose point2Control1 = poseFactory.of(23.4091, 27.2561, 0);
    private final Pose point2Control2 = poseFactory.of(26.694, 121.6754, 0);
    private final Pose point3 = poseFactory.of(8.8137, 46.6729, 180);
    private final Pose point3Control1 = poseFactory.of(23.9949, 111.4923, 0);
    private final Pose point3Control2 = poseFactory.of(32.2113, 38.9763, 0);
    private final Pose point4 = poseFactory.of(57.0327, 14.5948, 180);
    private final Pose point5 = poseFactory.of(6.8726, 95.299, 180);
    private final Pose point5Control1 = poseFactory.of(8.3502, 38.242, 0);

    public Path path1() {
        return line(path1Start, path1).linear(path1Start, path1);
    }

    public Path path2() {
        return curve(path1, point2Control1, point2Control2, point2).linear(path1, point2);
    }

    public Path path3() {
        return curve(point2, point3Control1, point3Control2, point3).linear(point2, point3);
    }

    public Path path4() {
        return line(point3, point4).constant(point4);
    }

    public Path path5() {
        return curve(point4, point5Control1, point5).constant(point5);
    }

}


@Autonomous(name = "SoloRedDownMech", group = "Autonomous")
public class SoloRedDownMech extends OpMode {


    //MECH -> ta na ordem do trajeto do artefato

    private TurretController config = new TurretController(hardwareMap.get(DcMotorEx.class, "shooter"));
    private TurretTracking tracker = new TurretTracking(config);
    private Feeder feeder = new Feeder();
    private midTake midTake = new midTake();






    private Follower follower;
    private SoloRedDownMechPath paths = new SoloRedDownMechPath();

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
                waitMs(670),
                follow(follower, paths.path2()),
                waitMs(4000),
                follow(follower, paths.path3()),
                waitMs(3000),
                follow(follower, paths.path4()),
                waitMs(1800),
                follow(follower, paths.path5())
        ));

    }

    @Override
    public void loop(){
        follower.update();
        Scheduler.execute();
    }
}

