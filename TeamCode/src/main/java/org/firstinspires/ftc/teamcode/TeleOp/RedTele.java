package org.firstinspires.ftc.teamcode.TeleOp;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Autonomous.pedro.AutoPath;
import org.firstinspires.ftc.teamcode.Autonomous.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.Color_Sensor;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.FieldCentric;
import org.firstinspires.ftc.teamcode.Mech.Led.RGB_Indicator;
import org.firstinspires.ftc.teamcode.Mech.Servos.LeftRampServo;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;

@TeleOp(name = "RedTele", group = "TeleOp")
public class RedTele extends LinearOpMode {

    //INSTANCE
    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;
    Follower follower;
    Feeder   feeder = new Feeder();
    LeftRampServo ramp  = new LeftRampServo();
    AutoPath autoPath;
    TurnOn_TurnOff button = new TurnOn_TurnOff();
    RGB_Indicator indicator = new RGB_Indicator();
    Color_Sensor sensor = new Color_Sensor();


    //BUTTON'S

    boolean lastRB = false, FLOWER_DOWN = false;
    boolean lastReset = false;
    boolean lastHold = false, Hold = true;
    boolean lastMode = false, Mode = false;

    //====================================

    double holdX;
    double holdY;
    Pose holdPos;

    int Quant = 0;


    @Override
    public void runOpMode(){

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }



        //INIT

        feeder.init(hardwareMap);
        ramp.init(hardwareMap);
        follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());
        follower.manual(0,0,0);
        follower.update();

        autoPath = new AutoPath(follower);
        FieldCentric fieldCentric = new FieldCentric(follower);
        fieldCentric.resetHeading();

        indicator.init(hardwareMap);
        indicator.setColorBasedInNumber(0);
        sensor.init(hardwareMap, 0.5f);


        waitForStart();
        while(opModeIsActive()){


            double x  = gamepad1.left_stick_x;
            double y  = gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;
            Pose pose =  follower.pose();


            // TURN ON/OFF

            FLOWER_DOWN = button.IsActivate(gamepad1.left_bumper, lastRB, FLOWER_DOWN);
            lastRB = FLOWER_DOWN;

            Mode  = button.IsActivate(gamepad1.b, lastMode, Mode);
            lastMode = gamepad1.b;

            Hold  = button.IsActivate(gamepad1.a, lastHold, Hold);


            boolean reset = gamepad1.options && !lastReset;
            lastReset = gamepad1.options;


            if (gamepad1.a && !lastHold){
                holdPos = pose;
                holdX   = Math.round(pose.x());
                holdY   = Math.round(pose.y());
            } lastHold  = gamepad1.a;


            boolean moved = Math.abs(x) > 0.1 || Math.abs(y) > 0.1 || Math.abs(rx) > 0.1;
            boolean PosPressed = gamepad1.dpad_left || gamepad1.dpad_right
                    || gamepad1.dpad_down || gamepad1.dpad_up;



            if (moved){
                DrivePowers p = Mode ? fieldCentric.powers(x, y, rx) : new DrivePowers(y, x, rx);
                follower.manual(p);
            }
            else if (Hold && holdPos != null) autoPath.HoldPosition(follower, holdPos, holdX, holdY, pose.x(), pose.y());

            else if (!PosPressed && !follower.following()) follower.manual(0,0,0);

            else autoPath.GoToPosition(false, gamepad1.dpad_right,
                        gamepad1.dpad_down, gamepad1.dpad_up,
                        false, false, false, false );

            if (reset) fieldCentric.resetHeading();




            follower.update();

            feeder.activate(gamepad1.y, gamepad1.right_bumper);

            //BALL's COUNTER


            Quant = sensor.CountBall(false, 35, 75, 2);
            if (gamepad1.left_stick_button) sensor.ResetBallCounter();

            //COLOR BASED IN BALL's COUNTER

            indicator.setColorBasedInNumber(Quant);


            //FLOWER SERVO
            ramp.Position(FLOWER_DOWN);




            telemetry.addData("Mode", Mode ? "FieldCentric" : "Normal");
            telemetry.addData("Holding", Hold);
            telemetry.addData("BALL", Quant);
            telemetry.addLine("====================================");
            telemetry.addLine("L3 para reset");
            telemetry.addLine("====================================");
            telemetry.addData("Servo Position", FLOWER_DOWN ? 1 : 0);
            telemetry.update();



        }
    }

}
