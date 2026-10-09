package org.firstinspires.ftc.teamcode.Testes;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.teamcode.Mech.Chassi;
import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.PinpointConf;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretController;
import org.firstinspires.ftc.teamcode.Mech.Turret.TurretTracking;
import org.firstinspires.ftc.teamcode.Mech.Turret.VisionPoseCorrector;

/*
 * Botoes:
 *   A = tracking da torreta (odometria)
 *   B = ajuste fino da mira pelo tx das tags (bias do TurretTracking)
 *   X = APLICAR correcao de pose pela visao. Desligado, o corretor so mede
 *       e mostra na telemetria (modo "dry run" para calibrar).
 */
@TeleOp(name = "TrackingOnChassi")
public class TestTrackingOnChassi extends LinearOpMode {

    private final TurnOn_TurnOff button = new TurnOn_TurnOff();
    private final LimeLight limeLight = new LimeLight();
    private final Chassi chassi = new Chassi();
    

    // Troque a posicao inicial aqui (RED_DOWN, RED_UP, BLUE_DOWN, BLUE_UP)
    private static final HiveTracking.StartPose START = HiveTracking.StartPose.RED_UP;


    public void runOpMode() {

        // Bulk caching: junta as leituras do Hub em um comando so por loop (loop bem mais rapido)
        for (LynxModule hub : hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        HiveTracking hive = new HiveTracking(START);

        PinpointLocalizer pimpas = PinpointConf.create(hardwareMap);
        pimpas.setPose(START.toPose());   // posicao + heading de CAMPO

        TurretController turret = new TurretController(hardwareMap.get(DcMotorEx.class, "turretMotor"));
        TurretTracking tracker = new TurretTracking(turret);
        VisionPoseCorrector corrector = new VisionPoseCorrector(pimpas);
        VoltageSensor vs = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next()
                : null;

        limeLight.init(hardwareMap);
        chassi.init(hardwareMap);

        // Liga a Limelight JA NO INIT, no pipeline da alianca + hive de partida.
        // Ela fica rodando o tempo todo; o B so decide se a visao corrige a odometria.
        boolean red = START.alliance == HiveTracking.Alliance.RED;
        limeLight.TurnON(red, hive.isSideUp());

        boolean lastA = false, A_ON = false;
        boolean lastB = false, B_ON = false;
        boolean lastX = false, X_ON = false;
        boolean TavaNOTracking = false;

        // Heading continuo (sem salto de 360 -> 0), usado so no feedforward
        pimpas.update();
        double lastRaw = Math.toDegrees(pimpas.pose().heading());
        double headingUnwrapped = lastRaw;

        // Bateria muda devagar: le a cada 250 ms (leitura lenta no Hub)
        double voltage = (vs != null) ? vs.getVoltage() : 12.0;
        long lastVoltMs = 0;
        long lastLoopNs = System.nanoTime();

        waitForStart();

        while (opModeIsActive()) {

            // >>> DRIVE TRAIN <<<
            double x = gamepad1.left_stick_x;
            double y = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;
            chassi.drive(x, y, rx);

            // >>> PINPOINT <<<
            pimpas.update();
            Pose p = pimpas.pose();
            double fx = p.x();
            double fy = p.y();
            double raw = Math.toDegrees(p.heading());   // heading de campo

            double d = raw - lastRaw;
            if (d > 180)  d -= 360;
            if (d < -180) d += 360;
            headingUnwrapped += d;
            lastRaw = raw;

            // >>> BOTOES <<<
            boolean A = gamepad1.a;
            boolean B = gamepad1.b;
            boolean X = gamepad1.x;

            A_ON = button.IsActivate(A, lastA, A_ON);
            B_ON = button.IsActivate(B, lastB, B_ON);
            X_ON = button.IsActivate(X, lastX, X_ON);

            // >>> LIMELIGHT <<< (pipeline segue a hive do loop anterior)
            limeLight.setSide(red, hive.isSideUp());
            LLResult llResult = limeLight.getResult();   // le uma vez por loop

            // >>> CORRECAO DE POSE PELA VISAO <<<
            // Angulo/velocidade da turret sao do loop anterior (ainda nao rodou turret.update)
            boolean corrigiu = corrector.update(llResult, fx, fy, raw,
                    turret.angleDeg, turret.velDegPerSec, X_ON);
            if (corrigiu) {
                fx = corrector.getCorrectedX();
                fy = corrector.getCorrectedY();
                raw = corrector.getCorrectedHeadingDeg();
                lastRaw = raw;                 // salto do heading nao entra no unwrapped
                tracker.onPoseCorrected();     // evita corrigir duas vezes (pose + bias)
            }

            double Heading = headingUnwrapped; // sinal de compensacao: TurretConfig.headingSign

            // >>> HIVE (SideToTrack + angulo pela odometria) <<<
            boolean sideUp = hive.update(fx, fy);
            double odoAngle = hive.turretAngleDeg(fx, fy, raw);

            if (A_ON && !TavaNOTracking) tracker.reset();
            TavaNOTracking = A_ON;

            long nowMs = System.currentTimeMillis();
            if (vs != null && nowMs - lastVoltMs > 250) {
                voltage = vs.getVoltage();
                lastVoltMs = nowMs;
            }

            // >>> TURRET <<<
            if (A_ON) {
                LLResult result = B_ON ? llResult : null;

                turret.enabled = true;
                tracker.updateHeading(Heading);
                tracker.update(result, hive, fx, fy, raw);
                turret.update(tracker.getTargetVelocity(), voltage);
            } else {
                turret.enabled = false;
                turret.manualPower = (gamepad1.right_trigger - gamepad1.left_trigger) / 2;
                turret.update(0, voltage);
            }

            boolean Locked = A_ON && tracker.isLocked();

            long loopNs = System.nanoTime();
            double loopMs = (loopNs - lastLoopNs) / 1e6;
            lastLoopNs = loopNs;

            telemetry.addData("Tracking (A) / Fino (B) / Pose (X)", "%s / %s / %s",
                    A_ON ? "ON" : "OFF", B_ON ? "ON" : "OFF", X_ON ? "ON" : "SO MEDE");
            telemetry.addData("Estado", tracker.state);
            telemetry.addData("Travado", Locked);
            telemetry.addData("SideToTrack", sideUp ? "CIMA" : "BAIXO");
            telemetry.addData("Pose odo", "%.1f, %.1f, %.1f", fx, fy, raw);
            telemetry.addData("Dist hive (eixo turret)", "%.1f", hive.distanceFromTurretToHive(fx, fy, raw));
            telemetry.addData("Angulo odo", "%.1f", odoAngle);
            telemetry.addData("Bias odo", "%.1f", tracker.getOdoBias());
            telemetry.addData("Angulo / alvo", "%.1f / %.1f", turret.angleDeg, turret.getTarget());
            telemetry.addData("Erro", "%.2f", turret.getError());
            telemetry.addData("Vel alvo / turret", "%.0f / %.0f", tracker.getTargetVelocity(), turret.velDegPerSec);
            telemetry.addData("Potencia", "%.3f", turret.getLastPower());
            telemetry.addData("Loop ms", "%.1f", loopMs);
            telemetry.addData("Tag usada / tx", "%d / %.1f", tracker.getLastTagId(), tracker.getLastTx());
            telemetry.addData("Tags na medida", tracker.getTagsUsadas());
            telemetry.addData("Pos da tag cadastrada", HiveTracking.hasTagPosition(tracker.getLastTagId())
                    ? "SIM" : "NAO (usando centro da hive)");

            // >>> CORRECAO DE POSE <<<
            telemetry.addData("Visao status", corrector.getStatus());
            telemetry.addData("Pose visao", "%.1f, %.1f", corrector.getVisionX(), corrector.getVisionY());
            telemetry.addData("Erro odo dx / dy / dyaw", "%.1f / %.1f / %.1f",
                    corrector.getInnovX(), corrector.getInnovY(), corrector.getYawErrDeg());
            telemetry.addData("Dist tags (in)", "%.0f", corrector.getTagDistIn());
            telemetry.addData("Janela / correcoes / rejeitadas", "%d / %d / %d",
                    corrector.getWindowSize(), corrector.getCorrections(), corrector.getRejections());
            telemetry.addData("Ultima correcao", "%.1f s (dx %.1f, dy %.1f)",
                    corrector.secondsSinceCorrection(), corrector.getLastAppliedDx(), corrector.getLastAppliedDy());
            telemetry.addData("Vel robo / giro", "%.0f in/s / %.0f deg/s",
                    corrector.getSpeedInPerSec(), corrector.getTurnDegPerSec());

            // >>> LIMELIGHT <<<
            telemetry.addData("LL conectada / rodando", "%b / %b", limeLight.isConnected(), limeLight.isRunning());
            telemetry.addData("LL pipeline pedido", limeLight.getRequestedPipeline());
            if (llResult == null) {
                telemetry.addData("Result", "null (sem dados da camera)");
            } else {
                StringBuilder ids = new StringBuilder();
                for (LLResultTypes.FiducialResult f : llResult.getFiducialResults()) {
                    ids.append(f.getFiducialId()).append(' ');
                }
                telemetry.addData("Result", llResult.isValid());
                telemetry.addData("LL pipeline ativo", llResult.getPipelineIndex());
                telemetry.addData("LL staleness ms", llResult.getStaleness());
                telemetry.addData("Tags vistas", ids.length() > 0 ? ids.toString() : "nenhuma");
            }
            telemetry.update();

            lastA = A;
            lastB = B;
            lastX = X;
        }
        limeLight.TurnOFF();
        turret.enabled = false;
        turret.manualPower = 0;
        if (vs != null) turret.update(0, vs.getVoltage());
    }
}