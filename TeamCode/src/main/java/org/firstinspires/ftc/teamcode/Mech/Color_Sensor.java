package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;


import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Color_Sensor {

    int count = 0;
    boolean lastSee = false;
    int lastValueBall = 0;
    NormalizedColorSensor colorSensor;


    public void init(HardwareMap hardwareMap, float Gain) {

        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensor");
        colorSensor.setGain(Gain);

    }


    private boolean DetectBall(double min, double max, float saturations) {


        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        float[] hsvValues = new float[3];
        android.graphics.Color.colorToHSV(colors.toColor(), hsvValues);

        float hue = hsvValues[0];
        float saturation = hsvValues[1];

        double distanceMM = ((RevColorSensorV3) colorSensor).getDistance(DistanceUnit.MM);


        //Viu a ball
        return (hue >= min && hue <= max && saturation > saturations) || distanceMM < 50;


    }

    public int CountBall(boolean Inverted, double min, double max, float saturations) {

        boolean Seeing = DetectBall(min, max, saturations);

        if (Seeing && !lastSee && (!Inverted && count < 4 || Inverted && count > 0)) {

            count += Inverted ? -1 : 1;

        }
        lastSee = Seeing;
        return count;
    }

    public void ResetBallCounter(){count = 0;}
    public boolean IsThereBall(double min, double max, float saturation){return DetectBall(min, max, saturation);}
}

