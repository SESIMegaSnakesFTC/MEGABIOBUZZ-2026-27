package org.firstinspires.ftc.teamcode.Testes;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Autonomo.pedro.AutoPath;
import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.FieldCentric;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;

@TeleOp(name = "AutoPath&FieldCentric", group = "Testes")
public class TestFieldAutoPath extends LinearOpMode {

    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;

    private final TurnOn_TurnOff holdButton = new TurnOn_TurnOff();
    private final TurnOn_TurnOff driveModeButton = new TurnOn_TurnOff();

    private boolean holdEnabled = false;
    private boolean fieldCentricEnabled = false;
    private boolean lastA = false;
    private boolean lastB = false;
    private boolean lastOptions = false;
    private Pose holdPose;
    private double holdX;
    private double holdY;

    @Override
    public void runOpMode() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Follower follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());
        follower.manual(0, 0, 0);

        FieldCentric field = new FieldCentric(follower);
        field.resetHeading();
        AutoPath autoPath = new AutoPath(follower);

        telemetry.addData("Status", "Pronto");
        telemetry.addData("Controles", "A: trava posicao | B: FIELD/ROBOT | options: reseta heading");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            double x = gamepad1.left_stick_x;
            double y = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;
            Pose pose = follower.pose();

            boolean options = gamepad1.options;
            if (options && !lastOptions) field.resetHeading();
            lastOptions = options;

            boolean a = gamepad1.a;
            holdEnabled = holdButton.IsActivate(a, lastA, holdEnabled);
            if (a && !lastA) {
                holdPose = pose;
                holdX = Math.round(pose.x());
                holdY = Math.round(pose.y());
            }
            lastA = a;

            boolean b = gamepad1.b;
            fieldCentricEnabled = driveModeButton.IsActivate(b, lastB, fieldCentricEnabled);
            lastB = b;

            boolean dpadPressed = gamepad1.dpad_left || gamepad1.dpad_right
                    || gamepad1.dpad_up || gamepad1.dpad_down;
            boolean stickMoved = Math.abs(x) > 0.1 || Math.abs(y) > 0.1 || Math.abs(rx) > 0.1;

            // Dispara paths nos botoes direcionais. O stick pode interromper o path.
            autoPath.GoToPosition(
                    gamepad1.dpad_left, gamepad1.dpad_right, gamepad1.dpad_down, gamepad1.dpad_up,
                    false, false, false, false);

            if (holdEnabled && holdPose != null) {
                autoPath.HoldPosition(follower, holdPose, holdX, holdY, pose.x(), pose.y());
            } else if (stickMoved) {
                DrivePowers powers = fieldCentricEnabled
                        ? field.powers(-x, y, -rx)
                        : new DrivePowers(y, -x, -rx);
                follower.manual(powers);
            } else if (!dpadPressed && !follower.following()) {
                // Mantem o follower em manual parado quando nao ha path nem comando.
                follower.manual(0, 0, 0);
            }

            follower.update();

            Pose current = follower.pose();
            telemetry.addData("Modo", follower.following() ? "PATH" : "MANUAL");
            telemetry.addData("Drive", fieldCentricEnabled ? "FIELD" : "ROBOT");
            telemetry.addData("X / Y", "%.1f / %.1f", current.x(), current.y());
            telemetry.addData("Heading", "%.1f", Math.toDegrees(current.heading()));
            telemetry.addData("Travado", holdEnabled ? "Sim" : "Nao");
            telemetry.addData("D-pad", "L:%s R:%s U:%s D:%s",
                    gamepad1.dpad_left, gamepad1.dpad_right,
                    gamepad1.dpad_up, gamepad1.dpad_down);
            telemetry.update();
        }
    }
}
