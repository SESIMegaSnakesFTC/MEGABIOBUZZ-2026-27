package org.firstinspires.ftc.teamcode.Mech.Led;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RGB_Indicator  {

    Servo Indicator;

    //color

    final double YELLOW = 0.388;
    final double GREEN  = 0.500;
    final double BLUE   = 0.611;
    final double RED    = 0.277;
    final double OFF    = 0;


    public void init(HardwareMap hardwareMap){

        Indicator = hardwareMap.get(Servo.class, "Indicator");
        Indicator.setPosition(0);
    }

    public void setColor(boolean Off, boolean yellow, boolean green,
                         boolean blue, boolean red){

        if (red) Indicator.setPosition(RED);
        if (blue) Indicator.setPosition(BLUE);
        if (green) Indicator.setPosition(GREEN);
        if (yellow) Indicator.setPosition(YELLOW);
        if (Off) Indicator.setPosition(OFF);
    }
}
