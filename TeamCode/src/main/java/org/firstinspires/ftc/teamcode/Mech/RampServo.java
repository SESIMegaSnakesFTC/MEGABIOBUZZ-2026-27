package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RampServo {

    private Servo rampServo;
    double Open = 0.83, Closed = 0.4;

    public void init(HardwareMap hardwareMap){

        rampServo = hardwareMap.get(Servo.class, "rightRampServo");
        rampServo.setPosition(0.5);
    }


    public void Position(boolean Xp2_ON){

        if (Xp2_ON){
            rampServo.setPosition(Open);
        }
        else{
            rampServo.setPosition(Closed);
        }

    }

}
