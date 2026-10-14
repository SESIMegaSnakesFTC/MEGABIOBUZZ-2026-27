package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class MidTake {

    private DcMotorEx MidTake;

    public void init(HardwareMap hardwareMap){

        MidTake = hardwareMap.get(DcMotorEx.class, "midTake");
        MidTake.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        MidTake.setVelocityPIDFCoefficients(60, 0,0, 16.67);
    }

    public void spin(boolean RBp1, boolean Yp2){

        if (RBp1 || Yp2){
            MidTake.setVelocity(1578);
        }
        else{
            MidTake.setVelocity(0);
        }
    }
}
