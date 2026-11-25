package org.firstinspires.ftc.teamcode.Autonomous.pedro.Ivy.RED;

import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.*;
import static com.pedropathing.ivy.groups.Groups.*;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.hardware.limelightvision.LLResult;
import org.firstinspires.ftc.teamcode.Mech.ValuesToShoot;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Autonomous.pedro.Constants;
import org.firstinspires.ftc.teamcode.Mech.Color_Sensor;
import org.firstinspires.ftc.teamcode.Mech.Feeder;
import org.firstinspires.ftc.teamcode.Mech.Led.RGB_Indicator;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.MidTake;
import org.firstinspires.ftc.teamcode.Mech.Servos.GateServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.HoodServo;
import org.firstinspires.ftc.teamcode.Mech.Servos.LeftRampServo;
import org.firstinspires.ftc.teamcode.Mech.Shooter;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.VisionPoseCorrector;

import com.pedropathing.api.Paths;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

class REDPath {

    private final PoseFactory poseFactory = PoseFactory.degrees();


    //CONSTANT
    public final Pose start = poseFactory.of(57.8474, 8.5, 90);
    private final Pose path1 = poseFactory.of(8.727, 8.48, 180);
    private final Pose point2 = poseFactory.of(34, 21, 180);


    //DEFINED BY LIMELIGHT

    

    public Path path1() {
        return Paths.line(start, path1).constant(path1);
    }

    public Path path2() {
        return Paths.line(path1, point2).constant(point2);
    }
}


@Disabled
@Autonomous(name = "AutoRED", group = "Autonomous")
public class RED extends OpMode {


    public static Pose finalPose;

    //TURRET ADJUST -> Has to be equals to first path localization
    private static final HiveTracking.StartPose START_POSE = HiveTracking.StartPose.RED_DOWN;

    //SHOOTING
    private final boolean USING_LIMELIGHT = true;
    private boolean CORRECT_ODO = true;
    private final float TIME_GATE = 0.7f;

    private Follower follower;
    private final REDPath paths = new REDPath();

    //MECH
    private LeftRampServo ramp  = new LeftRampServo();
    private Feeder feeder   = new Feeder();
    private MidTake midTake = new MidTake();
    private GateServo gate  = new GateServo();
    private HoodServo hood  = new HoodServo();
    private RGB_Indicator indicator = new RGB_Indicator();
    private Color_Sensor sensor = new Color_Sensor();
    private LimeLight limeLight = new LimeLight();


    private Shooter shooter = new Shooter();


    private HiveTracking hive;
    private TurretController turret;
    private TurretTracking tracker;
    private VisionPoseCorrector vision;
    private VoltageSensor vs;

    private boolean red;


    //STATUS

    private boolean Tracking = true;
    private boolean Feeding  = false;
    private boolean Shooting = false;
    private ElapsedTime timer = new ElapsedTime();

    private double lastHead, headUnwrapped;
    private double voltage = 12.2;
    private boolean rampOpen = false;
    private boolean correcting = false, corrected = false;
    private final ElapsedTime corrTimer = new ElapsedTime();
    private double motorPotence = 0;
    private double targetHood = Double.NaN;
    private long lastMsVolt = 0;


    @Override
    public void init(){

        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Scheduler.reset();

        follower = Constants.create(hardwareMap);
        follower.setPose(paths.start);
        follower.update();

        vs = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next()
                : null;

        if (vs != null) voltage = vs.getVoltage();

        //TURRET

        hive = new HiveTracking(START_POSE);
        turret = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        tracker = new TurretTracking(turret);
        vision = new VisionPoseCorrector((PinpointLocalizer) follower.localizer);
        shooter = new Shooter(vs);

        feeder.init(hardwareMap);
        midTake.init(hardwareMap);
        ramp.init(hardwareMap);
        hood.init(hardwareMap);
        gate.init(hardwareMap);
        shooter.init(hardwareMap);
        limeLight.init(hardwareMap);
        indicator.init(hardwareMap);
        sensor.init(hardwareMap, 0.5f);

        red = START_POSE.alliance == HiveTracking.Alliance.RED;
        limeLight.TurnON(red, hive.isSideUp());

        lastHead = Math.toDegrees(START_POSE.toPose().heading());
        headUnwrapped = lastHead;
    }

