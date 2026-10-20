package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class MidTake {

    private DcMotorEx midtake;

    public void init(HardwareMap hardwareMap){

        midtake = hardwareMap.get(DcMotorEx.class, "midTake");
        midtake.setDirection(DcMotorEx.Direction.REVERSE);
        midtake.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);

    }

    public void spin(boolean RBp1, boolean Yp2){

        if (RBp1 || Yp2){
            midtake.setVelocityPIDFCoefficients(60, 0,0, 16.67);
            midtake.setVelocity(1578);
        }
        else{
            midtake.setVelocity(0);
        }
    }

    public void Tune(double P, double I, double D, double F, double rpm){
        midtake.setVelocityPIDFCoefficients(P, I, D, F);
        midtake.setVelocity(rpm);
    }

    public double getVelocity(){ return midtake.getVelocity();}

}
