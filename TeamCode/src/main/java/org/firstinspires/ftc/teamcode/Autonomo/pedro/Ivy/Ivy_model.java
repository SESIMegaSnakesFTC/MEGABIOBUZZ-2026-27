package org.firstinspires.ftc.teamcode.Autonomo.pedro.Ivy;

import static com.pedropathing.api.Paths.*;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.commands.Commands.*;
import static com.pedropathing.ivy.groups.Groups.*;
import static com.pedropathing.ivy.pedro.PedroCommands.follow;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
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


// Caminhos do modelo: troque as poses ao copiar
class PathsIvyModel {

    private final PoseFactory p = PoseFactory.degrees();

    private final Pose start = p.of(56, 9, 180);
    private final Pose score = p.of(57, 14.6, 180);
    private final Pose park  = p.of(11.6, 9, 180);

    public Path toScore() {
        return line(start, score).constant(score);
    }

    public Path toPark() {
        return line(score, park).constant(park);
    }
}


/**
 * MODELO de autônomo Ivy com mecanismos (copiar para criar novos autônomos).
 * Mecanismos rodam todo loop em atualizarMecanismos(); a sequência do Ivy só liga/desliga flags.
 */
@Disabled
@Autonomous(name = "Ivy_model", group = "Pedro")
public class Ivy_model extends OpMode {

    // Pose inicial (deve bater com o primeiro ponto do primeiro caminho)
    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_DOWN;

    // Liga/desliga recursos de mira
    private static final boolean USAR_VISAO    = true;  // Limelight no tracking (B da TeleOp)
    private static final boolean CORRIGIR_POSE = true;  // corrige odometria com Limelight (X da TeleOp)
    private static final double  GATE_DELAY_S  = 1.5;   // tempo do shooter acelerar antes de abrir o gate

    private Follower follower;
    private final PathsIvyModel paths = new PathsIvyModel();

    private final Feeder    feeder    = new Feeder();
    private final MidTake   midTake   = new MidTake();
    private final GateServo gateServo = new GateServo();
    private final HoodServo hoodServo = new HoodServo();
    private final RampServo rampServo = new RampServo();
    private final LimeLight limeLight = new LimeLight();
    private Shooter shooter;

    private HiveTracking hive;
    private TurretController turret;
    private TurretTracking tracker;
    private VisionPoseCorrector corrector;
    private VoltageSensor vs;

    private boolean red;

    // Estado controlado pela sequência
    private boolean rastrear   = true;   // turret mira na hive
    private boolean atirando   = false;  // shooter + midtake ligados
    private boolean alimentando = false; // feeder puxando
    private final ElapsedTime shooterTimer = new ElapsedTime();

    // Heading desembrulhado (feedforward da turret)
    private double lastHead, headUnwrapped;
    private double voltage = 12;
    private long lastVoltMs = 0;

    // Últimos valores válidos da tabela (NaN = sem alvo -> hood parado)
    private double motorPotence = 0;
    private double targetHood = Double.NaN;


    @Override
    public void init() {
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        Scheduler.reset();
        follower = Constants.create(hardwareMap);
        follower.setPose(START.toPose());

        vs = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next() : null;
        if (vs != null) voltage = vs.getVoltage();

        // Usa o Pinpoint do follower (não criar outro)
        hive      = new HiveTracking(START);
        turret    = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        tracker   = new TurretTracking(turret);
        corrector = new VisionPoseCorrector((PinpointLocalizer) follower.localizer);
        shooter   = new Shooter(vs);

        feeder.init(hardwareMap);
        midTake.init(hardwareMap);
        gateServo.init(hardwareMap);
        hoodServo.init(hardwareMap);
        rampServo.init(hardwareMap);
        shooter.init(hardwareMap);
        limeLight.init(hardwareMap);

        red = START.alliance == HiveTracking.Alliance.RED;
        limeLight.TurnON(red, hive.isSideUp());

        lastHead = Math.toDegrees(START.toPose().heading());
        headUnwrapped = lastHead;
    }

    @Override
    public void start() {
        tracker.reset();
        corrector.reset();

        // Sequência: edite aqui
        schedule(sequential(
                instant(() -> atirando = true),          // já acelera o shooter no caminho
                follow(follower, paths.toScore()),
                atirar(2000),
                follow(follower, paths.toPark()),
                instant(() -> rastrear = false)
        ));
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();
        atualizarMecanismos();

        telemetry.addData("Turret Travada", rastrear && tracker.isLocked());
        telemetry.addData("Correção", corrector.getStatus());
        telemetry.update();
    }

    @Override
    public void stop() {
        limeLight.TurnOFF();
        hoodServo.stop();
        turret.enabled = false;
        turret.manualPower = 0;
        turret.update(0, voltage);
    }


    // ---------- Comandos para a sequência ----------

    /** Liga shooter, alimenta por "ms" e desliga tudo. */
    private Command atirar(double ms) {
        return sequential(
                instant(() -> atirando = true),
                waitUntil(this::gateAberto),
                instant(() -> alimentando = true),
                waitMs(ms),
                instant(() -> { alimentando = false; atirando = false; })
        );
    }

    private boolean gateAberto() {
        return atirando && shooterTimer.seconds() > GATE_DELAY_S;
    }




    private void atualizarMecanismos() {

        // Pose (follower.update() já atualizou o Pinpoint)
        Pose pose = follower.pose();
        double x = pose.x();
        double y = pose.y();
        double head = Math.toDegrees(pose.heading());

        double diff = head - lastHead;
        if (diff >  180) diff -= 360;
        if (diff < -180) diff += 360;
        headUnwrapped += diff;
        lastHead = head;

        // Bateria (lê a cada 250 ms)
        long nowMs = System.currentTimeMillis();
        if (vs != null && nowMs - lastVoltMs > 250) {
            voltage = vs.getVoltage();
            lastVoltMs = nowMs;
        }

        // Limelight
        limeLight.setSide(red, hive.isSideUp());
        LLResult ll = limeLight.getResult();

        // Correção de pose (aplica direto no localizer do follower)
        if (corrector.update(ll, x, y, head, turret.angleDeg, corrector.getTurnDegPerSec(), CORRIGIR_POSE)) {
            x = corrector.getCorrectedX();
            y = corrector.getCorrectedY();
            head = corrector.getCorrectedHeadingDeg();
            lastHead = head;
            tracker.onPoseCorrected();
        }

        hive.update(x, y);

        // Turret
        if (rastrear) {
            turret.enabled = true;
            tracker.updateHeading(headUnwrapped);
            tracker.update(USAR_VISAO ? ll : null, hive, x, y, head);
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

        if (rastrear) hoodServo.hoodAjust(targetHood, feeder.getHoodPos());
        else hoodServo.stop();

        // Shooter | MidTake | Gate | Feeder
        if (!atirando) shooterTimer.reset();   // timer conta desde que o shooter ligou
        shooter.Activate(atirando, motorPotence);
        midTake.spin(false, atirando);
        gateServo.ShooterIsWorking(gateAberto());
        feeder.activate(alimentando, false);
    }
}
