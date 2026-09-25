package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.Mecanismos;

import java.util.List;


@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class OficialTeleWarmUp extends LinearOpMode {

    boolean lastRT = false, RT_ON = false;
    boolean lastX = false, X_ON = true;
    boolean lastB = false, B_ON = false;



    //INSTÂNCIA PARA IMPORT'S(Bastante)
    
    private final Chassi chassi = new Chassi();
    private final Mecanismos mech = new Mecanismos();


    @Override
    public void runOpMode() {


        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        chassi.init(hardwareMap);
        mech.init(hardwareMap);

        waitForStart();

        while (opModeIsActive()) {

            double x = gamepad1.left_stick_x, y = -gamepad1.left_stick_y, rx = gamepad1.right_stick_x;

            boolean RT = gamepad2.right_trigger > 0.5;
            boolean LT = gamepad2.left_trigger > 0.5, LB = gamepad2.left_bumper;
            boolean X = gamepad2.x;
            boolean B = gamepad2.b;
            boolean RB1 = gamepad1.right_bumper;


            chassi.drive(x, y, rx);

            //BUTTON
            B_ON = mech.B_ON(B, lastB, B_ON);
            RT_ON = mech.RT_ON(RT, lastRT, RT_ON);
            mech.setShooters(RT_ON);


            mech.feed(LT, LB);
            X_ON = mech.X_ON(X, lastX, X_ON);
            mech.setRampServo(X_ON);

            mech.setMidTakePlayer1(RB1);


            lastRT = RT;
            lastX = X;

            telemetry.addData("Ramp STATUS", X_ON ? "ABERTA" : "FECHADA");
            telemetry.update();

        }
    }
}