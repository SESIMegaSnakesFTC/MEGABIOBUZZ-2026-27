package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.IMU_Initializer;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.TurretServo;
import org.firstinspires.ftc.teamcode.Mech.feeder;
import org.firstinspires.ftc.teamcode.Mech.midTake;

import java.util.List;

@TeleOp(name = "TeleWarmUpLimelight", group = "TeleOp")
public class RedTeleWarmUpLime extends LinearOpMode {

    //===========================================================================


    Chassi drivetrain = new Chassi(); feeder myfeeder = new feeder();
    IMU_Initializer imu = new IMU_Initializer(); midTake midtake = new midTake();
    TurretServo turret = new TurretServo(); Shooter shooter = new Shooter();

    //Limelight
    LimeLight limeLight = new LimeLight(); LLResult resultado;
    boolean lastA, A_ON;

    //Logic to button for turn on && turn off

    TurnOn_TurnOff logicButton = new TurnOn_TurnOff();


    //====RT STATUS FOR SHOOTER====

    boolean lastRT = false;
    boolean RT_ON = false;


    @Override
    public void runOpMode() {



        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        //INITS

        drivetrain.init(hardwareMap);
        myfeeder.init(hardwareMap);
        imu.IMU_init(hardwareMap);
        midtake.init(hardwareMap);
        turret.init(hardwareMap);
        shooter.init(hardwareMap);
        limeLight.init(hardwareMap);

        waitForStart();

        while (opModeIsActive()){

            //Limelight ON

            boolean A = gamepad1.a;
            A_ON = logicButton.IsActivate(A, lastA, A_ON);
            lastA = A;

            //=======================================

            //Drive train
            double x  = gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            drivetrain.drive(x, y, rx);

            //=======================================

            //Feeder

            boolean LT = gamepad2.left_trigger > 0.5;
            boolean LB = gamepad2.left_bumper;

            myfeeder.activate(LT, LB);

            //========================================

            //MidTake

            boolean RB = gamepad1.right_bumper;

            midtake.Spin(RB, false);

            //=========================================

            //Shooter

            boolean RTp2 = gamepad2.right_trigger > 0.5;

            RT_ON = logicButton.IsActivate(
                    RTp2, lastRT, RT_ON);
            shooter.Activate(RT_ON);
            lastRT = RTp2;

            //=========================================

            if (A_ON){

                //Limelight

                limeLight.TurnON(true);
                resultado = limeLight.getResult();


                //========================================

                //TurretServo

                boolean RTp1 = gamepad1.right_trigger > 0.5;
                boolean LTp1 = gamepad1.left_trigger > 0.5;

                double CurrentTX = limeLight.MediumTx();

                turret.TurretTracking(gamepad2, RTp1, LTp1, CurrentTX);

                //=========================================

            }
            else{
                limeLight.TurnOFF();
            }
        }
    }
}
