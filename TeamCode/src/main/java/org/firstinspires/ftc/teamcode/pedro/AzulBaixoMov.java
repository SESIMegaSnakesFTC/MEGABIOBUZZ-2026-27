package org.firstinspires.ftc.teamcode.pedro;

import static com.pedropathing.api.Paths.*;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;


class PathAzulBaixoMov {



    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(81.2894, 9.2554, 90);
    private final Pose point1 = poseFactory.of(94.4097, 31.2894, 0);
    private final Pose point2 = poseFactory.of(94.0896, 10.8585, -90);
    private final Pose point3 = poseFactory.of(93.8566, 16.783, -90);
    private final Pose point4 = poseFactory.of(81.1812, 16.7522, 0);
    private final Pose point5 = poseFactory.of(131.2452, 23.7759, 0);

    public Path path1() {
        return line(start, point1).constant(point1);
    }

    public Path path2() {
        return line(point1, point2).constant(point2);
    }

    public Path path3() {
        return line(point2, point3).constant(point3);
    }

    public Path path4() {
        return line(point3, point4).constant(point4);
    }

    public Path path5() {
        return line(point4, point5).constant(point5);
    }
}

@Disabled
@Autonomous(name = "AzulBaixo", group = "Auto")
public class AzulBaixoMov extends LinearOpMode {

    private Follower follower;
    private final PathAzulBaixoMov PathZulBaixoMov = new PathAzulBaixoMov();

    @Override
    public void runOpMode(){

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        follower = constant.create(hardwareMap);
        follower.setPose(PathZulBaixoMov.start);

        waitForStart();

        if (isStopRequested()) {return;}

        seguirPath(PathZulBaixoMov.path1());
        seguirPath(PathZulBaixoMov.path2());
        seguirPath(PathZulBaixoMov.path3());
        seguirPath(PathZulBaixoMov.path4());
        seguirPath(PathZulBaixoMov.path5());

    }

    private void seguirPath(Path path){
        follower.follow(path);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && follower.isBusy() && timer.seconds() < 5){

            follower.update();
        }
    }
}
