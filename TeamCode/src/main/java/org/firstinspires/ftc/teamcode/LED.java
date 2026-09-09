package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Disabled
@TeleOp(name = "LED", group = "TeleOp")
public class LED extends LinearOpMode {

    private Servo LED;
    private boolean LastA = false;
    private boolean ActualA = false;


    public void runOpMode(){

        LED = hardwareMap.get(Servo.class, "LED");

        waitForStart();

        while(opModeIsActive()){

            ActualA = gamepad1.a;

            if (ActualA && !LastA){

                LED.setPosition(1.0); //LIGADO

            }
            else if (ActualA && LastA){

                LED.setPosition(0.0); //DESLIGADO
            }

            LastA = ActualA;
        }
    }
}
