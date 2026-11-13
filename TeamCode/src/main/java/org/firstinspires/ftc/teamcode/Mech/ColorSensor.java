package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class ColorSensor {

    NormalizedColorSensor colorSensor;


    public void init(HardwareMap hardwareMap, float Gain){
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensor");
        colorSensor.setGain(Gain);
    }


    public boolean ReadColor(){


        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        float[] hsvValues = new float[3];
        android.graphics.Color.colorToHSV(colors.toColor(), hsvValues);

        float hue = hsvValues[0];
        float saturation = hsvValues[1];

        double distanceMM = ((RevColorSensorV3) colorSensor).getDistance(DistanceUnit.MM);


        //Viu a ball
        return (hue >= 40 && hue <= 70 && saturation > 0.5f) || distanceMM < 50;


    }
}
