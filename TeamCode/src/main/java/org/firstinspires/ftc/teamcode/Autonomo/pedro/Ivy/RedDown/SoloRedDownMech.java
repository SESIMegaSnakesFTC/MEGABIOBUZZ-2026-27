package org.firstinspires.ftc.teamcode.Autonomo.pedro.Ivy.RedDown;

import static com.pedropathing.api.Paths.curve;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.commands.Commands.waitMs;
import static com.pedropathing.ivy.commands.Commands.waitUntil;
import static com.pedropathing.ivy.groups.Groups.parallel;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;
import static com.pedropathing.ivy.pedro.PedroCommands.hold;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.MidTake;
import org.firstinspires.ftc.teamcode.Mech.Servos.GateServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.HoodServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.RampServo;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.VisionPoseCorrector;
import org.firstinspires.ftc.teamcode.Mech.ValuesToShoot;




class SoloRedDownMechPath {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose start = poseFactory.of(56, 9, 90);
    private final Pose path1Start = poseFactory.of(56, 9, 180);
    private final Pose path1 = poseFactory.of(11.6, 9, 180);
    private final Pose point2 = poseFactory.of(46.395, 129.4878, 90);
    private final Pose point2Control1 = poseFactory.of(23.4091, 27.2561, 0);
    private final Pose point2Control2 = poseFactory.of(26.694, 121.6754, 0);
    private final Pose point3 = poseFactory.of(12.0749, 46.6729, 180);
    private final Pose point3Control1 = poseFactory.of(23.9949, 111.4923, 0);
    private final Pose point3Control2 = poseFactory.of(32.2113, 38.9763, 0);
    private final Pose point4 = poseFactory.of(57.0327, 14.5948, 180);
    private final Pose point5 = poseFactory.of(9.2, 96.6, 90);
    private final Pose point5Control1 = poseFactory.of(9.5, 38, 0);

    public Path path1() {
        return line(path1Start, path1).linear(path1Start, path1);
    }

    public Path path2() {
        return curve(path1, point2Control1, point2Control2, point2).linear(path1, point2);
    }

    public Path path3() {
        return curve(point2, point3Control1, point3Control2, point3).linear(point2, point3);
    }

    public Path path4() {
        return line(point3, point4).constant(point4);
    }

    public Path path5() {
        return curve(point4, point5Control1, point5).tangent();
    }
}


@Autonomous(name = "SoloRedDownWithMech", group = "Autonomous")
public class SoloRedDownMech extends OpMode {

    // Posição inicial
    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;

    //=============================================

    // Recursos de Mira
    private static boolean  USING_VISION   = true; // Equivalente ao B do TeleOp
    private static boolean CORRECTING_POSE = true; // Equivalente ao X do TeleOp
    private static final double GATE_DELAY_Ms    = 1300; // (Time) Shooter girando antes do gate abrir


    // Mech

    private Follower follower;
    private final SoloRedDownMechPath paths = new SoloRedDownMechPath();

    private final Feeder feeder       = new Feeder();
    private final MidTake midTake     = new MidTake();
    private final GateServo gateServo = new GateServo();
    private final HoodServo hoodServo = new HoodServo();
    private final RampServo rampServo = new RampServo();
    private final LimeLight limeLight = new LimeLight();
    private Shooter shooter;

        //Turret
    private HiveTracking hive;
    private TurretController turret;
    private TurretTracking tracker;
    private VisionPoseCorrector corrector;
    private VoltageSensor vs; // Para dar um bisu na bateria

    private boolean red;

    // Estado da Turret

    private boolean Tracking = true; // Turret ajeitando na hive
    private boolean Shooting = false; // MidTake e Shooter
    private boolean Feeding  = false; // Feeder fazendo as coisas de feeder né
    private final ElapsedTime shooterTime = new ElapsedTime();

    // Heading "desembrulhado"

    private double lastHeading, headUnwrapped;
    private double voltage = 12;
    private long lastVoltMs = 0;

    // Valores de Shooting

    private double TargetVelocity = 0;
    private double HoodPos = Double.NaN;
    private double initialHoodPos;
    LLResult llr;

    //============================================



    @Override
    public void init(){

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());

        vs = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next()
                : null;
        if (vs != null) voltage = vs.getVoltage();



        hive      = new HiveTracking(START);
        turret    = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        tracker   = new TurretTracking(turret);
        corrector = new VisionPoseCorrector((PinpointLocalizer) follower.localizer);
        shooter   = new Shooter();


        feeder.init(hardwareMap);
        midTake.init(hardwareMap);
        gateServo.init(hardwareMap);
        hoodServo.init(hardwareMap);
        rampServo.init(hardwareMap);
        rampServo.Position(false);
        shooter.init(hardwareMap);
        limeLight.init(hardwareMap);


        // Norteamento tracking
        red = START.alliance == HiveTracking.Alliance.RED;
        limeLight.TurnON(red, hive.isSideUp());
        headUnwrapped = lastHeading;

