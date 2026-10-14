package org.firstinspires.ftc.teamcode.Mech.Servos;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class GateServo {

    // >>> Testar <<<
    double openPos   = 1;
    double closedPos = 0;

    private Servo gateServo;

    public void init(HardwareMap hardwareMap){

        gateServo = hardwareMap.get(Servo.class, "gateServo");


    }
    public void ShooterIsWorking(boolean shooterON){
        // >>> Chamar junto com o shooter <<<
        if (shooterON) gateServo.setPosition(openPos);
        else gateServo.setPosition(closedPos);
    }
}
