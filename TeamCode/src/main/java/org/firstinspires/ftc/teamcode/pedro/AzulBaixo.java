package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


import static com.pedropathing.api.Paths.*;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


import java.util.List;

class PathAzulBaixo {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(79.6277, 9.2763, 90);
    private final Pose path1 = poseFactory.of(94.4097, 35.4565, 0);
    private final Pose point2 = poseFactory.of(94.4565, 11.4302, -90);
    private final Pose point3 = poseFactory.of(94.6498, 21.0538, 0);
    private final Pose point4 = poseFactory.of(80.8399, 21.2324, 0);
    private final Pose point5 = poseFactory.of(131.5749, 25.9808, 0);

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
}


@Disabled
public class AzulBaixo extends LinearOpMode {

    enum Functions {SECOND_SHOOT, FLOWER_POLLEN, PARK, NO_ONE}
    Functions action = Functions.SECOND_SHOOT;
    private Follower follower;
    private PathAzulBaixo PathZulBaixo;
    

    //Mech

    Limelight3A limelight3A;

    //Servos
    private Servo LeftrampServo, RightrampServo, FeederServo, TurretLimeServo, GateServo;
    private DcMotor Shooter, Feeder;

    //======================================================================

    //INSTÂNCIA PARA IMPORT'S(Bastante)

    UsualFunctions Func = new UsualFunctions();


    public void runOpMode(){

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }



        follower = constant.create(hardwareMap);
        follower.setPose(PathZulBaixo.start);

        waitForStart();

        if(isStopRequested()){
            return;
        }

        action = Functions.NO_ONE;
        seguirPath(PathZulBaixo.path1(),action);
        seguirPath(PathZulBaixo.path2(),action);
        seguirPath(PathZulBaixo.path3(),action);
        seguirPath(PathZulBaixo.path4(),action);
        seguirPath(PathZulBaixo.path5(),action);


        while (opModeIsActive() && follower.isBusy()){

            LLResult LimeResult = limelight3A.getLatestResult();
            double tx = LimeResult.getTx();
            follower.update();


        }
        GateServo.setPosition(Func.GateClosed);
        TurretLimeServo.setPosition(Func.LimeInitPos);


    }

    public void seguirPath(Path path, Functions act) {
        follower.follow(path);

        switch (act){

            case FLOWER_POLLEN:



                break;

            case SECOND_SHOOT:


                break;

            case PARK:

                break;

            case NO_ONE:

                break;

        }
    }
}
