package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;


public class TurretServo {



    private CRServo TurretServo;
    private enum LimeStatus {CATCH, LEFT, RIGHT, NONE}
    LimeStatus act = LimeStatus.NONE;
    double lastTX = 0;


    public void init(HardwareMap hardwareMap){
        TurretServo = hardwareMap.get(CRServo.class, "TurretServo");
    }

    public void TurretTracking(Gamepad gamepad2, boolean RTp1, boolean LTp1, double CurrentTx, boolean Y_ON){


        //VALORES DE ALINHAMENTO -> FASTER && CONTROL
        double F_v = 0.289*CurrentTx/10.2;
        double C_v = 0.289*CurrentTx/15.6;

        //DEFININDO LADO PARA TRACKING
        //MANUAL TRACKING FORÇAR A RECONHECER
        if (!Y_ON){

            if (RTp1){
                TurretServo.setPower(0.2867);
            }
            else if (LTp1){
                TurretServo.setPower(-0.2867);
            }
            else TurretServo.setPower(0);
        }
        else{
            act = CurrentTx == 0  ? LimeStatus.NONE :(CurrentTx < -4.5 ? LimeStatus.LEFT :
                                                      (CurrentTx > 4.5 ? LimeStatus.RIGHT : LimeStatus.CATCH));
        }

        if (act == LimeStatus.NONE) {
            TurretServo.setPower(lastTX < 0 ? F_v : (lastTX > 0 ? -F_v : 0 ));
        }


        switch (act){


            case CATCH:

                TurretServo.setPower(0);
                gamepad2.rumble(0.5, 0.5, 500);
                break;

            case LEFT:
                TurretServo.setPower(CurrentTx > -5 ? F_v : C_v);
                break;

            case RIGHT:
                TurretServo.setPower(CurrentTx < 5 ? F_v : C_v);
                break;

            case NONE:

                break;

        }

        if (CurrentTx != 0){
            lastTX = CurrentTx;
        }



    }

    public LimeStatus getAct(){
        return act;
    }

    public double getLastTx(){
        return lastTX;
    }
}
