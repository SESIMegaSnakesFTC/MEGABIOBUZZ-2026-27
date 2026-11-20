package org.firstinspires.ftc.teamcode.Mech.Led;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class RGB_Indicator  {

    Servo Indicator;

    //color

    final double RED    = 0.299;
    final double YELLOW = 0.388;
    final double ORANGE = 0.321;
    final double GREEN  = 0.500;
    final double BLUE   = 0.611;
    final double OFF    = 0;


    public void init(HardwareMap hardwareMap){

        Indicator = hardwareMap.get(Servo.class, "Indicator");
        Indicator.setPosition(0);
    }

    public void setColor(boolean Off, boolean red, boolean orange, boolean yellow, boolean green ){

        if (red) Indicator.setPosition(RED);
        if (orange) Indicator.setPosition(ORANGE);
        if (yellow) Indicator.setPosition(YELLOW);
        if (green) Indicator.setPosition(GREEN);
        if (Off) Indicator.setPosition(OFF);
    }

    public void setColorBasedInNumber(int Number){
        /*Has to be a number in this period -> 0 until 4 */

        switch (Number){

            case 0:
                //No collor
                setColor(true, false, false, false, false);
                break;

            case 1:
                //RED
                setColor(false, true, false, false, false);
                break;

            case 2:
                //ORANGE
                setColor(false, false, true, false, false);
                break;

            case 3:
                //YELLOW
                setColor(false, false, false, true, false);
                break;

            case 4:
                //VERDE
                setColor(false, false, false, false, true);
                break;

        }


    }
}
