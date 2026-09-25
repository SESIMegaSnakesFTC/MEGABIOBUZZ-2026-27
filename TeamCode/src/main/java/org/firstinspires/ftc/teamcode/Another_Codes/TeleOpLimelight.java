package org.firstinspires.ftc.teamcode.Another_Codes;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;


@Disabled
@TeleOp(name = "OficialTeleOp", group = "TeleOp")
public class TeleOpLimelight extends LinearOpMode {


    //LIMELIGHT + Turret Servo
    private Servo TurretLimeServo;
    boolean LimelightON = false;
    private double potence = 0;

    private Limelight3A limelight3A;

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor shooter, feeder;
    private Servo LimeServo;

    //BOTÕES
    boolean LastRT = false;
    boolean LastLT = false;
    boolean LastA = false;
    boolean A_ON  = false;
    boolean LastB = false;

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
                LimelightON = !LimelightON;
            }

            if (LimelightON){

                limelight3A.start();
                limelight3A.setPollRateHz(75);
                limelight3A.pipelineSwitch(9); //AprilTag
                LimelightON = true;
            }
            else {
                limelight3A.stop();
                LimelightON = false;
            }


            //Limelight
            if (LimelightON) {

                LLResult result = limelight3A.getLatestResult();
                double tx = result.getTx();
                Func.LimelightTrakingServo(limelight3A, true, LimeServo);
                potence = Func.PotentShot(result);
            }


            if (RT && !LastRT) {
                LeftrampServo.setPosition(Func.RampClosedPos);
                RightrampServo.setPosition(Func.RampClosedPos);
                FeederServo.setPosition(Func.ClosedPosFeeder);
                shooter.setPower(potence);
            }


            if (LT && !LastLT) {
                LeftrampServo.setPosition(Func.RampClosedPos);
                RightrampServo.setPosition(Func.RampClosedPos);
                FeederServo.setPosition(Func.ClosedPosFeeder);
                shooter.setPower(potence);
            } else {
                shooter.setPower(0);
            }


            if (gamepad2.right_bumper || gamepad2.left_bumper) {

                FeederServo.setPosition(Func.FeederCatchPos);
                feeder.setPower(0.9);

            } else {
                FeederServo.setPosition(Func.ClosedPosFeeder);
                feeder.setPower(0);
            }


            if (A && !LastA) {
                A_ON = !A_ON;
            }








            //BOTÕES -> Past
            LastRT = RT;
            LastLT = LT;
            LastB = B;
            LastA = A;


        }
        RightrampServo.setPosition(Func.RampCatchPos);
        LeftrampServo.setPosition(Func.RampClosedPos);
        FeederServo.setPosition(Func.ClosedPosFeeder);


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

        LeftrampServo.setPosition(Func.InitPosFeeder);
        RightrampServo.setPosition(Func.InitPosFeeder);
        TurretLimeServo.setPosition(Func.LimeInitPos);


        LimeServo = hardwareMap.get(Servo.class, "LimeServo");

        //CAM

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
    }
}