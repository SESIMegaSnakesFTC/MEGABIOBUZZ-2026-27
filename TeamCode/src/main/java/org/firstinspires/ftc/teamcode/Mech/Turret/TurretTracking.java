package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;

import java.util.ArrayDeque;


public class TurretTracking {
    public enum State { ODO, TRACK }
    public State state = State.ODO;

    private final TurretController turret;
    private final ArrayDeque<double[]> hist = new ArrayDeque<>(); // {tempoMs, anguloDeg}
    private double target = 0;
    private double odoBias = 0;
    private double lastTs = -1;
    private int seen = 0;
    private long lastValidMs = 0;
    private double lastHeading = Double.NaN;
    private long lastHeadingMs = 0;
    private double chassisVelocity = 0;
    private double lastOdoAngle = Double.NaN;
    private long lastOdoNs = 0;
    private double targetVelocity = 0;   // graus/s do alvo relativo ao chassi (giro + translacao)
    private boolean hasBias = false;   // ja teve pelo menos uma medida da visao
    private int lastTagId = -1;
    private double lastTx = Double.NaN;
    private int tagsUsadas = 0;

    public TurretTracking(TurretController turret) { this.turret = turret; }

    public boolean isLocked() {
        return state == State.TRACK && turret.atTarget(TurretConfig.lockToleranceDeg);
    }

    /* Onde a torreta ESTAVA no instante t (para bater o tx com a posicao da hora da foto). */
    private double angleAt(long t) {
        double best = hist.isEmpty() ? 0 : hist.peekFirst()[1];
        for (double[] h : hist) {
            if (h[0] <= t) best = h[1]; else break;
        }
        return best;
    }

    private static double normalize180(double deg) {
        return ((deg + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }


    /**
     * @param x,y,headingDeg pose de campo do Pinpoint (heading em graus, como na HiveTracking)
     */
    public void update(LLResult r, HiveTracking hive, double x, double y, double headingDeg) {
        long now = System.currentTimeMillis();
        double odoAngleDeg = hive.turretAngleDeg(x, y, headingDeg);
        updateTargetVelocity(odoAngleDeg);

        hist.addLast(new double[]{now, turret.angleDeg});
        while (hist.size() > 40) hist.removeFirst();

        boolean fresh = r != null && r.isValid() && r.getStaleness() < TurretConfig.maxStaleMs;
        boolean good = false;

        if (fresh && r.getTimestamp() != lastTs) {
            double latencyMs = r.getStaleness() + r.getCaptureLatency() + r.getTargetingLatency();
            double turretNaFoto = angleAt(now - (long) latencyMs);

            // Cada tag da hive vista vira uma medida de bias, comparando com o angulo
            // esperado DAQUELA tag (nao do centro da hive). Depois tira a media.
            double somaBias = 0;
            double ref = Double.NaN;
            int n = 0;
            for (LLResultTypes.FiducialResult f : r.getFiducialResults()) {
                int id = f.getFiducialId();
                if (!hive.isTrackedTag(id)) continue;

                double meas = turretNaFoto + TurretConfig.txSign * f.getTargetXDegrees();
                double esperado = hive.turretAngleToTagDeg(id, x, y, headingDeg);
                double b = normalize180(meas - esperado);

                // media "circular": soma em relacao a primeira medida, evita bug perto de +-180
                if (n == 0) ref = b;
                somaBias += normalize180(b - ref);
                n++;

                lastTagId = id;
                lastTx = f.getTargetXDegrees();
            }

            if (n > 0) {
                good = true;
                lastTs = r.getTimestamp();
                seen++;
                lastValidMs = now;
                tagsUsadas = n;

                // Bias = quanto a odometria esta errada em relacao a visao.
                // Ele continua valendo quando a tag some, entao a odometria fica "recalibrada".
                double measBias = normalize180(ref + somaBias / n);
                if (!hasBias) {
                    odoBias = measBias;   // primeira medida: pega direto
                    hasBias = true;
                } else {
                    odoBias = normalize180(odoBias + TurretConfig.alpha * normalize180(measBias - odoBias));
                }
            }
        } else if (fresh) {
            good = seen > 0;   // mesmo frame de antes (loop mais rapido que a camera): nao conta de novo
        }
        if (!good) seen = 0;

        // estado
        if (good && seen >= TurretConfig.confirmFrames) {
            state = State.TRACK;
        } else if (now - lastValidMs > TurretConfig.lostTimeoutMs) {
            state = State.ODO;
        }

        target = odoAngleDeg + odoBias;
        turret.setTarget(target);
    }


    /**
     * Chamar quando o VisionPoseCorrector corrigir a pose do Pinpoint.
     * O bias foi medido em cima da pose ANTIGA; manter aplicaria a correcao duas vezes.
     * A proxima medida da visao reconstroi o bias direto (hasBias = false).
     */
    public void onPoseCorrected() {
        odoBias = 0;
        hasBias = false;
        lastOdoAngle = Double.NaN;   // o salto da pose nao vira pico no targetVelocity
    }

    public void updateHeading(double headingDeg) {
        long rn = System.currentTimeMillis();
        if (Double.isNaN(lastHeading)) {
            lastHeading = headingDeg;
            lastHeadingMs = rn;
            return;
        }

        double dt = Math.max((rn - lastHeadingMs) / 1000.0, 1e-3); // CORRIGIDO: 1000.0
        double df = headingDeg - lastHeading;
        while (df > 180) df -= 360;
        while (df < -180) df += 360;

        lastHeading = headingDeg;
        lastHeadingMs = rn;

        double raw = TurretConfig.headingSign * df / dt;
        chassisVelocity = 0.5 * chassisVelocity + 0.5 * raw;
    }

    /* Derivada do angulo da odometria (sem o bias, para nao dar pico quando a visao corrige). */
    private void updateTargetVelocity(double odoAngleDeg) {
        long ns = System.nanoTime();
        if (Double.isNaN(lastOdoAngle)) {
            lastOdoAngle = odoAngleDeg;
            lastOdoNs = ns;
            return;
        }
        double dt = Math.max((ns - lastOdoNs) / 1e9, 1e-3);
        double raw = normalize180(odoAngleDeg - lastOdoAngle) / dt;
        lastOdoAngle = odoAngleDeg;
        lastOdoNs = ns;
        targetVelocity += TurretConfig.targetVelAlpha * (raw - targetVelocity);
    }

    public double getChassisVelocity() { return chassisVelocity; }
    /** Velocidade do alvo em graus/s: passar para turret.update(). */
    public double getTargetVelocity() { return targetVelocity; }
    public double getOdoBias() { return odoBias; }
    public double getTarget() { return target; }
    public int getLastTagId() { return lastTagId; }
    public double getLastTx() { return lastTx; }
    public int getSeen() { return seen; }
    /** Quantas tags da hive entraram na ultima medida. */
    public int getTagsUsadas() { return tagsUsadas; }

    public void reset() {
        state = State.ODO;
        seen = 0;
        hist.clear();
        odoBias = 0;
        hasBias = false;
        lastTagId = -1;
        lastTx = Double.NaN;
        tagsUsadas = 0;
        lastValidMs = 0;
        target = turret.angleDeg;
        turret.setTarget(turret.angleDeg);
        lastHeading = Double.NaN;
        chassisVelocity = 0;
        lastOdoAngle = Double.NaN;
        targetVelocity = 0;
    }
}