package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.PIDS.GeralShooterConfig;

@Disabled
@TeleOp(name = "testTwoShooters", group = "TeleOp")
public class testTwoShootersPIDF extends LinearOpMode {

    private DcMotor MidTake, feeder;
    private Servo rampServo;
    private GeralShooterConfig shooters;

    boolean LastY = false, LastX = false;
    boolean STATUSA = false, STATUSX = false;
    double potencia = 0.50;

    private static final double TARGET_TICKS_PER_SEC = 1980;

    private UsualFunctions Func = new UsualFunctions();

    public void runOpMode(){

        MidTake   = hardwareMap.get(DcMotor.class, "midtake");
        feeder    = hardwareMap.get(DcMotor.class, "feeder");
        rampServo = hardwareMap.get(Servo.class, "rampServo");

        shooters = new GeralShooterConfig(hardwareMap);

        rampServo.setPosition(Func.RampClosedPos);
        MidTake.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();

        while (opModeIsActive()){

            boolean A  = gamepad2.a;
            boolean Y  = gamepad2.y;
            boolean LT = gamepad2.left_trigger > 0.5;
            boolean X  = gamepad2.x;
            boolean RB  = gamepad2.right_bumper;
            boolean LB = gamepad2.left_bumper;



            if (X && !LastX){
                STATUSX = !STATUSX;
            }
            if (Y && !LastY){

            }
            if (RB){
                potencia += 0.01;
            }
            if (LB){
                potencia -= 0.01;
            }





            if (STATUSX){

                rampServo.setPosition(Func.RampCatchPos);
            }
            else{
                rampServo.setPosition(Func.RampClosedPos);
            }


            LastY = Y;
            LastX = X;

            rampServo.setPosition(STATUSX ? Func.RampCatchPos : Func.RampClosedPos);

            if (STATUSA){
                shooters.setTargetVelocity(TARGET_TICKS_PER_SEC);
                shooters.update();
            } else {
                shooters.stop();
            }

            LastY = Y;
            LastX = X;

            telemetry.addData("potencia", potencia);
            telemetry.addData("leftVel", shooters.getLeftVelocity());
            telemetry.addData("rightVel", shooters.getRightVelocity());
            telemetry.update();
        }
    }
}