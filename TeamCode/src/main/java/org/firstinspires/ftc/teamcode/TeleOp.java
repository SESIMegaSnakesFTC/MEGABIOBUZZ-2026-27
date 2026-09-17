package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;


@Disabled
@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "TeleOpBase", group = "TeleOp")
public class TeleOp extends LinearOpMode {


    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //Mech

    private DcMotor spindexer, L_shooter, R_shooter;

    boolean LastRT = false;
    boolean LastLT = false;

    //Servos
    private Servo rampServo, FeederServo, TurretServo;

        //Feeder Servo
    float InitPos = -0.50f, ClosedPos = 0.30f, FeederCatchPos = 0.20f; //MUDAR DEPOIS

        //Ramp Servo
    float RampCatchPos = 0.3f, RampClosedPos = 0.0f;

        //Turret Servo
    float Minpos = -0.40f, MaxPos = 0.40f; //MUDAR DEPOIS



    @Override
    public void runOpMode(){

        INIT();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        waitForStart();

        while(opModeIsActive()){


            boolean RT = gamepad2.right_trigger > 0.5;
            boolean LT = gamepad2.left_trigger > 0.5;

            double x = gamepad1.left_stick_x * 1.09;
            double y = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;


            double Median = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 0.9);

            double rfp = ( y + x + rx) / Median;
            double rbp = ( y - x + rx) / Median;
            double lfp = ( y - x - rx) / Median;
            double lbp = ( y + x - rx) / Median;

            LeftFront.setPower(rfp);LeftBack.setPower(rbp);
            RightFront.setPower(lfp);RightFront.setPower(lbp);

            if (RT && !LastRT){
                spindexer.setPower(1);
            }
            if (LT && !LastLT){
                spindexer.setPower(-1);
            }

            LastLT = LT; LastRT = RT;

            if (gamepad1.right_bumper){
                rampServo.setPosition(RampCatchPos);
                FeederServo.setPosition(FeederCatchPos);
                L_shooter.setPower(1.0);R_shooter.setPower(1.0);
            }
        }

        rampServo.setPosition(RampClosedPos);
        FeederServo.setPosition(ClosedPos);


    }


    public void INIT(){

        LeftBack = hardwareMap.get(DcMotor.class, "leftBack");
        LeftFront = hardwareMap.get(DcMotor.class, "leftFront");
        RightBack = hardwareMap.get(DcMotor.class, "rightBack");
        RightFront = hardwareMap.get(DcMotor.class,"rightFront");

        RightFront.setDirection(DcMotor.Direction.REVERSE);
        RightBack.setDirection(DcMotor.Direction.REVERSE);
        LeftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        LeftFront.setDirection(DcMotorSimple.Direction.FORWARD);

        RightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Mech

        spindexer = hardwareMap.get(DcMotor.class, "spindexer");
        L_shooter = hardwareMap.get(DcMotor.class, "LeftShooter");
        R_shooter = hardwareMap.get(DcMotor.class, "RightShooter");

        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        L_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        R_shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        rampServo   = hardwareMap.get(Servo.class, "rampServo");
        FeederServo = hardwareMap.get(Servo.class, "feederServo");
        TurretServo = hardwareMap.get(Servo.class, "turretServo");


    }

}
