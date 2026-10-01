package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.TurretServo;

@TeleOp(name = "TrackingServo", group = "TeleOp")
public class TrackingTurretServo extends LinearOpMode {

    double CurrenTx = 0;
    double adjust = 0;
    boolean lastUP = false, lastDOWN = false;
    private final TurretServo turretServo = new TurretServo();
    private final LimeLight limelight = new LimeLight();

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

            //Adjust value

            boolean dpadUP = gamepad1.dpad_up;
            boolean dpadDOWN = gamepad1.dpad_down;

            if (dpadDOWN && !lastDOWN){
                adjust += 0.01;
            }

            if (dpadUP && !lastUP){
                adjust -= 0.01;
            }

            lastUP = dpadUP;
            lastDOWN = dpadDOWN;


            boolean RT = gamepad1.right_trigger > 0.5;
            boolean LT = gamepad1.left_trigger  > 0.5;

            //Limelight Activating
            boolean B = gamepad1.b;

            B_ON = button.IsActivate(B, lastB, B_ON);


            if (B_ON) {

                limelight.TurnON(false);
                CurrenTx = limelight.MediumTx();
            }
            else limelight.TurnOFF();

            //Activating tracking btw

            boolean A = gamepad1.a;
            A_ON = button.IsActivate(A, lastA, A_ON);

            //Nova variável
            LLResult result = limelight.getResult();
            boolean TagVisible = result != null && result.isValid();


            turretServo.TurretTracking(gamepad1, RT, LT, TagVisible, CurrenTx, A_ON);




            double trackingPotence = turretServo.getPower();

            lastA = A;
            lastB = B;



            telemetry.addData("Limelight", B_ON ? "ON" : "OFF");
            telemetry.addData("tracking", A_ON ? "ON" : "OFF");
            telemetry.addData("Manual Mode", !A_ON ? "ON" : "OFF");
            telemetry.addData("Status Limelight", turretServo.getAct());
            telemetry.addData("LastTx", turretServo.getLastTx());
            telemetry.addData("lastAct", turretServo.getLastAct());
            telemetry.addData("Força tracking", trackingPotence);
            telemetry.update();
        }
    }

}
