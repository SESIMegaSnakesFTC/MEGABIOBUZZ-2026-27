package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Feeder {



    private DcMotorEx feeder;


    public void init(HardwareMap hardwareMap){

        feeder = hardwareMap.get(DcMotorEx.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        feeder.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        feeder.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
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

    public double getHoodPos(){
        return feeder.getCurrentPosition();
        //TODO: Vou usar o External encoder do hood na porta de encoder do feeder
    }
}
