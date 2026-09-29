package org.firstinspires.ftc.teamcode.Mech;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

public class TurretMotor {

    double lastTX = 0;
    double p = 0.567;
    double i = 1.2;
    double d = 0.13;
    double f = 1.3;

    private enum LimeStatus {CATCH, LEFT, RIGHT, NONE}
    LimeStatus act = LimeStatus.NONE;

    private DcMotorEx turretMotor;

    public void init(HardwareMap hardwareMap){

        turretMotor = hardwareMap.get(DcMotorEx.class, "turretMotor");
        turretMotor.setVelocityPIDFCoefficients(p,i,d,f);
        turretMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    }

    public void TurretMotorTracking(Gamepad gamepad1, boolean RTp1, boolean LTp1, double CurrentTx){


        //DEFININDO LADO PARA TRACKING

        act = CurrentTx == 0 ? LimeStatus.NONE : (CurrentTx < -2.25 ? LimeStatus.LEFT :
                                                             (CurrentTx > 2.25 ? LimeStatus.RIGHT : LimeStatus.CATCH));


        //MANUAL TRACKING FORÇAR A RECONHECER
        if (RTp1){
            act = LimeStatus.RIGHT;
        }
        else if (LTp1){
            act = LimeStatus.LEFT;
        }


        switch (act){


            case CATCH:
                turretMotor.setVelocity(0);
                gamepad1.rumble(0.5, 0.5, 500);
                break;

            case LEFT:
                turretMotor.setVelocity(Math.abs(CurrentTx*210/18.5));
                break;

            case RIGHT:
                turretMotor.setVelocity(Math.abs(CurrentTx*210/18.5)*-1);
                break;

            case NONE:

                ElapsedTime timer = new ElapsedTime();
                turretMotor.setVelocity(lastTX < -0.1 ? Math.abs(CurrentTx*210/20.45)*-1 : (timer.seconds() > 1.5 ? Math.abs(CurrentTx*210/18.5):0));

                break;

        }
        lastTX = CurrentTx;
    }

    public int saveCurrentPos(){
        return turretMotor.getCurrentPosition();
    }
    public void setPIDF(double p, double i, double d, double f){
        turretMotor.setVelocityPIDFCoefficients(p,i,d,f);
    }

    public void GoToLastPos(int Position){
        turretMotor.setTargetPosition(Position-5);
        turretMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        turretMotor.setPower(0.467);
    }
    public boolean FinishedTrack(){
        return !turretMotor.isBusy();
    }
    public void setNormalMode(){
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
}
