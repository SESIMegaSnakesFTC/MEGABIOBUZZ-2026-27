package org.firstinspires.ftc.teamcode.Another_Codes;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Mech.Chassi;

import java.util.List;


@TeleOp(name = "Movimento Mecanum", group = "TeleOp")
public class Moviment extends LinearOpMode {

    private DcMotor LeftBack, LeftFront, RightBack, RightFront;

    @Override
    public void runOpMode() {



        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        Chassi chassi = new Chassi();
        chassi.init(hardwareMap);

        waitForStart();

        while (opModeIsActive()) {

            double y =  gamepad1.left_stick_y;
            double x =  gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            chassi.drive(x, y, rx);


        }
    }


}
