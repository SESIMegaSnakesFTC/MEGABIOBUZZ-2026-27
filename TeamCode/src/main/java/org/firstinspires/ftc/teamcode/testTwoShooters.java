package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


@TeleOp(name = "testTwoShooters", group = "TeleOp")
public class testTwoShooters extends LinearOpMode {
    private DcMotor LeftShooter, RightShooter;
    private boolean LastA = false;


    public void runOpMode(){

        LeftShooter  = hardwareMap.get(DcMotor.class, "leftShooter");
        RightShooter = hardwareMap.get(DcMotor.class, "rightShooter");
        LeftShooter.setDirection(DcMotor.Direction.REVERSE);

        waitForStart();
        while (opModeIsActive()){

            boolean A = gamepad2.a;

            if (A && !LastA){

                LeftShooter.setPower(0.8); RightShooter.setPower(0.8);
            }
            else {
                LeftShooter.setPower(0); RightShooter.setPower(0);
            }

            LastA = A;
        }

    }
}
