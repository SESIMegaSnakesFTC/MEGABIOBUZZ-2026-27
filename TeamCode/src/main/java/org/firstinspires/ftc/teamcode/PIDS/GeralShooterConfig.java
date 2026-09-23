package org.firstinspires.ftc.teamcode.PIDS;



import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class GeralShooterConfig {

    private DcMotorEx leftShooter, rightShooter;
    private ShootersPIDF leftPIDF , rightPIDF;
    private double targetVelocity = 0;

    private static final double K_SYNC = 0;

    public GeralShooterConfig(HardwareMap hardwareMap){

        leftShooter  = hardwareMap.get(DcMotorEx.class, "leftShooter");
        rightShooter = hardwareMap.get(DcMotorEx.class, "rightShooter");

        leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftShooter.setDirection(DcMotorEx.Direction.FORWARD);
        rightShooter.setDirection(DcMotorEx.Direction.REVERSE);

        double Kp = 0.0008, Ki = 0.00054, Kd = 0.0001, Kf = 0.00058;
        rightPIDF = new ShootersPIDF(Kp, Ki, Kd, Kf, hardwareMap);
        leftPIDF  = new ShootersPIDF(Kp, Ki, Kd, Kf, hardwareMap);

    }
    public void setTargetVelocity(double TicksPerSec){

        this.targetVelocity = TicksPerSec;
    }


    public void stop(){

        targetVelocity = 0;
        leftPIDF.reset();
        rightPIDF.reset();
        leftShooter.setPower(0);
        rightShooter.setPower(0);
    }

    public void update(){



        double leftVel  = leftShooter.getVelocity();
        double rightvel = rightShooter.getVelocity();

        double leftPotence  = leftPIDF.Calculate(targetVelocity, leftVel);
        double rightPotence = rightPIDF.Calculate(targetVelocity, rightvel);

        double Diff =  rightvel - leftVel;
        double correction = K_SYNC * (Diff+0.02);

        leftShooter.setPower(LimiterPotence(leftPotence + correction));
        rightShooter.setPower(LimiterPotence(rightPotence - correction));
    }


    private double LimiterPotence(double potent){

        return Math.max(-1, Math.min(0.75, potent));
    }

    public double getRightVelocity(){ return rightShooter.getVelocity(); }
    public double getLeftVelocity(){ return leftShooter.getVelocity(); }
}
