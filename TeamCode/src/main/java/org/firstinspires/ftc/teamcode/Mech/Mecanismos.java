package org.firstinspires.ftc.teamcode.Mech;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

public class Mecanismos {

    private DcMotor feeder, midTake;
    private DcMotorEx leftShooter, rightShooter;
    private final double targetvelocity = 1560;

    public double LeftErro = 0, RightErro = 0;

    enum LimeStatus { CATCH, RIGHT, LEFT, NONE}
    LimeStatus act = LimeStatus.NONE;

    double AligmentTurret = 0.08;

    // Init's for mech devices

    public void init(HardwareMap hardwareMap){

        feeder       = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftShooter  = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");
        leftShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightShooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftShooter.setDirection(DcMotorSimple.Direction.FORWARD);
        rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

        midTake      = hardwareMap.get(DcMotor.class, "midTake");


    }

    public void IMU_init(HardwareMap hardmap){

        IMU imu;
        imu = hardmap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientationOnRobot =
              new RevHubOrientationOnRobot(
                      RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                      RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
              );

    }
    public void Limelight_init(HardwareMap hardwareMap){

        Limelight3A limelight3A;
        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
        limelight3A.start();
        limelight3A.setPollRateHz(75);
        limelight3A.pipelineSwitch(8);
    }



    //===============================================================================

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


            leftShooter.setVelocityPIDFCoefficients(60.5, 0, 0, 19.89);
            leftShooter.setVelocity(targetvelocity);
            rightShooter.setVelocityPIDFCoefficients(60.5, 0,0, 16.67);
            rightShooter.setVelocity(targetvelocity);


            LeftErro  = targetvelocity - leftShooter.getVelocity();
            RightErro = targetvelocity - rightShooter.getVelocity();
        }
        else{

            StopAllShooters();


        }


    }

    public void StopAllShooters(){
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
            midTake.setPower(0.3867);
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
            midTake.setPower(0.67);
        }
        else{
            midTake.setPower(0);
        }
    }

    public void setFlowerServo(boolean Y, CRServo servoFlower){
        if (Y){

            servoFlower.setPower(0.8);
        }
        else{
            servoFlower.setPower(0);
        }
    }

    public void Trackinglimelight(Servo TurretServo, LLResult result, Gamepad gamepad2){

        if (result != null && result.isValid()){

            act = result.getTx() < -5 ? LimeStatus.LEFT :
                    (result.getTx() > 5 ? LimeStatus.RIGHT : LimeStatus.CATCH);
        }else{
            act = LimeStatus.NONE;
        }



        switch (act){


            case CATCH:
                gamepad2.rumble(0.5, 0.5, 500);
                rightShooter.setVelocityPIDFCoefficients(60.5, 0, 0, 19.89);
                leftShooter.setVelocityPIDFCoefficients(60.5, 0, 0, 16.67);
                break;

            case LEFT:
                TurretServo.setPosition(turretCurrentPos(TurretServo) - AligmentTurret);
                break;

            case RIGHT:
                TurretServo.setPosition(turretCurrentPos(TurretServo) + AligmentTurret);
                break;

            case NONE:
                break;

        }
    }

    public double turretCurrentPos(Servo Turret){ return Turret.getPosition(); }

    public double GetShooterRPM(LLResult result){


    }

}
