package org.firstinspires.ftc.teamcode.Mech.Turret;

import com.qualcomm.hardware.limelightvision.LLResult;

import java.util.ArrayDeque;

/**
 * TurretTracker: le a Limelight, decide QUAL e o angulo alvo e controla os estados.
 * SEARCH: sem alvo, varre de um limite ao outro.
 * TRACK:  alvo confirmado, segue o alvo.
 * LOST:   perdeu o alvo, segura o ultimo ponto por lostTimeoutMs; depois volta a SEARCH.
 * Chame update() DEPOIS de turret.update().
 */
public class TurretTracking {
    public enum State { SEARCH, TRACK, LOST }
    public State state = State.SEARCH;

    private final TurretController turret;
    private final ArrayDeque<double[]> hist = new ArrayDeque<>(); // {tempoMs, anguloDeg}
    private double target = 0;       // target filtrado
    private double sweepDir = 1;     // sentido da varredura
    private double lastTs = -1;      // timestamp do ultimo quadro usado
    private int seen = 0;            // quadros NOVOS validos seguidos
    private long lastValidMs = 0;
    private long lastMs = System.currentTimeMillis();

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

    public void update(LLResult r) {
        long now = System.currentTimeMillis();
        double dt = Math.max((now - lastMs) / 1000.0, 1e-3);
        lastMs = now;

        // guarda o angulo atual no historico (ate 40 amostras)
        hist.addLast(new double[]{now, turret.angleDeg});
        while (hist.size() > 40) hist.removeFirst();


        boolean good = r != null && r.isValid() && r.getStaleness() < TurretConfig.maxStaleMs;


        if (good && r.getTimestamp() != lastTs) {
            lastTs = r.getTimestamp();
            seen++;
            lastValidMs = now;
            double latencyMs = r.getStaleness() + r.getCaptureLatency() + r.getTargetingLatency();

            double meas = angleAt(now - (long) latencyMs) + TurretConfig.txSign * r.getTx();
            target = (seen == 1) ? meas : TurretConfig.alpha * meas + (1 - TurretConfig.alpha) * target;
        } else if (!good) {
            seen = 0;
        }


        State prev = state;
        if (good && seen >= TurretConfig.confirmFrames) state = State.TRACK;
        else if (state == State.TRACK && !good) state = State.LOST;
        if (state == State.LOST && now - lastValidMs > TurretConfig.lostTimeoutMs) state = State.SEARCH;


        if (state == State.SEARCH) {
            if (prev != State.SEARCH) target = turret.angleDeg;
            target += sweepDir * TurretConfig.sweepDegPerSec * dt;
            if (target > TurretConfig.maxDeg) { target = TurretConfig.maxDeg; sweepDir = -1; }
            if (target < TurretConfig.minDeg) { target = TurretConfig.minDeg; sweepDir = 1; }
        }


        turret.setTarget(target);
    }
}