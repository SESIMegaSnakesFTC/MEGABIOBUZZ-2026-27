package org.firstinspires.ftc.teamcode.Mech.Servos;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.TreeMap;


public class HoodServo {

    // >>> Variáveis para ajuste de angulação <<<



    // >>> SERVO <<<
    private Servo hoodServo;

    private static final TreeMap<Double, double[]> InclinationValues = new TreeMap<>();


    public void init(HardwareMap hwMap){

        hoodServo = hwMap.get(Servo.class, "hoodServo");

    }
    public void hodAdjust(double distance){




    }
}
