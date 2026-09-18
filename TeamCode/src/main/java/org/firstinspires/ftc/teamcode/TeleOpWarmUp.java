package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedro.UsualFunctions;

import java.util.List;


@Disabled
@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class TeleOpWarmUp extends LinearOpMode {



    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor L_shooter, R_shooter, feeder, midTake;

    boolean LastRT = false;
    boolean LastLT = false;
    boolean LastA = false;

    //Servos
    private Servo rampServo;

    //===========================================================================


    //INSTÂNCIA PARA IMPORT'S(Bastante)

    UsualFunctions Func = new UsualFunctions();



    @Override
    public void runOpMode() {

        INIT();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        waitForStart();

        while (opModeIsActive()) {


            boolean RT = gamepad2.right_trigger > 0.5;
            boolean LT = gamepad2.left_trigger > 0.5;
            boolean A = gamepad2.a;


            double x = gamepad1.left_stick_x * 1.09;
            double y = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;


            double Median = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 0.9);

            double lfp = (y + x + rx) / Median;
            double lbp = (y - x + rx) / Median;
            double rfp = (y - x - rx) / Median;
            double rbp = (y + x - rx) / Median;

            LeftFront.setPower(lfp);
            LeftBack.setPower(lbp);
            RightFront.setPower(rfp);
            RightBack.setPower(rbp);


            if (RT && !LastRT) {

                rampServo.setPosition(Func.RampClosedPos);
                L_shooter.setPower(0.8);
                R_shooter.setPower(0.8);
                sleep(375);
                midTake.setPower(1);
            }


            if (LT && !LastLT) {

                rampServo.setPosition(Func.RampClosedPos);
                L_shooter.setPower(0.8);
                R_shooter.setPower(0.8);
                midTake.setPower(1);
            } else {
                L_shooter.setPower(0);
                R_shooter.setPower(0);
                midTake.setPower(0);
            }


            if (gamepad2.right_bumper || gamepad2.left_bumper) {

                feeder.setPower(0.9);
                midTake.setPower(0.2);

            } else {

                feeder.setPower(0);
                midTake.setPower(0);
            }

            if (A && !LastA){
                rampServo.setPosition(Func.RampCatchPos);
            }
            else {
                rampServo.setPosition(Func.RampClosedPos);
            }

            LastRT = RT;
            LastLT = LT;
            LastA = A;


        }
        rampServo.setPosition(Func.RampClosedPos);
    }


    public void INIT() {

        LeftBack = hardwareMap.get(DcMotor.class, "leftBack");
        LeftFront = hardwareMap.get(DcMotor.class, "leftFront");
        RightBack = hardwareMap.get(DcMotor.class, "rightBack");
        RightFront = hardwareMap.get(DcMotor.class, "rightFront");

        RightFront.setDirection(DcMotor.Direction.REVERSE);
        RightBack.setDirection(DcMotor.Direction.REVERSE);
        LeftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        LeftFront.setDirection(DcMotorSimple.Direction.FORWARD);

        RightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Mech
        midTake = hardwareMap.get(DcMotor.class, "midTake");
        L_shooter = hardwareMap.get(DcMotor.class, "leftShooter");
        R_shooter = hardwareMap.get(DcMotor.class, "rightShooter");
        midTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        L_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        R_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);


        rampServo = hardwareMap.get(Servo.class, "rightRampServo");
        rampServo.setDirection(Servo.Direction.REVERSE);

        rampServo.setPosition(Func.RampClosedPos);


    }
}