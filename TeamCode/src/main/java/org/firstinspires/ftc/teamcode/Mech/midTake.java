package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class midTake {

    private DcMotor MidTake;

    public void init(HardwareMap hardwareMap){

        MidTake = hardwareMap.get(DcMotor.class, "midTake");
        MidTake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

    }

    public void Spin(boolean RBp1, boolean Yp2){

        if (RBp1 || Yp2){
            MidTake.setPower(0.4);
        }
        else{
            MidTake.setPower(0);
        }
    }
}
