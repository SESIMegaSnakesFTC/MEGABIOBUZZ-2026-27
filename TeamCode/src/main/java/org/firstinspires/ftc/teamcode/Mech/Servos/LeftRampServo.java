package org.firstinspires.ftc.teamcode.Mech.Servos;


import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class LeftRampServo {

    private Servo leftRampServo;
    float Open = 1, Closed = 0;

    public void init(HardwareMap hardwareMap){

        leftRampServo = hardwareMap.get(Servo.class, "leftRampServo");
        leftRampServo.setDirection(Servo.Direction.REVERSE);
    }

    public void Position(boolean RB_ON){leftRampServo.setPosition(RB_ON ? Open : Closed); }
}
