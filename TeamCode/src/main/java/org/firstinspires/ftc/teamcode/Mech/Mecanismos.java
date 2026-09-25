package org.firstinspires.ftc.teamcode.Mech;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Mecanismos {

    private Servo rampServo;
    private DcMotor feeder, midTake;
    private DcMotorEx leftShooter, rightShooter;
    private final double targetvelocity = 1560;

    public double LeftErro = 0, RightErro = 0;

    double RampCatchPos = 0.85f, RampClosedPos = 0.47f;


    public void init(HardwareMap hardwareMap){

        rampServo    = hardwareMap.get(Servo.class, "rightRampServo");
        feeder       = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftShooter  = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");
        leftShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftShooter.setDirection(DcMotorSimple.Direction.FORWARD);
        rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

        midTake      = hardwareMap.get(DcMotor.class, "midTake");
        midTake.setDirection(DcMotorSimple.Direction.REVERSE);

        rampServo.setPosition(RampCatchPos);

    }

    public boolean RT_ON(boolean RT, boolean lastRT, boolean RT_ON){

        if (RT && !lastRT){
           return !RT_ON;
        }
        else{
            return RT_ON;
        }
    }
    public void setShooters(boolean RT_ON){

        if (RT_ON){


            leftShooter.setVelocityPIDFCoefficients(60, 0, 0, 19.89);
            leftShooter.setVelocity(targetvelocity);
            rightShooter.setVelocityPIDFCoefficients(60, 0,0, 16.5);
            rightShooter.setVelocity(targetvelocity);


            LeftErro  = targetvelocity - leftShooter.getVelocity();
            RightErro = targetvelocity - rightShooter.getVelocity();
        }
        else{

            StopAll();

        }


    }
    public void StopAll(){
        leftShooter.setVelocityPIDFCoefficients(0,0,0,0);
        rightShooter.setVelocityPIDFCoefficients(0,0,0,0);
        rightShooter.setVelocity(0);
        leftShooter.setVelocity(0);
    }

    public void feed(boolean LT, boolean LB){

        if (LT){
            feeder.setPower(0.9);
        }else if (LB) {
            feeder.setPower(-0.9);
        }
        else{
            feeder.setPower(0);
        }


    }

    public boolean X_ON(boolean X, boolean lastX, boolean X_ON){


        if(X && !lastX){
            return !X_ON;
        }
        else{
            return X_ON;
        }


    }
    public void setRampServo(boolean X_ON){

        if (X_ON){

            rampServo.setPosition(RampCatchPos);
        }
        else{
            rampServo.setPosition(RampClosedPos);
        }

    }

    public boolean Y_ON(boolean Y, boolean lastY, boolean Y_ON){

        if (Y && !lastY){

            return !Y_ON;
        }
        else{
            return Y_ON;
        }

    }
    public void setMidTake(boolean Y_ON){

        if (Y_ON){
            midTake.setPower(-0.68);
        }
        else{
           midTake.setPower(0);
        }


    }

    public boolean B_ON(boolean B, boolean lastB, boolean B_ON){

        if (B && !lastB){
            return !B_ON;
        }
        else{
            return B_ON;
        }
    }

    public void setMidTakePlayer1(boolean RB1){

        if (RB1){
            midTake.setPower(-0.7);
        }
        else{
            midTake.setPower(0);
        }
    }




}
