package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class IMU_Initializer {

    IMU imu;


    public void IMU_init(HardwareMap hardwareMap){

        imu = hardwareMap.get(IMU.class, "imu");

        RevHubOrientationOnRobot orientationOnRobot = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.BACKWARD
        );

        imu.initialize(new IMU.Parameters(orientationOnRobot));
        imu.resetYaw();
    }

    public double getAngle(){
        return imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
    }

    public void resetYaw(){
        imu.resetYaw();
    }
}
