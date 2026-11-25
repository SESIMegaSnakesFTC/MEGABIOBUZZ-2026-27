package org.firstinspires.ftc.teamcode.Mech.Servos;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RightRampServo {

    private Servo rightRampServo;
    float Open = 1, Closed = 0;

    public void init(HardwareMap hardwareMap){

        rightRampServo = hardwareMap.get(Servo.class, "rightRampServo");
        rightRampServo.setPosition(0.5);
    }


    public void Position(boolean RB_ON){ rightRampServo.setPosition(RB_ON ? Open : Closed); }

}
