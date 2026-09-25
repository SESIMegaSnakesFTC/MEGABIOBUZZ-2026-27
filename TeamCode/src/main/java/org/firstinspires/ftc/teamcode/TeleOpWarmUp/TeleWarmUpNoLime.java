package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.teamcode.Another_Codes.UsualFunctions;

import java.util.List;


@Disabled
@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class TeleWarmUpNoLime extends LinearOpMode {

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    boolean LastX = false, X_ON = false;
    boolean LastRT = false, RT_ON = false;
    private DcMotor LeftShooter, RightShooter, feeder;


    //Servos
    private Servo rampServo;

    //===========================================================================


    //INSTÂNCIA PARA IMPORT'S(Bastante)

    private final UsualFunctions Func = new UsualFunctions();


    @Override
    public void runOpMode() {

        INIT();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }



        waitForStart();

        while (opModeIsActive()) {


            boolean X = gamepad2.x;
            boolean RT = gamepad2.right_trigger > 0.5;

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

            if (gamepad2.right_bumper){

                feeder.setPower(0.9);
            }
            else{
                feeder.setPower(0);
            }

            if (gamepad2.left_bumper){
                feeder.setPower(-0.9);
            }
            else{
                feeder.setPower(0);
            }

            if (RT && !LastRT){

                RT_ON = !RT_ON;
            }

            if (RT_ON){
                RightShooter.setPower(0.7);
                LeftShooter.setPower(0.7);
            }
            else{
                LeftShooter.setPower(0);
                RightShooter.setPower(0);
            }



            telemetry.addData("STATUS RAMP", rampServo.getPosition() == Func.RampCatchPos ? "ABERTO" : "FECHADO");
            telemetry.update();


            LastX = X;
            LastRT = RT;



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


        rampServo = hardwareMap.get(Servo.class, "rightRampServo");
        LeftShooter = hardwareMap.get(DcMotor.class, "leftShooter");
        RightShooter = hardwareMap.get(DcMotor.class, "rightShooter");
        feeder = hardwareMap.get(DcMotor.class, "feeder");
        RightShooter.setDirection(DcMotor.Direction.REVERSE);


        RightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);





    }
}