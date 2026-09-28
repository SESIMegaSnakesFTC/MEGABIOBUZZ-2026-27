package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class FlowerServo {

    private CRServo FlowerServo;


    public void init(HardwareMap hardwareMap){

        FlowerServo = hardwareMap.get(CRServo.class, "FlowerServo");

    }
    public void Spin(boolean Ap1){

        if (Ap1){
            FlowerServo.setPower(0.5);
        }
        else{
            FlowerServo.setPower(0);
        }
    }

}
