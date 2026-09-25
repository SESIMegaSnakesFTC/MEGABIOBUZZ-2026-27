package org.firstinspires.ftc.teamcode.pedro.Others;

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

import org.firstinspires.ftc.teamcode.pedro.constant;

import java.util.List;


class pathAzulCima {

    private final PoseFactory poseFactory = PoseFactory.degrees();


    public final Pose start = poseFactory.of(85.4565, 133.7247, 0);
    private final Pose point1 = poseFactory.of(132.2759, 133.6549, 0);
    private final Pose point2 = poseFactory.of(85.9411, 133.6498, 0);
    private final Pose point3 = poseFactory.of(121.2497, 133.662, 0);
    private final Pose point4 = poseFactory.of(121.6191, 94.6146, 0);
    private final Pose point5 = poseFactory.of(130.8688, 94.6146, 0);
    private final Pose point6 = poseFactory.of(130.8688, 94.4334, 0);
    private final Pose point7 = poseFactory.of(126.6946, 94.6216, 0);
    private final Pose point8 = poseFactory.of(129.1837, 47.2446, 93.0075);

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

    public Path path6() {
        return line(point5, point6).constant(point6);
    }

    public Path path7() {
        return line(point6, point7).constant(point7);
    }

    public Path path8() {
        return line(point7, point8).constant(point8);
    }
}


@Disabled
@Autonomous(name = "AzulCima", group = "TeleOp")
public class AzulCimaMov extends LinearOpMode {

    private Follower follower;
    private final pathAzulCima pathZulCimaMov = new pathAzulCima();

    @Override
    public void runOpMode() {

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        follower = constant.create(hardwareMap);
        follower.setPose(pathZulCimaMov.start);

        waitForStart();

        if (isStopRequested()) {
            return;
        }


        seguirPath(pathZulCimaMov.path1());
        seguirPath(pathZulCimaMov.path2());
        seguirPath(pathZulCimaMov.path3());
        seguirPath(pathZulCimaMov.path4());
        seguirPath(pathZulCimaMov.path5());
        seguirPath(pathZulCimaMov.path6());
        seguirPath(pathZulCimaMov.path7());
        seguirPath(pathZulCimaMov.path8());
    }



    private void seguirPath(Path path) {
        follower.follow(path);
        ElapsedTime timer = new ElapsedTime();
        while (opModeIsActive() && follower.isBusy() && timer.seconds() < 5) {
            follower.update();
            
        }
    }
}