package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Corrige a pose do Pinpoint com a pose de campo (botpose, MegaTag1) da Limelight.
 *
 * Como funciona:
 *  1. Todo loop guarda (tempo, pose da odometria, angulo da torreta) num historico de 1 s.
 *  2. Quando chega um frame NOVO e bom, volta no historico ate o instante da foto
 *     (compensa latencia), transforma a pose da CAMERA em pose do ROBO
 *     (passando pela torreta, se a camera estiver nela) e calcula o erro da odometria.
 *  3. Filtros: idade, numero de tags, distancia, robo/torreta parados, dentro do campo,
 *     yaw coerente, salto maximo. Medida ruim nao entra.
 *  4. Erros bons se acumulam numa janela. A cada correctionIntervalMs a MEDIA da janela
 *     e aplicada com ganho positionGain (sem salto brusco).
 *  5. Se a odometria derivou muito, varios "outliers" coerentes entre si sao aceitos.
 *
 * IMPORTANTE (configuracao da Limelight): com a camera na torreta, deixe a
 * "Camera Pose in Robot Space" da Limelight TODA EM ZERO. Assim o botpose e a pose da
 * propria camera, e esta classe faz a conta da torreta (que a Limelight nao conhece).
 *
 * As posicoes das tags vem do FIELD MAP (.fmap) carregado na Limelight. Tag fora do
 * mapa (ex.: azuis 38-45 ainda sem medida) nao gera botpose -> status NO_POSE, nada e corrigido.
 * Parametros em TurretConfig.PoseCorrection.
 */
public class VisionPoseCorrector {

    public enum Status {
        NO_DATA, INVALID, STALE, FEW_TAGS, NO_POSE, FAR, MOVING,
        OUT_OF_FIELD, YAW_MISMATCH, OUTLIER, ACCEPTED
    }

    private static final double M_TO_IN = 39.3701;
    private static final long HIST_MS = 1000;

    private static final class Sample {
        final long t;
        double x, y, h;
        final double turret;
        Sample(long t, double x, double y, double h, double turret) {
            this.t = t; this.x = x; this.y = y; this.h = h; this.turret = turret;
        }
    }

    private static final class Innov {
        final long t;
        final double dx, dy, dh;
        Innov(long t, double dx, double dy, double dh) {
            this.t = t; this.dx = dx; this.dy = dy; this.dh = dh;
        }
    }

    private final PinpointLocalizer loc;
    private final ArrayDeque<Sample> hist = new ArrayDeque<>();
    private final List<Innov> window = new ArrayList<>();
    private final List<Innov> outliers = new ArrayList<>();

    private double lastTs = -1;
    private long lastCorrectionMs = 0;
    private boolean forceApply = false;

    private double speedInPerSec = 0, turnDegPerSec = 0;

    // telemetria / saida
    private Status status = Status.NO_DATA;
    private double visX = Double.NaN, visY = Double.NaN;
    private double innovX = Double.NaN, innovY = Double.NaN, yawErrDeg = Double.NaN;
    private double tagDistIn = Double.NaN;
    private double corrX, corrY, corrH;
    private double lastAppliedDx = 0, lastAppliedDy = 0, lastAppliedDh = 0;
    private int corrections = 0, rejections = 0;

    public VisionPoseCorrector(PinpointLocalizer loc) {
        this.loc = loc;
    }

    /**
     * Chame TODO loop, logo depois do pimpas.update().
     *
     * @param r            resultado da Limelight (pode ser null)
     * @param x,y          pose atual do Pinpoint (in)
     * @param headingDeg   heading atual do Pinpoint (graus, campo)
     * @param turretDeg    angulo atual da torreta (TurretController.angleDeg)
     * @param turretVelDegPerSec velocidade da torreta (TurretController.velDegPerSec)
     * @param apply        false = so mede e mostra na telemetria (nao mexe na pose)
     * @return true se a pose do Pinpoint foi corrigida neste loop
     */
    public boolean update(LLResult r, double x, double y, double headingDeg,
                          double turretDeg, double turretVelDegPerSec, boolean apply) {
        long now = System.currentTimeMillis();
        record(now, x, y, headingDeg, turretDeg);
        corrX = x;
        corrY = y;
        corrH = headingDeg;

        processObservation(r, now, turretVelDegPerSec);
        return maybeApply(now, x, y, headingDeg, apply);
    }

    // ------------------------------------------------------------------

