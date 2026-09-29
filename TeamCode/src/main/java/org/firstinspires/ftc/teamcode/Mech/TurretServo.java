package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.ArrayList;
import java.util.List;

public class TurretServo {



    private Servo TurretServo;
    private enum LimeStatus {CATCH, LEFT, RIGHT, NONE}
    LimeStatus act = LimeStatus.NONE;
    double AligmentTurret = 0.067;


    public void init(HardwareMap hardwareMap){
        TurretServo = hardwareMap.get(Servo.class, "TurretServo");
        TurretServo.setPosition(0.5); //No meio alinhado
    }

    public void TurretTracking(Gamepad gamepad2, boolean RTp1, boolean LTp1, double CurrentTx){


        //DEFININDO LADO PARA TRACKING

        act = CurrentTx == 0 ? LimeStatus.NONE :(CurrentTx < -2.5 ? LimeStatus.LEFT :
                         (CurrentTx > 2.5 ? LimeStatus.RIGHT : LimeStatus.CATCH));

        //MANUAL TRACKING FORÇAR A RECONHECER
        if (RTp1){
            act = LimeStatus.RIGHT;
        }
        else if (LTp1){
            act = LimeStatus.LEFT;
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