    @Override
    public void start(){
        tracker.reset();
        vision.reset();
        sensor.ResetBallCounter();

        // Sequência: atira (path1 inteiro, fim do path3 e fim do path4)
        // Shooter só fica ligado nos tiros e desliga no fim de cada um
        schedule(sequential(
                instant(() -> { Shooting = true; Feeding = true; }),
                follow(follower, paths.path1()),
                fimTiro(),
                parallel(                                  // rampa abre 1 s depois do início do path2
                        follow(follower, paths.path2()),
                        sequential(waitMs(1000), instant(() -> rampOpen = true))
                ),
                instant(() -> Shooting = true),            // acelera durante o path3
                //follow(follower, paths.path3()),
                corrigirPose(),
                atirar(2500),
                instant(() -> { Shooting = true; rampOpen = false; }),   // acelera e fecha a rampa no path4
                //follow(follower, paths.path4()),
                corrigirPose(),
                atirar(1000),
                //follow(follower, paths.path5()),
                instant(() -> { Tracking = false; finalPose = follower.pose(); })
        ));
    }

    @Override
    public void loop(){
        follower.update();
        Scheduler.execute();
        updateMech();

    }

    @Override
    public void stop(){
        finalPose = follower.pose();   // para a TeleOp pegar a pose final
        limeLight.TurnOFF();
        hood.stop();
        turret.enabled = false;
        turret.manualPower = 0;
        turret.update(0, voltage);
    }


    // ---------- Comandos ----------

    /** Espera o gate, alimenta por "ms" e encerra o tiro. */
    private Command atirar(double ms){
        return sequential(
                waitUntil(this::gateOpen),
                instant(() -> Feeding = true),
                waitMs(ms),
                fimTiro()
        );
    }

    /** Para feeder e shooter e zera o contador de bolas. */
    private Command fimTiro(){
        return instant(() -> { Feeding = false; Shooting = false; sensor.ResetBallCounter(); });
    }

    /** Liga a correção e espera aplicar uma vez (timeout de 1 s se não ver tag). */
    private Command corrigirPose(){
        return sequential(
                instant(() -> { corrected = false; correcting = true; corrTimer.reset(); }),
                waitUntil(() -> corrected || corrTimer.seconds() > 1.0),
                instant(() -> correcting = false)
        );
    }

    private boolean gateOpen(){
        return Shooting && timer.seconds() > TIME_GATE;
    }


    // ---------- Mecanismos (todo loop) ----------

    private void updateMech(){

        Pose pose = follower.pose();
        double x = pose.x();
        double y = pose.y();
        double head = Math.toDegrees(pose.heading());

        double diff = head - lastHead;
        if (diff >  180) diff -= 360;
        if (diff < -180) diff += 360;
        headUnwrapped += diff;
        lastHead = head;

        // Bateria (a cada 250 ms)
        long nowMs = System.currentTimeMillis();
        if (vs != null && nowMs - lastMsVolt > 250) {
            voltage = vs.getVoltage();
            lastMsVolt = nowMs;
        }

        limeLight.setSide(red, hive.isSideUp());
        LLResult ll = limeLight.getResult();

        // Correção de pose com Limelight
        if (vision.update(ll, x, y, head, turret.angleDeg, vision.getTurnDegPerSec(), CORRECT_ODO && correcting)) {
            corrected = true;
            x = vision.getCorrectedX();
            y = vision.getCorrectedY();
            head = vision.getCorrectedHeadingDeg();
            lastHead = head;
            tracker.onPoseCorrected();
        }

        hive.update(x, y);

        // Turret
        if (Tracking) {
            turret.enabled = true;
            tracker.updateHeading(headUnwrapped);
            tracker.update(USING_LIMELIGHT ? ll : null, hive, x, y, head);
            turret.update(tracker.getTargetVelocity(), voltage);
        } else {
            turret.enabled = false;
            turret.manualPower = 0;
            turret.update(0, voltage);
        }

        // Valores de tiro (segura o último válido)
        double distance = limeLight.getDistance();
        double newMotor = ValuesToShoot.motorPower(distance);
        double newHood  = ValuesToShoot.servoPosition(distance);
        if (!Double.isNaN(newMotor)) motorPotence = newMotor;
        if (!Double.isNaN(newHood))  targetHood = newHood;

        if (Tracking) hood.hoodAjust(targetHood, feeder.getHoodPos());
        else hood.stop();

        // Shooter | MidTake | Gate | Feeder
        if (!Shooting) timer.reset();   // timer conta desde que o shooter ligou
        shooter.Activate(Shooting, motorPotence);
        midTake.spin(false, Shooting);
        gate.ShooterIsWorking(gateOpen());
        ramp.Position(rampOpen);
        feeder.activate(Feeding && gateOpen(), false);   // só alimenta com o gate aberto

        // Contador de bolas -> cor do indicador
        indicator.setColorBasedInNumber(sensor.CountBall(false, 35, 75, 2));
    }
}
