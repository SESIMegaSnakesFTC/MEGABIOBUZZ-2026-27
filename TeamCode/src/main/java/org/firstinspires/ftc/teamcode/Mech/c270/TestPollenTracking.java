package org.firstinspires.ftc.teamcode.Mech.c270;

import android.util.Size;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;

/**
 * TeleOp de teste do PollenDetector (webcam C270, nome "Webcam 1" na config).
 * Não mexe em nenhum motor: só câmera + telemetria. Seguro rodar com o robô parado.
 *
 * COMO TESTAR:
 *   1. Dê INIT. O detector já roda durante o INIT.
 *   2. Ainda no INIT, no Driver Station: menu (⋮) -> "Camera Stream" para ver
 *      a imagem com as anotações (o stream do DS só funciona no INIT).
 *        amarelo = cada pólen | ciano = outros grupos | verde = maior grupo
 *        linha vermelha = centro da imagem (Tx = 0)
 *   3. Ajuste os parâmetros com o gamepad1 (abaixo) olhando a telemetria.
 *   4. Pode dar START: continua igual, só sem o stream no DS.
 *
 * CONTROLES (gamepad1, cada toque = um passo):
 *   dpad cima/baixo      -> CLUSTER_DIST_PX  +5 / -5
 *   dpad direita/esquerda-> MIN_AREA         +25 / -25
 *   RB / LB              -> H mínimo         +1 / -1
 *   Y / A                -> H máximo         +1 / -1
 *   B / X                -> S mínimo         +5 / -5
 *
 * CONTROLES (gamepad2):
 *   dpad cima/baixo      -> MIN_FILL         +0.05 / -0.05
 *   dpad direita/esquerda-> SPLIT_RATIO      +0.05 / -0.05
 *
 * Os valores ajustados ficam até reiniciar o app; anote os bons e copie para
 * os padrões no topo do PollenDetector.
 */
@TeleOp(name = "TestPollenTracking")
public class TestPollenTracking extends LinearOpMode {

    @Override
    public void runOpMode() {
        PollenDetector detector = new PollenDetector();

        // 640x480 é a resolução que tem calibração embutida para a C270,
        // o que deixa o Tx em graus preciso.
        VisionPortal portal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .setCameraResolution(new Size(640, 480))
                .addProcessor(detector)
                .build();

        // !isStopRequested() cobre INIT e RUN no mesmo loop
        // (ajustar no INIT é importante porque é quando o stream do DS funciona).
        while (!isStopRequested()) {
            ajustarParametros();

            // Lê UM resultado por loop: todos os números abaixo são do mesmo frame
            PollenDetector.Result r = detector.getResult();

            telemetry.addData("Fase", isStarted() ? "RUN" : "INIT (use o Camera Stream)");
            telemetry.addData("Câmera", "%s | %.0f FPS", portal.getCameraState(), portal.getFps());
            telemetry.addData("Tx calculado por", detector.isUsingCalibration() ? "calibração da lente" : "FOV_H_DEG (sem calibração)");
            telemetry.addLine();

            telemetry.addData("Tem alvo", r.hasTarget());
            telemetry.addData("Total de pólens", r.pollens.size());
            telemetry.addData("Grupos", r.groups.size());
            telemetry.addData("Maior grupo", r.largestGroupCount());
            telemetry.addData("Tx (graus)", "%.2f", r.txDeg);
            telemetry.addData("Tx (normalizado)", "%.3f", r.txNorm);
            if (r.dominant != null) {
                telemetry.addData("Centro do grupo (px)", "x=%.0f y=%.0f", r.dominant.centerX, r.dominant.centerY);
                telemetry.addData("Maior pólen do grupo (px²)", "%.0f", r.dominant.maxArea);
            }
            // Idade alta (> ~200 ms) = câmera travou ou caiu
            telemetry.addData("Idade do frame (ms)", r.timeNanos == 0 ? "sem frame ainda" : String.format("%.0f", r.ageMs()));
            telemetry.addLine();

            telemetry.addData("HSV mín", "H=%.0f S=%.0f V=%.0f", PollenDetector.lowerYellow.val[0], PollenDetector.lowerYellow.val[1], PollenDetector.lowerYellow.val[2]);
            telemetry.addData("HSV máx", "H=%.0f S=%.0f V=%.0f", PollenDetector.upperYellow.val[0], PollenDetector.upperYellow.val[1], PollenDetector.upperYellow.val[2]);
            telemetry.addData("MIN_AREA / MAX_AREA", "%.0f / %.0f", PollenDetector.MIN_AREA, PollenDetector.MAX_AREA);
            telemetry.addData("CLUSTER_DIST_PX", "%.0f", PollenDetector.CLUSTER_DIST_PX);
            telemetry.addData("MIN_FILL / SPLIT_RATIO", "%.2f / %.2f", PollenDetector.MIN_FILL, PollenDetector.SPLIT_RATIO);
            telemetry.addLine();
            telemetry.addLine("GP1: dpad ↕ cluster | dpad ↔ área mín | RB/LB H mín | Y/A H máx | B/X S mín");
            telemetry.addLine("GP2: dpad ↕ MIN_FILL | dpad ↔ SPLIT_RATIO");
            telemetry.update();

            sleep(20);
        }

        // Libera a câmera para o próximo OpMode
        portal.close();
    }

