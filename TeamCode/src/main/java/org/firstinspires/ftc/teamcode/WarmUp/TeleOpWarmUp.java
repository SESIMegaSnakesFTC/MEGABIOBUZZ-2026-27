package org.firstinspires.ftc.teamcode.WarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedro.UsualFunctions;

import java.util.List;


@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class TeleOpWarmUp extends LinearOpMode {



    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor L_shooter, R_shooter, feeder;

    boolean LastRT = false;
    boolean LastLT = false;
    boolean LastA = false;

    //Servos
    private Servo LeftrampServo, RightrampServo, FeederServo;

    //===========================================================================

    //Gate Servo
    private Servo GateServo;


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

            double rfp = (y + x + rx) / Median;
            double rbp = (y - x + rx) / Median;
            double lfp = (y - x - rx) / Median;
            double lbp = (y + x - rx) / Median;

            LeftFront.setPower(rfp);
            LeftBack.setPower(rbp);
            RightFront.setPower(lfp);
            RightFront.setPower(lbp);


            if (RT && !LastRT) {
                LeftrampServo.setPosition(Func.RampClosedPos);
                RightrampServo.setPosition(Func.RampClosedPos);
                FeederServo.setPosition(Func.ClosedPosFeeder);
                L_shooter.setPower(0.8);
                R_shooter.setPower(0.8);
            }


            if (LT && !LastLT) {
                LeftrampServo.setPosition(Func.RampClosedPos);
                RightrampServo.setPosition(Func.RampClosedPos);
                FeederServo.setPosition(Func.ClosedPosFeeder);
                L_shooter.setPower(0.8);
                R_shooter.setPower(0.8);
            } else {
                L_shooter.setPower(0.8);
                R_shooter.setPower(0.8);
            }


            if (gamepad2.right_bumper || gamepad2.left_bumper) {

                FeederServo.setPosition(Func.FeederCatchPos);
                feeder.setPower(0.9);

            } else {
                FeederServo.setPosition(Func.ClosedPosFeeder);
                feeder.setPower(0);
            }


            if (A && !LastA) {
                GateServo.setPosition(Func.GateOpen);
            } else {
                GateServo.setPosition(Func.GateClosed);
            }


            LastRT = RT;
            LastLT = LT;
            LastA = A;


        }
        RightrampServo.setPosition(Func.RampCatchPos);
        LeftrampServo.setPosition(Func.RampClosedPos);
        FeederServo.setPosition(Func.ClosedPosFeeder);
        GateServo.setPosition(Func.GateClosed);

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
        L_shooter = hardwareMap.get(DcMotor.class, "leftShooter");
        R_shooter = hardwareMap.get(DcMotor.class, "rightShooter");
        feeder = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        L_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        R_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        RightrampServo = hardwareMap.get(Servo.class, "rightRampServo");
        RightrampServo.setDirection(Servo.Direction.REVERSE);
        LeftrampServo = hardwareMap.get(Servo.class, "leftRampServo");
        FeederServo = hardwareMap.get(Servo.class, "feederServo");
        GateServo = hardwareMap.get(Servo.class, "gateServo");


        LeftrampServo.setPosition(Func.InitPosFeeder);
        RightrampServo.setPosition(Func.InitPosFeeder);
        GateServo.setPosition(Func.GateClosed);


    }
}