package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import java.util.List;
import static com.pedropathing.api.Paths.*;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.UsualFunctions;


class PathAzulBaixoWarmUp {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(80.5336, 9.4575, 90);
    private final Pose path1 = poseFactory.of(94.4097, 35.4565, 59);
    private final Pose point2 = poseFactory.of(94.2145, 10.0749, -90);
    private final Pose point3 = poseFactory.of(94.3886, 18.1287, -91.2386);
    private final Pose point4 = poseFactory.of(78.3931, 15.2138, 85);
    private final Pose point5 = poseFactory.of(131.4027, 28.1197, -166.3168);

    public Path path1() {
        return line(start, path1).constant(path1);
    }

    public Path path2() {
        return line(path1, point2).constant(point2);
    }

    public Path path3() {
        return line(point2, point3).reverseTangent();
    }

    public Path path4() {
        return line(point3, point4).constant(point4);
    }

    public Path path5() {
        return line(point4, point5).reverseTangent();
    }
}

@Disabled
@Autonomous(name = "AzulBaixoWarmUP", group = "Autonomous")
public class AuzlBaixoWarmUp extends LinearOpMode {

    //ACTIONS

    private enum CurrentAction  { INIT_SHOOT, SHOOT, FEED_OPEN, FEED_CLOSE}
    private CurrentAction AutoAction = CurrentAction.INIT_SHOOT;

    private Follower follower;
    private final PathAzulBaixoWarmUp pathAzulBaixoWarmUp = new PathAzulBaixoWarmUp();

    //MECH

    private DcMotor L_shooter = null, R_shooter = null, feeder = null, MidTakeMech = null;
    private Servo RampServo = null;

            // IMPORT's
    private UsualFunctions Func = new UsualFunctions();



    public void runOpMode(){

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        initHardware();

        follower = constant.create(hardwareMap);
        follower.setPose(pathAzulBaixoWarmUp.start);

        //>>>RAMPA FECHADA<<<

        RampServo.setPosition(Func.RampClosedPos);

        waitForStart();

        if (isStopRequested()){
            return;
        }

        DoAction(AutoAction);
        seguirPath(pathAzulBaixoWarmUp.path1());

        seguirPath(pathAzulBaixoWarmUp.path2());
        AutoAction = CurrentAction.FEED_OPEN;
        DoAction(AutoAction);
        AutoAction = CurrentAction.FEED_CLOSE;
        DoAction(AutoAction);

        seguirPath(pathAzulBaixoWarmUp.path3());


        seguirPath(pathAzulBaixoWarmUp.path4());
        AutoAction = CurrentAction.SHOOT;
        DoAction(AutoAction);

        seguirPath(pathAzulBaixoWarmUp.path5());

    }

    private void seguirPath(Path path){

        follower.follow(path);
        ElapsedTime timer = new ElapsedTime();

        while (opModeInInit() && follower.isBusy() && timer.seconds() < 5){
            follower.update();
        }
    }


    private void initHardware(){

        feeder      = hardwareMap.get(DcMotor.class, "feeder" );
        L_shooter   = hardwareMap.get(DcMotor.class, "leftShooter");
        L_shooter.setDirection(DcMotor.Direction.REVERSE);
        R_shooter   = hardwareMap.get(DcMotor.class, "rightShooter");
        MidTakeMech = hardwareMap.get(DcMotor.class, "midTake");
        RampServo = hardwareMap.get(Servo.class, "rampServo");
    }

    private void DoAction(CurrentAction currentAction){

        switch (currentAction){

            case INIT_SHOOT:

                sleep(4000);
                L_shooter.setPower(0.66);
                R_shooter.setPower(0.66);
                sleep(400);
                MidTakeMech.setPower(1);
                sleep(3600);

                break;

            case SHOOT:

                L_shooter.setPower(0.66);
                R_shooter.setPower(0.66);
                sleep(400);
                MidTakeMech.setPower(1);
                sleep(3600);

                break;

            case FEED_OPEN:

                feeder.setPower(1.0);
                sleep(2700);
                break;

            case FEED_CLOSE:
                feeder.setPower(0);
                break;
        }
    }
    private void OpenClose(String OpenOrClose){ RampServo.setPosition(OpenOrClose == "OPEN" ? Func.RampCatchPos : Func.ClosedPosFeeder );
    }
}