    /**
     * Altera os parâmetros estáticos do detector pelo gamepad1.
     * *WasPressed() só é true no loop em que o botão foi apertado (borda de
     * subida), então segurar o botão não dispara várias vezes.
     * A thread da câmera lê esses valores no próximo frame; a mudança no meio
     * de um frame é inofensiva para um teste.
     */
    private void ajustarParametros() {
        if (gamepad1.dpadUpWasPressed())    PollenDetector.CLUSTER_DIST_PX += 5;
        if (gamepad1.dpadDownWasPressed())  PollenDetector.CLUSTER_DIST_PX = Math.max(0, PollenDetector.CLUSTER_DIST_PX - 5);
        if (gamepad1.dpadRightWasPressed()) PollenDetector.MIN_AREA += 25;
        if (gamepad1.dpadLeftWasPressed())  PollenDetector.MIN_AREA = Math.max(0, PollenDetector.MIN_AREA - 25);

        // val[0] = H, val[1] = S, val[2] = V. H vai até 179 no OpenCV, S até 255.
        double[] lo = PollenDetector.lowerYellow.val;
        double[] hi = PollenDetector.upperYellow.val;
        if (gamepad1.rightBumperWasPressed()) lo[0] = Range.clip(lo[0] + 1, 0, hi[0]);
        if (gamepad1.leftBumperWasPressed())  lo[0] = Range.clip(lo[0] - 1, 0, hi[0]);
        if (gamepad1.yWasPressed())           hi[0] = Range.clip(hi[0] + 1, lo[0], 179);
        if (gamepad1.aWasPressed())           hi[0] = Range.clip(hi[0] - 1, lo[0], 179);
        if (gamepad1.bWasPressed())           lo[1] = Range.clip(lo[1] + 5, 0, 255);
        if (gamepad1.xWasPressed())           lo[1] = Range.clip(lo[1] - 5, 0, 255);

        // gamepad2: redondeza e separação de bolas coladas (0 a 1)
        if (gamepad2.dpadUpWasPressed())    PollenDetector.MIN_FILL = Range.clip(PollenDetector.MIN_FILL + 0.05, 0, 1);
        if (gamepad2.dpadDownWasPressed())  PollenDetector.MIN_FILL = Range.clip(PollenDetector.MIN_FILL - 0.05, 0, 1);
        if (gamepad2.dpadRightWasPressed()) PollenDetector.SPLIT_RATIO = Range.clip(PollenDetector.SPLIT_RATIO + 0.05, 0, 1);
        if (gamepad2.dpadLeftWasPressed())  PollenDetector.SPLIT_RATIO = Range.clip(PollenDetector.SPLIT_RATIO - 0.05, 0, 1);
    }
}
