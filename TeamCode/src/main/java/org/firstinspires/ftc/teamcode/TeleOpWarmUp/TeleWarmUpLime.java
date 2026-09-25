package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.PIDS.GeralShooterConfig;
import org.firstinspires.ftc.teamcode.Another_Codes.UsualFunctions;

import java.util.List;

@TeleOp(name = "TeleWarmUpLimelight", group = "TeleOp")
public class TeleWarmUpLime extends LinearOpMode {

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor feeder, midTake;
    private GeralShooterConfig shooters;
    private double targetShooterVelocity = 1585;
    private Limelight3A limelight3A;
    private IMU imu;

    boolean LastRT = false, RT_ON = false;
    boolean LastX = false, X_ON = false;
    boolean LastB = false, B_ON = false;

    private final float RampClosedPos = 0.59f, RampOpenPos = 0.84f;

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


            boolean RT  = gamepad2.right_trigger > 0.5;
            boolean X   = gamepad2.x;
            boolean B   = gamepad2.b;
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



            if (B_ON){
                limelight3A.start();
                limelight3A.setPollRateHz(75);
                limelight3A.pipelineSwitch(9);

                LLResult result = limelight3A.getLatestResult();


                if ( A && (result != null && result.isValid())){

                    Func.PID_Spin_WithoutServo(imu.getRobotYawPitchRollAngles().getYaw()
                                    + result.getTx(),
                            LeftFront, LeftBack, RightFront, RightBack, imu);

                }
            }
            else{
                limelight3A.stop();
            }



            if (RT && !LastRT) {

              RT_ON = !RT_ON;
            }


            if (gamepad2.right_bumper ) {

                feeder.setPower(0.9);

            }else if (gamepad2.left_bumper){

                feeder.setPower(-0.9);
            }
            else {

                feeder.setPower(0);

            }

            if (X && !LastX){
                X_ON = !X_ON;
            }

            if (B && !LastB ){
                B_ON = !B_ON;
            }

            if (X_ON){

                rampServo.setPosition(RampOpenPos);
            }
            else{
                rampServo.setPosition(RampClosedPos);
            }



            if (RT_ON){
                shooters.setTargetVelocity(targetShooterVelocity);
                shooters.update();
                midTake.setPower(0.8);
            } else {
                shooters.stop();
                midTake.setPower(0);
            }

            telemetry.addData("Shooter Status", RT_ON ? "LIGADO (PIDF)" : "DESLIGADO");
            telemetry.addData("Target Vel", targetShooterVelocity);
            telemetry.addData("Left Shooter Vel", shooters.getLeftVelocity());
            telemetry.addData("Right Shooter Vel", shooters.getRightVelocity());
            telemetry.update();



            LastRT = RT;
            LastX = X;
            LastB = B;


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
        shooters = new GeralShooterConfig(hardwareMap);
        midTake = hardwareMap.get(DcMotor.class, "midTake");
        midTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");


        rampServo = hardwareMap.get(Servo.class, "rightRampServo");
        rampServo.setDirection(Servo.Direction.REVERSE);

        rampServo.setPosition(Func.RampClosedPos);

        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
        );

        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();
    }
}