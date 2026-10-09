package org.firstinspires.ftc.teamcode.Another_Codes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.Feeder;

@TeleOp(name = "TestFeeder")
public class MovimentAndFeeder extends LinearOpMode {

    private Feeder feeder = new Feeder();
    private Chassi chassi = new Chassi();


    @Override
    public void runOpMode(){

        chassi.init(hardwareMap);
        feeder.init(hardwareMap);

        waitForStart();

        while(opModeIsActive()){

            double x  = gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            chassi.drive(x, y, rx);
            feeder.activate(gamepad1.left_bumper, gamepad1.right_bumper);

        }
    }
}
