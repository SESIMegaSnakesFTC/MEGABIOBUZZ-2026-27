package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;


@TeleOp(name = "testTwoShooters", group = "TeleOp")
public class testTwoShooters extends LinearOpMode {
    private DcMotor MidTake, feeder, L_shooter, R_shooter;
    private Servo rampServo;

    boolean LastY = false;
    boolean LastA = false;
    boolean LastX = false;
    boolean STATUSA = false;
    boolean STATUSY = false;
    boolean STATUSX = false;
    double potencia = 0.50;


    private UsualFunctions Func = new UsualFunctions();


    public void runOpMode(){

        L_shooter  = hardwareMap.get(DcMotor.class, "leftShooter");
        R_shooter  = hardwareMap.get(DcMotor.class, "rightShooter");
        R_shooter.setDirection(DcMotor.Direction.REVERSE);
        MidTake    = hardwareMap.get(DcMotor.class, "midtake");
        feeder       = hardwareMap.get(DcMotor.class, "feeder");
        rampServo    = hardwareMap.get(Servo.class, "rampServo");
        rampServo.setPosition(Func.RampClosedPos);
        MidTake.setDirection(DcMotor.Direction.REVERSE);




        waitForStart();
        while (opModeIsActive()){



            boolean A  = gamepad2.a;
            boolean Y  = gamepad2.y;
            boolean LT = gamepad2.left_trigger > 0.5;
            boolean X  = gamepad2.x;
            boolean B  = gamepad2.b;



            if (A && !LastA){
                STATUSA = !STATUSA;

            }
            if (X && !LastX){
                STATUSX = !STATUSX;
            }
            if (Y && !LastY){
                STATUSY = !STATUSY;
            }
            if (B){
                potencia += 0.05;
            }



            if (STATUSY){
                MidTake.setPower(0.85);
            }
            else{
                MidTake.setPower(0);
            }

            if (LT){
                feeder.setPower(0.8676767 );
            }
            else{
                feeder.setPower(0);
            }

            if (STATUSX){

                rampServo.setPosition(Func.RampCatchPos);
            }
            else{
                rampServo.setPosition(Func.RampClosedPos);
            }
            if (STATUSA){

                L_shooter.setPower(potencia);
                R_shooter.setPower(potencia);
            }
            else {
                L_shooter.setPower(0);
                R_shooter.setPower(0);
            }


            LastY = Y;
            LastA = A;
            LastX = X;


            telemetry.addData("potencia", potencia);
            telemetry.update();


        }
    }
}
