package org.firstinspires.ftc.teamcode.TeleOperados;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.midTake;

@Disabled
@TeleOp(name = "TeleOpTracking", group = "TeleOp")
public class TeleOperado extends LinearOpMode {




    @Override
    public void runOpMode(){


        //INITS --> Um Monte slk

        // >>> Bateria <<<

        VoltageSensor vs = hardwareMap.voltageSensor.iterator().next(); //Sapeca a tensao da bateria


            //>>>Chassi<<<

        Chassi chassi   = new Chassi();
        chassi.init(hardwareMap);

            //>>>Feeder<<<

        Feeder feeder   = new Feeder();
        feeder.init(hardwareMap);

            //>>>MidTake<<<

        midTake midtake = new midTake();
        midtake.init(hardwareMap);

            //>>>Shooter<<<
        Shooter shooter = new Shooter(vs);
        shooter.init(hardwareMap);

            //>>>Limelight

        LimeLight limeLight = new LimeLight();
        limeLight.init(hardwareMap);

            //>>>Turret<<<

        TurretController turret = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        TurretTracking tracker  = new TurretTracking(turret);




        boolean lastA = false, A_ON = false; //Tracking
        boolean lastB = false, B_ON = false; //Limelight
        boolean TaNoTracking = false;


        waitForStart();

        while(opModeIsActive()){




        }


    }


}
