package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedro.UsualFunctions;

import java.util.List;


@TeleOp(name = "TeleOpLimelight", group = "TeleOp")
public class TeleOpLimelight extends LinearOpMode {


    //LIMELIGHT

    private Limelight3A limelight3A;

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor shooter, feeder;

    boolean LastRT = false;
    boolean LastLT = false;
    boolean LastA = false;
    boolean LastB = false;

    //Servos
    private Servo LeftrampServo, RightrampServo, FeederServo;

    //Feeder Servo
    private final float InitPosFeeder = -0.50f, ClosedPosFeeder = 0.30f, FeederCatchPos = 0.20f; //MUDAR DEPOIS


    //===========================================================================

    //Limelight + Turret Servo
    private Servo TurretLimeServo;
    boolean LimelightON = false;
    private final float InitPosLime = 0f; //AJUSTAR DEPOIS -> TESTE
    private double potence = 0;
    //===========================================================================

    //Ramp Servo
    private final float RampCatchPos = 0.3f, RampClosedPos = 0.0f;

    //Gate Servo
    private Servo GateServo;
    private final float GateOpen = 0.5f, GateClosed = 0f; //AJUSTAR


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
            boolean B = gamepad2.b;

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


            //TURN ON && OFF LIMELIGHT
            if (B && !LastB) {


                limelight3A.start();
                limelight3A.setPollRateHz(75);
                limelight3A.pipelineSwitch(9); //AprilTag
                LimelightON = true;

            }


            //Limelight
            if (LimelightON) {

                LLResult result = limelight3A.getLatestResult();
                double tx = result.getTx();
                Func.LimelightTrakingServo(result, tx);
                potence = Func.PotentShot(result);

            } else {
                limelight3A.stop();
                LimelightON = false;
            }


            if (RT && !LastRT) {
                LeftrampServo.setPosition(RampClosedPos);
                RightrampServo.setPosition(RampClosedPos);
                FeederServo.setPosition(ClosedPosFeeder);
                shooter.setPower(potence);
            }


            if (LT && !LastLT) {
                LeftrampServo.setPosition(RampClosedPos);
                RightrampServo.setPosition(RampClosedPos);
                FeederServo.setPosition(ClosedPosFeeder);
                shooter.setPower(potence);
            } else {
                shooter.setPower(0);
            }


            if (gamepad2.right_bumper || gamepad2.left_bumper) {

                FeederServo.setPosition(FeederCatchPos);
                feeder.setPower(0.9);

            } else {
                FeederServo.setPosition(ClosedPosFeeder);
                feeder.setPower(0);
            }


            if (A && !LastA) {
                GateServo.setPosition(GateOpen);
            } else {
                GateServo.setPosition(GateClosed);
            }


            LastRT = RT;
            LastLT = LT;
            LastB = B;
            LastA = A;


        }
        RightrampServo.setPosition(RampCatchPos);
        LeftrampServo.setPosition(RampClosedPos);
        FeederServo.setPosition(ClosedPosFeeder);
        GateServo.setPosition(GateClosed);

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
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        feeder = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        RightrampServo = hardwareMap.get(Servo.class, "rightRampServo");
        RightrampServo.setDirection(Servo.Direction.REVERSE);
        LeftrampServo = hardwareMap.get(Servo.class, "leftRampServo");
        FeederServo = hardwareMap.get(Servo.class, "feederServo");
        TurretLimeServo = hardwareMap.get(Servo.class, "limeServo");
        GateServo = hardwareMap.get(Servo.class, "gateServo");

        //FEEDER PARA DENTRO && LIMELIGHT FORWARD

        LeftrampServo.setPosition(InitPosFeeder);
        RightrampServo.setPosition(InitPosFeeder);
        TurretLimeServo.setPosition(InitPosLime);
        GateServo.setPosition(GateClosed);

        //CAM

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
    }
}