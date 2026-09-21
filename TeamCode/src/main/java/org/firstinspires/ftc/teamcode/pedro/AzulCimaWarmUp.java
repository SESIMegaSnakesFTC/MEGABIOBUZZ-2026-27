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
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.UsualFunctions;

import java.util.List;


class PathAzulCimaWarmUp {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(79.9155, 131.9257, 90);
    private final Pose path1 = poseFactory.of(97.1274, 130.5749, -90);
    private final Pose point2 = poseFactory.of(130.7199, 132.4773, 3);
    private final Pose point3 = poseFactory.of(80.4776, 128.4641, 3);
    private final Pose point4 = poseFactory.of(80.3528, 129.4309, -88);
    private final Pose point5 = poseFactory.of(125.8521, 104.9699, -90);
    private final Pose point6 = poseFactory.of(132.7561, 41.5551, -90);

    public Path path1() {
        return line(start, path1).constant(path1);
    }

    public Path path2() {
        return line(path1, point2).constant(point2);
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
}




@Disabled
@Autonomous(name = "AzulCimaWarmUp", group = "Autonomous")
public class AzulCimaWarmUp extends LinearOpMode {

    private DcMotor L_Shooter = null, R_Shooter = null, midTake = null;
    private DcMotor feeder;
    private Servo RampServo= null;
    private Follower follower;
    private enum CurrentActions{
        INITIAL_SHOOT, SHOOT, FEED_OPEN
    }
    private CurrentActions AutoAction = CurrentActions.INITIAL_SHOOT;
    private final PathAzulCimaWarmUp pathAzulCimaWarmUp = new PathAzulCimaWarmUp();



    //IMPORT's
    private UsualFunctions functions  = new UsualFunctions();


    public void runOpMode(){

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        follower = constant.create(hardwareMap);
        follower.setPose(pathAzulCimaWarmUp.start);

        //RAMPA FECHADA

        RampServo.setPosition(functions.RampClosedPos);

        waitForStart();

        if (isStopRequested()){
            return;
        }


        seguirPath(pathAzulCimaWarmUp.path1());
        seguirPath(pathAzulCimaWarmUp.path2());
        seguirPath(pathAzulCimaWarmUp.path3());
        seguirPath(pathAzulCimaWarmUp.path4());
        seguirPath(pathAzulCimaWarmUp.path5());
        seguirPath(pathAzulCimaWarmUp.path6()); //ESTACIONAR

    }

    private void INIT(){

        L_Shooter = hardwareMap.get(DcMotor.class, "leftShooter");
        L_Shooter.setDirection(DcMotor.Direction.REVERSE);
        R_Shooter = hardwareMap.get(DcMotor.class, "rightShooter");
        midTake   = hardwareMap.get(DcMotor.class, "midTake");
        midTake.setDirection(DcMotor.Direction.REVERSE);
        feeder    = hardwareMap.get(DcMotor.class, "feeder");
        RampServo = hardwareMap.get(Servo.class, "rampServo");

        L_Shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        R_Shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        midTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    private void seguirPath(Path path){

        follower.follow(path);
        ElapsedTime timer = new ElapsedTime();

        while(opModeIsActive() && follower.isBusy() && timer.seconds() < 5 ){

            follower.update();
        }
    }

    private void DoAction(CurrentActions act){

        switch (act){

            case INITIAL_SHOOT:

                sleep(4000);
                L_Shooter.setPower(0.66);
                R_Shooter.setPower(0.66);
                sleep(400);
                midTake.setPower(0.7);
                sleep(3600);

                break;

            case SHOOT:

                L_Shooter.setPower(0.66);
                R_Shooter.setPower(0.66);
                sleep(400);
                midTake.setPower(0.7);
                sleep(3600);

                break;

            case FEED_OPEN:

                feeder.setPower(1);
                sleep(2700);
                break;
        }
    }
}
