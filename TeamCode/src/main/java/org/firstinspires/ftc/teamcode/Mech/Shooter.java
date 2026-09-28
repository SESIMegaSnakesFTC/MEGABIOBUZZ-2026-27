package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Shooter {

    DcMotorEx shooter;
    VoltageSensor battery;
    ElapsedTime timer = new ElapsedTime();

    double TargetVelocity = 1578;

    static final double BasePotence = 16.67;
    static final double PorcentOfBatteryOf = 12.67;

    double lastF = BasePotence;
    double cachedVolts;


    public void init(HardwareMap hardwareMap){

        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setVelocityPIDFCoefficients(60, 0, 0, BasePotence);
    }

    public void Activate(boolean RTp2){

        if (RTp2){

            if (timer.milliseconds() > 500){
                cachedVolts = battery.getVoltage();
                timer.reset();
            }

            double f = BasePotence * (PorcentOfBatteryOf/cachedVolts);

            if(Math.abs(f - lastF) > 0.05){

                shooter.setVelocityPIDFCoefficients(60, 0,0, f);
                lastF = f;
            }

            shooter.setVelocity(TargetVelocity);
        }
        else{
            shooter.setVelocity(0);
        }

    }
}
