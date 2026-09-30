package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.TurretServo;
import org.firstinspires.ftc.teamcode.Mech.midTake;

import java.util.List;


@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class RedOficialTeleWarmUp extends LinearOpMode {



    private final Chassi chassi       = new Chassi();
    private final Shooter shooter     = new Shooter();
    private final midTake midtake     = new midTake();
    private final Feeder feeder       = new Feeder();
    private final LimeLight limelight = new LimeLight();
    private final TurretServo turret  = new TurretServo();


    //>>>>Logic button<<<<

    TurnOn_TurnOff button = new TurnOn_TurnOff();


    //>>>Logic Variables<<<

    boolean lastRT = false, RT_ON = false;
    boolean lastA = false, A_ON = false;
    boolean lastB = false, B_ON = false;


    @Override
    public void runOpMode() {


        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        //INITS

        chassi.init(hardwareMap);
        shooter.init(hardwareMap);
        midtake.init(hardwareMap);
        feeder.init(hardwareMap);
        limelight.init(hardwareMap);
        turret.init(hardwareMap);


        waitForStart();

        while (opModeIsActive()){

            //>>>Drive Train<<<
            double x  = gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            chassi.drive(x, y, rx);

        //=======================================

            //>>>Shooter<<<

            boolean RTp2 = gamepad2.right_trigger > 0.5;

            RT_ON = button.IsActivate(RTp2, lastRT, RT_ON);
            shooter.Activate(RT_ON);
            lastRT = RTp2;

        //=======================================

            //>>>MidTake<<<

            boolean RBp1 = gamepad1.right_bumper;

            midtake.Spin(RBp1, false);//Sem acionamento no player 2


        //=======================================

            //>>>Feeder<<<

            boolean LT = gamepad2.left_trigger > 0.5;
            boolean LB = gamepad2.left_bumper;

            feeder.activate(LT, LB);

        //=======================================

            //>>>Limelight && Turret<<<

                //Limelight
            boolean A = gamepad1.a;
            A_ON = button.IsActivate(A, lastA, A_ON);
            double CurrentTx = limelight.MediumTx();

                //Turret Manual/Auto
            boolean B    = gamepad1.b;
            boolean RTp1 = gamepad1.right_trigger > 0.5;
            boolean LTp1 = gamepad1.left_trigger > 0.5;

            B_ON = button.IsActivate(B, lastB, B_ON);

            if (A_ON){
                limelight.TurnON(true);

                if (B_ON){
                    //turret.TurretTracking(gamepad1, RTp1, LTp1, CurrentTx, B_ON, 0);
                }
            }

            lastA = A;
            lastB = B;

            telemetry.addData("Limelight ON", A_ON);
            telemetry.addData("Auto Tracking", B_ON);
            telemetry.addData("CurrentTx", CurrentTx);
            telemetry.update();
        }




    }
}