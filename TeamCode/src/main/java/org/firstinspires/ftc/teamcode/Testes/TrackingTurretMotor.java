package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.TurretMotor;

@TeleOp(name = "TrackingMotorTurret", group = "TeleOp")
public class TrackingTurretMotor extends LinearOpMode {

    TurretMotor turretMotor = new TurretMotor();
    LimeLight limeLight = new LimeLight();
    LLResult result;

    //Button
    boolean lastA = false, A_ON = true;
    boolean lastB = false, B_ON = false;
    int CurrentPos = 0;


    //====================

    boolean lastUp = false, lastDown = false;
    boolean lastDLeftp2 = false, lastDRightp2 = false;
    boolean lastDLeft = false, lastDRight = false;
    boolean lastDownP2 = false, lastUpP2 = false;
    double targetVelocity = 320;
    boolean Running = false;
    double p = 0.567; double pStep = 0.1;
    double i = 1.2; double iStep = 0.1;
    double d = 0.13; double dStep = 0.1;
    double f = 1.3; double fStep = 0.1;


    TurnOn_TurnOff button = new TurnOn_TurnOff();

    public void runOpMode(){

        turretMotor.init(hardwareMap);
        limeLight.init(hardwareMap);
        limeLight.TurnON(false);

        waitForStart();

        while (opModeIsActive()){

            //LIGA E DESLIGA LIMELIGHT
            boolean A = gamepad1.a;

            boolean DLeftp2 = gamepad2.dpad_left;
            boolean DRightp2 = gamepad2.dpad_right;
            boolean up = gamepad1.dpad_up;
            boolean down = gamepad1.dpad_down;
            boolean DLeft = gamepad1.dpad_left;
            boolean DRight = gamepad1.dpad_right;
            boolean UpP2 = gamepad2.dpad_up;
            boolean DownP2 = gamepad2.dpad_down;
            boolean RT = gamepad1.right_trigger > 0.5;
            boolean LT = gamepad1.left_trigger > 0.5;

            A_ON = button.IsActivate(A, lastA, A_ON);


            if (A_ON){

                limeLight.TurnON(B_ON);

                if (CurrentPos != 0 && !Running){

                    turretMotor.GoToLastPos(CurrentPos);

                }else{

                    if (up && !lastUp) {
                        p += pStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (down && !lastDown) {
                        p -= pStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }
                    if (DLeft && !lastDLeft){
                        i -= iStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (DRight && !lastDRight){
                        i += iStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (DLeftp2 && !lastDLeftp2) {
                        d -= dStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (DRightp2 && !lastDRightp2) {
                        d += dStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (DownP2 && !lastDownP2){
                        f -= fStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }

                    if (UpP2 && !lastUpP2){
                        f += fStep;
                        turretMotor.setPIDF(p,i,d,f);
                    }


                    lastUp = up;
                    lastDown = down;
                    lastDLeft = DLeft;
                    lastDRight = DRight;
                    lastUpP2 = UpP2;
                    lastDownP2 = DownP2;
                    lastDRightp2 = DRightp2;
                    lastDLeftp2 = DLeftp2;



                    result = limeLight.getResult();
                    double CurrentTX = limeLight.MediumTx();
                    turretMotor.TurretMotorTracking(gamepad1, RT, LT, CurrentTX);


                }
            }
            else{
                CurrentPos = turretMotor.saveCurrentPos();
                limeLight.TurnOFF();
            }

            //==================================================


            //TROCANDO DE MODO DE RECONHECIMENTO DE APRIL TAG

            boolean B = gamepad1.b;
            B_ON = button.IsActivate(B, lastB, B_ON);


            //==================================================


            lastA = A;
            lastB = B;


            telemetry.addData("SIDE", B_ON ? "RED_DOWN" : "BLUE_UP");
            telemetry.addData("Limelight ON", A_ON);
            telemetry.addData("Seeing April Tag", result != null && result.isValid());
            telemetry.addData("P", p);
            telemetry.addData("I", i);
            telemetry.addData("D", d);
            telemetry.addData("F", f);
            telemetry.addData("Velocity (RPM)", targetVelocity);
            telemetry.update();
        }
    }
}
