package org.firstinspires.ftc.teamcode.pedro.WarmUp_WithoutPedro;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.Another_Codes.UsualFunctions;

import java.util.List;


@Autonomous(name = "azulCimaPARK", group = "Autonomous")
public class AzulBaixoPARK extends LinearOpMode {

    private DcMotor leftFront, leftBack, rightFront, rightBack;

    //MECH

    private DcMotor feeder, midTake;

    private IMU imu;
    private DcMotorEx leftShooter, rightShooter;

    UsualFunctions func = new UsualFunctions();


    public void runOpMode(){



        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        INIT();


        if (opModeIsActive()){

            driveMecanum(0, 0.5, 0);
            sleep(300);
            StopAll();
            driveMecanum(0.5,0,0);
            sleep(300);
            StopAll();


        }




    }

    private void driveMecanum(double strafe, double drive, double turn) {
        double fl = drive + strafe + turn;
        double fr = drive - strafe - turn;
        double bl = drive - strafe + turn;
        double br = drive + strafe - turn;

        double max = Math.max(Math.abs(fl), Math.max(Math.abs(fr), Math.max(Math.abs(bl), Math.abs(br))));
        if (max > 1.0) {
            fl /= max;
            fr /= max;
            bl /= max;
            br /= max;
        }

        leftFront.setPower(fl);
        rightFront.setPower(fr);
        leftBack.setPower(bl);
        rightBack.setPower(br);
    }

    private void INIT(){

        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");
        leftShooter = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");
        rightShooter.setDirection(DcMotor.Direction.REVERSE);

        rightFront.setDirection(DcMotor.Direction.REVERSE);
        rightBack.setDirection(DcMotor.Direction.REVERSE);

        feeder  = hardwareMap.get(DcMotor.class, "feeder");
        midTake = hardwareMap.get(DcMotor.class, "midTake");


        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        midTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);




    }



    private void StopAll(){
        driveMecanum(0, 0, 0);
    }


}
