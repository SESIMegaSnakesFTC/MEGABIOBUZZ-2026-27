package org.firstinspires.ftc.teamcode.TeleOperados;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.MidTake;
import org.firstinspires.ftc.teamcode.Mech.PinpointConf;
import org.firstinspires.ftc.teamcode.Mech.Servos.HoodServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.RampServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.GateServo;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.VisionPoseCorrector;
import org.firstinspires.ftc.teamcode.Mech.ValuesToShoot;





@TeleOp(name = "TeleOp RED", group = "TeleOp")
public class TeleOp_RED extends LinearOpMode {


    private final Chassi drivetrain   = new Chassi();
    private final Feeder feeder       = new Feeder();
    private final MidTake midtake     = new MidTake();
    private final GateServo gateServo = new GateServo();
    private final HoodServo hoodServo = new HoodServo();
    private final RampServo rampServo = new RampServo();
    private final LimeLight limeLight = new LimeLight();


    //Logic Button
    private final TurnOn_TurnOff button = new TurnOn_TurnOff();

    //Midtake

    boolean lastRT = false, RT_ON = false;

    ElapsedTime timer;


    //Turret | Limelight | Odometry

    /** >>>  Tudo no player 1  <<< **/

    final boolean A_ON = true;
    final boolean B_ON = false;
    boolean WasOnTracking = false;


    /**Isso aqui vai definir o local de início do robô
     --> localiza a hive com base na odometria (falta adicionar os locais de park)**/
    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_PARK;


    @Override
    public void runOpMode(){

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }


        //Turret and Pinpoint initialize
        HiveTracking hives            = new HiveTracking(START);
        PinpointLocalizer pinpoint    = PinpointConf.create(hardwareMap);
        pinpoint.setPose(START.toPose());
        TurretController controller   = new TurretController(hardwareMap.get(DcMotorEx.class,"turretMotor"));
        TurretTracking tracker        = new TurretTracking(controller);
        VisionPoseCorrector corrector = new VisionPoseCorrector(pinpoint);
        VoltageSensor voltageSensor   = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next() : null;


        //iniciar o shooter aqui mesmo, sutileza
        final Shooter shooter         = new Shooter(voltageSensor);
        shooter.init(hardwareMap);


        //Localização sendo passada

        boolean red = START.alliance == HiveTracking.Alliance.RED;
        pinpoint.update();
        double lastHead = Math.toDegrees(pinpoint.pose().heading());
        double headBefore = lastHead;
        double voltage  = voltageSensor != null ? voltageSensor.getVoltage() : 12;
        long lastVoltMs = 0;



        //Initializing the robot controller mechanisms
        initialize();
        limeLight.TurnON(red, hives.isSideUp());

        waitForStart();

        // Ultimos valores validos da tabela (NaN = ainda sem alvo -> hood parado)
        double MotorPotence = 0;
        double TargethoodPosition = Double.NaN;

        while(opModeIsActive()){


            // Values to shoot

            // Se a tabela der NaN, segura o ultimo valor valido
            double distance = limeLight.getDistance();
            double newMotor = ValuesToShoot.motorPower(distance);
            double newHood  = ValuesToShoot.servoPosition(distance);
            if (!Double.isNaN(newMotor)) MotorPotence = newMotor;
            if (!Double.isNaN(newHood))  TargethoodPosition = newHood;


            // Drivetrain

            double x  = gamepad1.left_stick_x;
            double y  = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;

            drivetrain.drive(x, y, rx);

            //Pinpoint
            pinpoint.update();
            Pose pose = pinpoint.pose();
            double PosX = pose.x();
            double PosY = pose.y();
            double head = Math.toDegrees(pose.heading());

            double diff = head - lastHead;
            if (diff >  180) diff -= 360;
            if (diff < -180) diff += 360;
            headBefore += diff;
            lastHead = head;


            //Button for tracking adjust


            boolean X = gamepad1.x;




            // Limelight

            limeLight.setSide(red, hives.isSideUp());
            LLResult llresultado = limeLight.getResult();


            //Correcting the pose of pod's using the limelight result
            boolean correct = corrector.update(llresultado, PosX, PosY, head, controller.angleDeg,
                    corrector.getTurnDegPerSec(), X);

            if (correct){
                PosX = corrector.getCorrectedX();
                PosY = corrector.getCorrectedY();
                head = corrector.getCorrectedHeadingDeg();
                lastHead = head;
                tracker.onPoseCorrected();
            }
            double Heading = headBefore;



            // HIVE
            hives.update(PosX, PosY);


            if (A_ON && !WasOnTracking) tracker.reset();
            WasOnTracking = A_ON;

            long nowMs = System.currentTimeMillis();
            if (voltageSensor != null && nowMs - lastVoltMs > 250){
                voltage = voltageSensor.getVoltage();
                lastVoltMs = nowMs;
            }

            // Turret

            if (A_ON){

                LLResult result = B_ON ? llresultado : null; //filtrozin

                controller.enabled = true;
                tracker.updateHeading(Heading);
                tracker.update(result, hives, PosX, PosY, head);
                controller.update(tracker.getTargetVelocity(), voltage);


            }else{
                controller.enabled = false;
                controller.manualPower = (gamepad1.right_trigger - gamepad1.left_trigger)/2;
                controller.update(0, voltage);
            }

            boolean Locked = A_ON && tracker.isLocked();



            // Feeder

            feeder.activate(gamepad2.left_trigger > 0.5, gamepad2.left_bumper);


            // MidTake | Shooter

                //Hood Correction

            if (A_ON)hoodServo.hoodAjust(TargethoodPosition,feeder.getHoodPos());
            else hoodServo.stop(); //para


            boolean RT = gamepad2.right_trigger > 0.5;
            RT_ON = button.IsActivate(RT, lastRT, RT_ON);
            midtake.spin(false, RT_ON);
            shooter.Activate(RT_ON, MotorPotence);

            if (RT_ON && !lastRT) timer = new ElapsedTime();

            lastRT = RT;


            //Gate Servo

            boolean Open = timer != null && RT_ON && timer.seconds() > 1.5;
            gateServo.ShooterIsWorking(Open);//ToDo: falta testar valores de abertura



            telemetry.addData("Turret Travada", Locked);

        }
        limeLight.TurnOFF();
        hoodServo.stop();
        controller.enabled = false;
        controller.manualPower = 0;
        if (voltageSensor != null) controller.update(0, voltageSensor.getVoltage());

    }
    public void initialize(){
        drivetrain.init(hardwareMap);
        feeder.init(hardwareMap);
        midtake.init(hardwareMap);
        gateServo.init(hardwareMap);
        hoodServo.init(hardwareMap);
        rampServo.init(hardwareMap);
        limeLight.init(hardwareMap);
    }
}
