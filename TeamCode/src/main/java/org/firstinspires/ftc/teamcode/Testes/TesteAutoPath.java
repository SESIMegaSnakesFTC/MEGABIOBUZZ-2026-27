package org.firstinspires.ftc.teamcode.Testes;


import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Autonomo.pedro.AutoPath;
import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;


@TeleOp(name = "Teste AutoPath", group = "Testes")
public class TesteAutoPath extends LinearOpMode {


    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;
    private boolean lastA = false, A_ON = false;
    private final TurnOn_TurnOff button = new TurnOn_TurnOff();
    private Pose lastPose;


    @Override
    public void runOpMode() {

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        double XtoAdjust = 0; double YtoAdjust = 0;
        Follower follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());
        follower.manual(0, 0, 0); // começa no manual, não IDLE

        AutoPath autoPath = new AutoPath(follower);

        waitForStart();

        while (opModeIsActive()) {



            // Pedro: + lateral = esquerda, + giro = anti-horário

            Pose CurrentPos = follower.pose();
            double y  = -gamepad1.left_stick_y;
            double x  = -gamepad1.left_stick_x;
            double rx = -gamepad1.right_stick_x;
            boolean A = gamepad1.a;

            A_ON = button.IsActivate(A, lastA, A_ON);
            if (A && !lastA){
                XtoAdjust = Math.round(follower.pose().x());
                YtoAdjust = Math.round(follower.pose().y());
                lastPose = CurrentPos;
            }
            boolean mexeu = Math.abs(y) > 0.1 || Math.abs(x) > 0.1 || Math.abs(rx) > 0.1;




            // A=L1 (RED_PARK), B=L2 (BLUE_PARK), resto sem coordenada
            autoPath.GoToPosition(
                    gamepad1.dpad_left, gamepad1.dpad_right, gamepad1.dpad_down, gamepad1.dpad_up,
                    false, false, false, false);

            // stick sempre vence o path
            if (mexeu || follower.manual()) follower.manual(y, x, rx);


            if (A_ON) {

                autoPath.HoldPosition(follower, lastPose, XtoAdjust/*Target*/, YtoAdjust/*Target*/,
                        follower.pose().x(), follower.pose().y());
            }
            else if(!gamepad1.dpad_left && !gamepad1.dpad_right
                    && !gamepad1.dpad_up && !gamepad1.dpad_down ) follower.manual(y,x,rx);

            lastA = A;

            follower.update();

            Pose pose = follower.pose();
            telemetry.addData("Modo", follower.following() ? "PATH" : "MANUAL");
            telemetry.addData("X", "%.1f", pose.x());
            telemetry.addData("Y", "%.1f", pose.y());
            telemetry.addData("Heading", "%.1f", Math.toDegrees(pose.heading()));
            telemetry.addData("left", gamepad1.dpad_left);
            telemetry.addData("right", gamepad1.dpad_right);
            telemetry.addData("Up", gamepad1.dpad_up);
            telemetry.addData("Down", gamepad1.dpad_down);
            telemetry.addLine("===============================");
            telemetry.addData("Travado", A_ON);
            telemetry.update();
        }
    }
}
