package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class feeder {

    private DcMotor feeder;


    public void init(HardwareMap hardwareMap){

        feeder = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    public void activate(boolean LT, boolean LB){

        if (LT){
            feeder.setPower(0.8);
        } else if (LB){
            feeder.setPower(-0.8);
        } else {
            feeder.setPower(0);
        }
    }
}
