package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class TurretServo {

    /*

     Falta conversar com os drivers para definir um botão para tiros automáticos

    */


    private Servo TurretServo;
    private enum LimeStatus {CATCH, LEFT, RIGHT, NONE}
    LimeStatus act = LimeStatus.NONE;
    double AligmentTurret = 0.08;


    public void init(HardwareMap hardwareMap){
        TurretServo = hardwareMap.get(Servo.class, "TurretServo");
        TurretServo.setPosition(0.5); //No meio alinhado
    }

    public void TurretTracking(LLResult result, Gamepad gamepad2){

        if (result != null && result.isValid()){

            act = result.getTx() < -5 ? LimeStatus.LEFT :
                    (result.getTx() > 5 ? LimeStatus.RIGHT : LimeStatus.CATCH);
        }else{
            act = LimeStatus.NONE;
        }


        switch (act){


            case CATCH:
                gamepad2.rumble(0.5, 0.5, 500);
                break;

            case LEFT:
                TurretServo.setPosition(turretCurrentPos() - AligmentTurret);
                break;

            case RIGHT:
                TurretServo.setPosition(turretCurrentPos() + AligmentTurret);
                break;

            case NONE:
                break;

        }
    }

    private double turretCurrentPos(){ return TurretServo.getPosition(); }

}
