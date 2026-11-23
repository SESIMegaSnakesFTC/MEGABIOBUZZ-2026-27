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



    public void setColorBasedInNumber(int Number){
        /*Has to be a number in this period -> 0 until 4 */

        switch (Number){

            case 0:
                //No collor
                Indicator.setPosition(OFF);
                break;

            case 1:
                //RED
                Indicator.setPosition(RED);
                break;

            case 2:
                //ORANGE
                Indicator.setPosition(ORANGE);
                break;

            case 3:
                //YELLOW
                Indicator.setPosition(YELLOW);
                break;

            case 4:
                //VERDE
                Indicator.setPosition(GREEN);
                break;

        }


    }
}
