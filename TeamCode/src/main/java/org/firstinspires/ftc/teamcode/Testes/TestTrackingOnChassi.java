package org.firstinspires.ftc.teamcode.Testes;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;

@TeleOp(name = "TrackingOnChassi")
public class TestTrackingOnChassi extends LinearOpMode {

    private final TurnOn_TurnOff button = new TurnOn_TurnOff();
    private final LimeLight limeLight = new LimeLight();
    private final Chassi chassi = new Chassi();


    public void runOpMode(){

        TurretController turret = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        TurretTracking tracker  = new TurretTracking(turret);
        VoltageSensor vs = hardwareMap.voltageSensor.iterator().next();
        limeLight.init(hardwareMap);
        chassi.init(hardwareMap);

        boolean lastA = false, A_ON = false;
        boolean lastB = false, B_ON = false;
        boolean TavaNOTracking = false;

        waitForStart();

        while(opModeIsActive()){


            // >>> DRIVE TRAIN <<<

            double x  =  gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            chassi.drive(x, y, rx);


            // Limelight && Turret

            boolean A = gamepad1.a;
            boolean B = gamepad1.b;

            A_ON = button.IsActivate(A, lastA, A_ON);
            B_ON = button.IsActivate(B, lastB, B_ON);


            if (B_ON) limeLight.TurnONred(false); else limeLight.TurnOFF();

            boolean TaNOTracking = A_ON && B_ON;

            if (TaNOTracking && !TavaNOTracking) tracker.reset(); // RESETA 1 VEZ
            TavaNOTracking = TaNOTracking;

            if (TaNOTracking){

                turret.enabled = true;
                turret.update(0, vs.getVoltage()); //Falta colocar o giro do chassi e tunnar
                tracker.update(limeLight.getResult());
            }else {
                turret.enabled = false;
                turret.manualPower = (gamepad1.right_trigger - gamepad1.left_trigger)/2;
                turret.update(0, vs.getVoltage());
            }


            //já deixar no esquema para telemetria ou sla, lógicas para shooter
            boolean Locked = TaNOTracking && tracker.isLocked();

            telemetry.addData("Limelight", B_ON ? "ON" : "OFF");
            telemetry.addData("Tracking", TaNOTracking ? "ON" : "OFF");
            telemetry.addData("Estado", tracker.state);
            telemetry.addData("Travado", Locked);
            telemetry.addData("Angulo / alvo", "%.1f / %.1f", turret.angleDeg, turret.getTarget());
            telemetry.addData("Erro", "%.2f", turret.getError());
            telemetry.update();



        }
        limeLight.TurnOFF();
        turret.enabled = false;
        turret.manualPower = 0;
        turret.update(0, vs.getVoltage());
    }
}
