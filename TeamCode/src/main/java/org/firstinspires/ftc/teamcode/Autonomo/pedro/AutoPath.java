package org.firstinspires.ftc.teamcode.Autonomo.pedro;

import static com.pedropathing.api.Paths.line;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.Mech.Turret.HiveTracking;

/**
 * Leva robô da pose atual até um dos 8 locais fixos, no TeleOp.
 * Botão apertado -> reta até o local. Quem chama decide o override (stick).
 */
public class AutoPath {

    private final Follower follower;

    // 8 locais. null = sem coordenada ainda, botão ignorado
    private final Pose[] locais = {
            HiveTracking.StartPose.RED_PARK.toPose(),  // L1
            HiveTracking.StartPose.BLUE_PARK.toPose(), // L2
            null, // L3
            null, // L4
            null, // L5
            null, // L6
            null, // L7
            null  // L8
    };

    // estado anterior de cada botão (borda de subida)
    private final boolean[] antes = new boolean[8];

    public AutoPath(Follower follower) {
        this.follower = follower;
    }

    public void GoToPosition(boolean b1, boolean b2, boolean b3, boolean b4,
                             boolean b5, boolean b6, boolean b7, boolean b8) {
        boolean[] agora = {b1, b2, b3, b4, b5, b6, b7, b8};
        for (int i = 0; i < 8; i++) {
            // só dispara no instante que aperta, não enquanto segura
            if (agora[i] && !antes[i] && locais[i] != null) {
                Pose atual = follower.pose();
                follower.follow(line(atual, locais[i]).linear(atual, locais[i]));
            }
            antes[i] = agora[i];
        }
    }
}
