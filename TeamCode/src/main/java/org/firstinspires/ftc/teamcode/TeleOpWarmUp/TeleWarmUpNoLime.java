package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.PIDS.GeralShooterConfig;
import org.firstinspires.ftc.teamcode.UsualFunctions;

import java.util.List;

@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class TeleWarmUpNoLime extends LinearOpMode {

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor feeder, midTake;
    private GeralShooterConfig shooters;
    private double targetShooterVelocity = 1405;

    boolean LastRT = false, RT_ON = false;
    boolean LastX = false, X_ON = false;



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
            boolean X = gamepad2.x;


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


            if (RT && !LastRT ) {

                RT_ON = !RT_ON;
            }


            if (gamepad2.right_bumper) {

                feeder.setPower(0.9);


            } else if (gamepad2.left_bumper){

                feeder.setPower(-0.88);

            }
            else{
                feeder.setPower(0);
            }



                // BUTTONS

            if (X && !LastX){
                X_ON = !X_ON;
            }

            if (X_ON){
                rampServo.setPosition(Func.RampCatchPos);
            }
            else{
                rampServo.setPosition(Func.RampClosedPos);
            }



            if (RT_ON){
                shooters.setTargetVelocity(targetShooterVelocity);
                shooters.update();
                midTake.setPower(-0.99);
            }
            else{
                shooters.stop();
                midTake.setPower(0);
            }

            telemetry.addData("Target Vel", targetShooterVelocity);
            telemetry.addData("Left Shooter Vel", shooters.getLeftVelocity());
            telemetry.addData("Right Shooter Vel", shooters.getRightVelocity());
            telemetry.addData("STATUS RAMP", rampServo.getPosition() == Func.RampCatchPos ? "ABERTO" : "FECHADO");
            telemetry.update();


            LastRT = RT;
            LastX = X;




        }

    }


    public void INIT() {

        LeftBack = hardwareMap.get(DcMotor.class, "leftBack");
        LeftFront = hardwareMap.get(DcMotor.class, "leftFront");
        RightBack = hardwareMap.get(DcMotor.class, "rightBack");
        RightFront = hardwareMap.get(DcMotor.class, "rightFront");

        RightFront.setDirection(DcMotor.Direction.FORWARD);
        RightBack.setDirection(DcMotor.Direction.FORWARD);
        LeftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        LeftFront.setDirection(DcMotorSimple.Direction.REVERSE);

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


        rampServo = hardwareMap.get(Servo.class, "rightRampServo");



    }
}