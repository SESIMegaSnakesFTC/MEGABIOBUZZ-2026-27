package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Feeder {



    private DcMotor feeder;


    public void init(HardwareMap hardwareMap){

        feeder = hardwareMap.get(DcMotorEx.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder.setDirection(DcMotor.Direction.REVERSE);
        
    }

    public void activate(boolean LB, boolean RB){

        if (RB){
            feeder.setPower(0.8);
        }
        else if (LB){
            feeder.setPower(-0.8);
        }

        else feeder.setPower(0);

    }

    public double getHoodPos(){
        return feeder.getCurrentPosition();
        //TODO: Vou usar o External encoder do hood na porta de encoder do feeder
    }
}
