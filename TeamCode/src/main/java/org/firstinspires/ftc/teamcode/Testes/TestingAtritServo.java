package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;

@TeleOp(name = "Teste kS")
public class TestingAtritServo extends LinearOpMode {
    @Override
    public void runOpMode() {
        CRServo servo = hardwareMap.get(CRServo.class, "TurretServo");
        double power = 0;

        waitForStart();
        while (opModeIsActive()) {
            if (gamepad1.dpad_up)   { power += 0.002; sleep(50); }
            if (gamepad1.dpad_down) { power -= 0.002; sleep(50); }
            if (gamepad1.b) power = 0;

            servo.setPower(power);
            telemetry.addData("power", power);
            telemetry.update();
        }
    }
}