        // Posição inicial do hood

        initialHoodPos = feeder.getHoodPos();
    }

    @Override
    public void start(){

        tracker.reset();
        corrector.reset();


        schedule(

                //ATO 1
                parallel(
                        follow(follower, paths.path1()),
                        instant(() -> {Shooting = true; Feeding = true;})
                ),

                waitMs(300),

                //ATO 2
                parallel(
                        instant(() -> {Shooting = false; Feeding = false;
                            CORRECTING_POSE = false; USING_VISION = false; Flower(true);}),
                        follow(follower, paths.path2())
                ),

                waitUntil(() -> llr != null && llr.isValid()),

                //ATO 3
                parallel(
                        instant(() -> {Shooting = true; Feeding = true;}),
                        hold(follower),
                        instant(() -> {CORRECTING_POSE = true; USING_VISION = true;}),
                        waitMs(2000)
                ),

                //ATO 4
                parallel(
                        instant(() -> {Shooting = false; Feeding = false;
                            CORRECTING_POSE = false; USING_VISION = false;}),
                        follow(follower, paths.path3()),
                        instant(() -> Flower(false)),
                        waitMs(860),
                        instant(() -> Flower(true))
                ),

                //ATO 5
                sequential(
                        instant(() -> Feeding = true),
                        waitMs(2000),
                        instant(() -> {Feeding = false; Flower(false);}),
                        follow(follower, paths.path4())
                ),

                waitUntil(() -> llr != null && llr.isValid()),

                //ATO 6
                parallel(
                        instant(() -> Shooting = true),
                        hold(follower),
                        instant(() -> {CORRECTING_POSE = true; USING_VISION = false;}),
                        waitMs(800)
                ),

                //ATO 7
                sequential(
                        instant(() -> {Shooting = false; Feeding = false;}),
                        follow(follower, paths.path5()),
                        hold(follower)
                ));
    }

    @Override
    public void loop(){

        follower.update();
        Scheduler.execute();
        AtualizarMech();

    }

    @Override
    public void stop(){

        Tracking = false;
        turret.enabled = false;
        hoodServo.hoodAjust(initialHoodPos, feeder.getHoodPos()); // volta para ângulação de início
        limeLight.TurnOFF();
        turret.manualPower = 0;
        turret.update(0, voltage);

    }

    private boolean gateOpen(){
        return Shooting && shooterTime.milliseconds() > GATE_DELAY_Ms;
    }
    private void Flower(boolean way){
        rampServo.Position(way);

    }

    private void AtualizarMech(){

        Pose pose = follower.pose();
        double x = pose.x();
        double y = pose.y();
        double head = pose.heading();

        double diff = head - lastHeading;
        if (diff >  180) diff -= 360;
        if (diff < -180) diff += 360;

        headUnwrapped += diff;
        lastHeading = head;

        // Leitura da bateria com limitador de 250Ms
        long nowMs = System.currentTimeMillis();
        if (vs != null && nowMs - lastVoltMs > 250){
            voltage = vs.getVoltage();
            lastVoltMs = nowMs;
        }

        // Limelight
        limeLight.setSide(red, hive.isSideUp());
        llr = limeLight.getResult();

        // Correção da pose bitch

        if (corrector.update(llr, x, y,head, turret.angleDeg, corrector.getTurnDegPerSec(), CORRECTING_POSE)){
            x = corrector.getCorrectedX();
            y = corrector.getCorrectedY();
            head = corrector.getCorrectedHeadingDeg();
            lastHeading = head;
            tracker.onPoseCorrected();
        }
        hive.update(x, y);

        //Turret
        if (Tracking){
            turret.enabled = true;
            tracker.updateHeading(headUnwrapped);//É a soma das diff's
            tracker.update(USING_VISION ? llr : null, hive, x, y, head);
            turret.update(tracker.getTargetVelocity(), voltage);
        }else{
            turret.enabled = false;
            turret.manualPower = 0;
            turret.update(0, voltage);
        }

        //Valores para dar uns tiros brabos(só que não)

        double distance = limeLight.getDistance();
        double newMotor = ValuesToShoot.motorPower(distance);
        double newHood  = ValuesToShoot.servoPosition(distance);

        if (!Double.isNaN(newHood)) HoodPos = newHood;
        if (!Double.isNaN(newMotor)) TargetVelocity = newMotor;

        if (Tracking) hoodServo.hoodAjust(HoodPos, feeder.getHoodPos());
        else hoodServo.stop();


        // MIDTAKE | SHOOTER | GATE | FEEDER

        if (!Shooting) shooterTime.reset();

        shooter.Activate(Shooting, TargetVelocity);
        midTake.spin(false, Shooting);
        gateServo.ShooterIsWorking(gateOpen()); // função
        feeder.activate(Feeding, false);
    }
}