    private void processObservation(LLResult r, long now, double turretVel) {
        if (r == null) { status = Status.NO_DATA; return; }
        if (!r.isValid()) { status = Status.INVALID; return; }
        if (r.getStaleness() > TurretConfig.PoseCorrection.maxStaleMs) { status = Status.STALE; return; }
        if (r.getTimestamp() == lastTs) return;   // mesmo frame do loop anterior: mantem status
        lastTs = r.getTimestamp();

        if (r.getBotposeTagCount() < TurretConfig.PoseCorrection.minTagCount) { status = Status.FEW_TAGS; return; }

        Pose3D bp = r.getBotpose();
        if (bp == null) { status = Status.NO_POSE; return; }
        Position pos = bp.getPosition().toUnit(DistanceUnit.INCH);
        if (pos.x == 0 && pos.y == 0) { status = Status.NO_POSE; return; }   // tag fora do mapa

        tagDistIn = r.getBotposeAvgDist() * M_TO_IN;   // botpose_avgdist vem em metros
        if (tagDistIn > TurretConfig.PoseCorrection.maxTagDistIn) { status = Status.FAR; return; }

        if (speedInPerSec > TurretConfig.PoseCorrection.maxRobotSpeedInPerSec
                || turnDegPerSec > TurretConfig.PoseCorrection.maxRobotTurnDegPerSec
                || (TurretConfig.PoseCorrection.cameraOnTurret
                    && Math.abs(turretVel) > TurretConfig.PoseCorrection.maxTurretSpeedDegPerSec)) {
            status = Status.MOVING;
            return;
        }

        // Instante da foto (mesma conta do TurretTracking)
        double latencyMs = r.getStaleness() + r.getCaptureLatency() + r.getTargetingLatency();
        Sample s = sampleAt(now - (long) latencyMs);
        if (s == null) { status = Status.NO_DATA; return; }

        // ---- Pose da camera: FTC (centro, m) -> Pedro (canto, in) ----
        double rot = Math.toRadians(TurretConfig.PoseCorrection.ftcToPedroRotDeg);
        double c = TurretConfig.PoseCorrection.fieldCenterIn;
        double camX = pos.x * Math.cos(rot) - pos.y * Math.sin(rot) + c;
        double camY = pos.x * Math.sin(rot) + pos.y * Math.cos(rot) + c;
        double camYaw = normalize180(bp.getOrientation().getYaw(AngleUnit.DEGREES)
                + TurretConfig.PoseCorrection.ftcToPedroRotDeg);

        // ---- Camera -> eixo da torreta -> centro do robo ----
        // Usa o heading da ODOMETRIA (confiavel) e o angulo da torreta NA HORA DA FOTO.
        double mountDeg = s.h + (TurretConfig.PoseCorrection.cameraOnTurret ? s.turret : 0.0);
        double m = Math.toRadians(mountDeg);
        double ax = camX - (TurretConfig.PoseCorrection.camFwdIn * Math.cos(m) - TurretConfig.PoseCorrection.camLeftIn * Math.sin(m));
        double ay = camY - (TurretConfig.PoseCorrection.camFwdIn * Math.sin(m) + TurretConfig.PoseCorrection.camLeftIn * Math.cos(m));

        double rx = ax, ry = ay;
        if (TurretConfig.PoseCorrection.cameraOnTurret) {
            double h = Math.toRadians(s.h);
            rx = ax - (HiveTracking.TURRET_OFFSET_X_IN * Math.cos(h) - HiveTracking.TURRET_OFFSET_Y_IN * Math.sin(h));
            ry = ay - (HiveTracking.TURRET_OFFSET_X_IN * Math.sin(h) + HiveTracking.TURRET_OFFSET_Y_IN * Math.cos(h));
        }
        visX = rx;
        visY = ry;

        if (rx < TurretConfig.PoseCorrection.fieldMinIn || rx > TurretConfig.PoseCorrection.fieldMaxIn
                || ry < TurretConfig.PoseCorrection.fieldMinIn || ry > TurretConfig.PoseCorrection.fieldMaxIn) {
            status = Status.OUT_OF_FIELD;
            return;
        }

        yawErrDeg = normalize180(camYaw - (mountDeg + TurretConfig.PoseCorrection.camYawOffsetDeg));
        if (Math.abs(yawErrDeg) > TurretConfig.PoseCorrection.maxYawErrorDeg) { status = Status.YAW_MISMATCH; return; }

        innovX = rx - s.x;
        innovY = ry - s.y;
        Innov in = new Innov(now, innovX, innovY, yawErrDeg);

        if (Math.hypot(innovX, innovY) > TurretConfig.PoseCorrection.maxJumpIn) {
            if (!outliers.isEmpty()) {
                Innov mo = mean(outliers);
                if (Math.hypot(in.dx - mo.dx, in.dy - mo.dy) > TurretConfig.PoseCorrection.outlierConsistencyIn) {
                    outliers.clear();   // nao bate com os anteriores: recomeca a contagem
                }
            }
            outliers.add(in);
            rejections++;
            if (outliers.size() >= TurretConfig.PoseCorrection.outlierRecoveryCount) {
                // Varias medidas seguidas concordam entre si: quem esta errada e a odometria.
                window.clear();
                window.addAll(outliers);
                outliers.clear();
                forceApply = true;
                status = Status.ACCEPTED;
            } else {
                status = Status.OUTLIER;
            }
            return;
        }

        outliers.clear();
        window.add(in);
        status = Status.ACCEPTED;
    }

