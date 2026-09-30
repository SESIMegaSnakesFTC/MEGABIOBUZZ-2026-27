package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.TurretServo;

@TeleOp(name = "TrackingServo", group = "TeleOp")
public class TrackingTurretServo extends LinearOpMode {

    double CurrenTx = 0;
    private TurretServo turretServo = new TurretServo();
    private LimeLight limelight = new LimeLight();

    //Logic Button

    TurnOn_TurnOff button = new TurnOn_TurnOff();

    public void runOpMode(){

        turretServo.init(hardwareMap);
        limelight.init(hardwareMap);

        //Variables for button A && B

        boolean lastA = false, A_ON = false;
        boolean lastB = false, B_ON = false;


        waitForStart();

        while(opModeIsActive()){

            boolean RT = gamepad1.right_trigger > 0.5;
            boolean LT = gamepad1.left_trigger  > 0.5;

            //Limelight Activating
            boolean B = gamepad1.b;

            B_ON = button.IsActivate(B, lastB, B_ON);


            if (B_ON) {

                limelight.TurnON(true);
                CurrenTx = limelight.MediumTx();
            }
            else limelight.TurnOFF();

            //Activating tracking btw

            boolean A = gamepad1.a;
            A_ON = button.IsActivate(A, lastA, A_ON);

            if (A_ON){
                turretServo.TurretTracking(gamepad1, RT, LT, CurrenTx, true);
            }
            else{
                turretServo.TurretTracking(gamepad1, RT, LT, CurrenTx, false);
            }

            lastA = A;
            lastB = B;



            telemetry.addData("Limelight", B_ON ? "ON" : "OFF");
            telemetry.addData("tracking", A_ON ? "ON" : "OFF");
            telemetry.addData("Manual Mode", !A_ON ? "ON" : "OFF");
            telemetry.addData("Status Limelight", turretServo.getAct());
            telemetry.addData("LastTx", turretServo.getLastTx());
            telemetry.update();
        }
    }

}
