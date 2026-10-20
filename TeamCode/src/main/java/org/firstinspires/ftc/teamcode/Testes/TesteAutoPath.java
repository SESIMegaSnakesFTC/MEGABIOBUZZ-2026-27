package org.firstinspires.ftc.teamcode.Testes;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Autonomo.pedro.AutoPath;
import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;

@TeleOp(name = "Teste AutoPath", group = "Testes")
public class TesteAutoPath extends LinearOpMode {

    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;

    @Override
    public void runOpMode() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Follower follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());
        follower.manual(0, 0, 0); // começa no manual, não IDLE

        AutoPath autoPath = new AutoPath(follower);

        waitForStart();

        while (opModeIsActive()) {
            // Pedro: + lateral = esquerda, + giro = anti-horário
            double y  = -gamepad1.left_stick_y;
            double x  =  gamepad1.left_stick_x;
            double rx =  gamepad1.right_stick_x;
            boolean mexeu = Math.abs(y) > 0.1 || Math.abs(x) > 0.1 || Math.abs(rx) > 0.1;

            // A=L1 (RED_PARK), B=L2 (BLUE_PARK), resto sem coordenada
            autoPath.GoToPosition(
                    gamepad1.a, gamepad1.b, gamepad1.x, gamepad1.y,
                    gamepad1.dpad_up, gamepad1.dpad_down, gamepad1.dpad_left, gamepad1.dpad_right);

            // stick sempre vence o path
            if (mexeu || follower.manual()) follower.manual(y, x, rx);

            follower.update();

            Pose pose = follower.pose();
            telemetry.addData("Modo", follower.following() ? "PATH" : "MANUAL");
            telemetry.addData("X", "%.1f", pose.x());
            telemetry.addData("Y", "%.1f", pose.y());
            telemetry.addData("Heading", "%.1f", Math.toDegrees(pose.heading()));
            telemetry.update();
        }
    }
}