    private boolean maybeApply(long now, double x, double y, double h, boolean apply) {
        prune(window, now);
        prune(outliers, now);

        if (!apply) { forceApply = false; return false; }

        boolean due = now - lastCorrectionMs >= TurretConfig.PoseCorrection.correctionIntervalMs
                && window.size() >= TurretConfig.PoseCorrection.minSamplesPerCorrection;
        if (!due && !forceApply) return false;
        if (window.isEmpty()) { forceApply = false; return false; }

        Innov m = mean(window);
        double ddx = TurretConfig.PoseCorrection.positionGain * m.dx;
        double ddy = TurretConfig.PoseCorrection.positionGain * m.dy;
        double ddh = TurretConfig.PoseCorrection.correctHeading ? TurretConfig.PoseCorrection.headingGain * m.dh : 0.0;

        corrX = x + ddx;
        corrY = y + ddy;
        corrH = normalize180(h + ddh);
        loc.setPose(new Pose(corrX, corrY, Math.toRadians(corrH)));

        // O historico passa a ficar no mesmo referencial da pose corrigida
        for (Sample s : hist) {
            s.x += ddx;
            s.y += ddy;
            s.h = normalize180(s.h + ddh);
        }

        lastAppliedDx = ddx;
        lastAppliedDy = ddy;
        lastAppliedDh = ddh;
        window.clear();
        forceApply = false;
        lastCorrectionMs = now;
        corrections++;
        return true;
    }

    // ------------------------------------------------------------------

    private void record(long now, double x, double y, double h, double turret) {
        Sample last = hist.peekLast();
        if (last != null) {
            double dt = (now - last.t) / 1000.0;
            if (dt > 1e-3) {
                double v = Math.hypot(x - last.x, y - last.y) / dt;
                double w = Math.abs(normalize180(h - last.h)) / dt;
                speedInPerSec = 0.7 * speedInPerSec + 0.3 * v;
                turnDegPerSec = 0.7 * turnDegPerSec + 0.3 * w;
            }
        }
        hist.addLast(new Sample(now, x, y, h, turret));
        while (!hist.isEmpty() && now - hist.peekFirst().t > HIST_MS) hist.removeFirst();
    }

    /** Ultima amostra do historico com tempo <= t (ou a mais antiga). */
    private Sample sampleAt(long t) {
        Sample best = null;
        for (Sample s : hist) {
            if (s.t <= t) best = s; else break;
        }
        return best != null ? best : hist.peekFirst();
    }

    private static Innov mean(List<Innov> l) {
        double sx = 0, sy = 0, sh = 0;
        for (Innov i : l) { sx += i.dx; sy += i.dy; sh += i.dh; }
        int n = l.size();
        return new Innov(0, sx / n, sy / n, sh / n);   // dh e pequeno (filtrado), media simples serve
    }

    private static void prune(List<Innov> l, long now) {
        Iterator<Innov> it = l.iterator();
        while (it.hasNext()) {
            if (now - it.next().t > TurretConfig.PoseCorrection.sampleMaxAgeMs) it.remove();
        }
    }

    private static double normalize180(double deg) {
        return ((deg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    public void reset() {
        hist.clear();
        window.clear();
        outliers.clear();
        lastTs = -1;
        lastCorrectionMs = 0;
        forceApply = false;
        speedInPerSec = 0;
        turnDegPerSec = 0;
        status = Status.NO_DATA;
    }

    // ---------- Getters (telemetria e uso no OpMode) ----------
    public Status getStatus() { return status; }
    public double getCorrectedX() { return corrX; }
    public double getCorrectedY() { return corrY; }
    public double getCorrectedHeadingDeg() { return corrH; }
    public double getVisionX() { return visX; }
    public double getVisionY() { return visY; }
    public double getInnovX() { return innovX; }
    public double getInnovY() { return innovY; }
    public double getYawErrDeg() { return yawErrDeg; }
    public double getTagDistIn() { return tagDistIn; }
    public int getWindowSize() { return window.size(); }
    public int getCorrections() { return corrections; }
    public int getRejections() { return rejections; }
    public double getLastAppliedDx() { return lastAppliedDx; }
    public double getLastAppliedDy() { return lastAppliedDy; }
    public double getLastAppliedDh() { return lastAppliedDh; }
    public double getSpeedInPerSec() { return speedInPerSec; }
    public double getTurnDegPerSec() { return turnDegPerSec; }
    /** Segundos desde a ultima correcao aplicada (NaN se nunca corrigiu). */
    public double secondsSinceCorrection() {
        return corrections == 0 ? Double.NaN : (System.currentTimeMillis() - lastCorrectionMs) / 1000.0;
    }
}
