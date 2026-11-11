package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.prism.Color;
import org.firstinspires.ftc.teamcode.prism.Direction;
import org.firstinspires.ftc.teamcode.prism.GoBildaPrismDriver;
import org.firstinspires.ftc.teamcode.prism.GoBildaPrismDriver.LayerHeight;
import org.firstinspires.ftc.teamcode.prism.PrismAnimations;

import java.util.concurrent.TimeUnit;

/**
 * Prism RGB LED Driver + LED Strips: 1 exemplo de cada uma das 12 animacoes.
 *
 * Config Driver Hub: porta I2C -> "goBILDA® Prism RGB LED Driver", nome "prism".
 * dpad direita/esquerda = proxima/anterior animacao.
 * back = apaga tudo.
 *
 * Libs (4 arquivos em teamcode/prism): GoBildaPrismDriver, PrismAnimations, Color, Direction.
 */
@TeleOp(name = "Prism - Todas Animacoes", group = "Exemplos")
public class TestePrismAnimacoes extends LinearOpMode {

    static final int TOTAL_LEDS = 12;   // AJUSTAR: LEDs das barras ligadas
    static final int TOTAL_ANIMACOES = 12;

    GoBildaPrismDriver prism;
    int atual = 0;

    @Override
    public void runOpMode() {
        prism = hardwareMap.get(GoBildaPrismDriver.class, "prism");
        prism.setStripLength(TOTAL_LEDS);   // quantos LEDs tem na fita
        prism.clearAllAnimations();

        waitForStart();
        mostrar(atual);

        boolean dirAntes = false, esqAntes = false;

        while (opModeIsActive()) {
            if (gamepad1.dpad_right && !dirAntes) { atual = (atual + 1) % TOTAL_ANIMACOES; mostrar(atual); }
            if (gamepad1.dpad_left && !esqAntes)  { atual = (atual + TOTAL_ANIMACOES - 1) % TOTAL_ANIMACOES; mostrar(atual); }
            if (gamepad1.back) prism.clearAllAnimations();

            dirAntes = gamepad1.dpad_right;
            esqAntes = gamepad1.dpad_left;

            telemetry.addData("Animacao", atual);
            telemetry.addData("FPS", prism.getCurrentFPS());
            telemetry.update();
        }
    }

    /** Escolhe qual animacao mostrar. */
    void mostrar(int n) {
        prism.clearAllAnimations();
        PrismAnimations.AnimationBase anim;

        switch (n) {
            case 0:  anim = solid();         break;
            case 1:  anim = blink();         break;
            case 2:  anim = pulse();         break;
            case 3:  anim = sineWave();      break;
            case 4:  anim = droidScan();     break;
            case 5:  anim = rainbow();       break;
            case 6:  anim = snakes();        break;
            case 7:  anim = random();        break;
            case 8:  anim = sparkle();       break;
            case 9:  anim = singleFill();    break;
            case 10: anim = rainbowSnakes(); break;
            default: anim = policeLights();  break;
        }

        // Toda animacao herda de AnimationBase: brilho + indices
        anim.setBrightness(50);                  // 0-100
        anim.setIndexes(0, TOTAL_LEDS - 1);      // primeiro e ultimo LED

        prism.insertAndUpdateAnimation(LayerHeight.LAYER_0, anim);
    }

    // 1. Solid: cor fixa
    PrismAnimations.AnimationBase solid() {
        PrismAnimations.Solid a = new PrismAnimations.Solid(Color.GREEN);
        a.setPrimaryColor(0, 255, 0);            // tambem da pra mudar por RGB
        return a;
    }

    // 2. Blink: alterna cor 1 / cor 2
    PrismAnimations.AnimationBase blink() {
        PrismAnimations.Blink a = new PrismAnimations.Blink(Color.ORANGE, Color.TRANSPARENT);
        a.setPeriod(1, TimeUnit.SECONDS);                 // ciclo completo
        a.setPrimaryColorPeriod(300, TimeUnit.MILLISECONDS); // tempo da cor 1
        return a;
    }

    // 3. Pulse: brilho sobe e desce
    PrismAnimations.AnimationBase pulse() {
        PrismAnimations.Pulse a = new PrismAnimations.Pulse(Color.CYAN, Color.TRANSPARENT);
        a.setPeriod(2, TimeUnit.SECONDS);
        return a;
    }

    // 4. SineWave: onda senoidal entre 2 cores
    PrismAnimations.AnimationBase sineWave() {
        PrismAnimations.SineWave a = new PrismAnimations.SineWave(Color.PURPLE, Color.YELLOW);
        a.setPeriod(3, TimeUnit.SECONDS);
        return a;
    }

    // 5. DroidScan: olho que vai e volta (cor 1 = olho, cor 2 = fundo)
    PrismAnimations.AnimationBase droidScan() {
        return new PrismAnimations.DroidScan(Color.RED, Color.TRANSPARENT);
    }

    // 6. Rainbow: faixa de matiz (hue). 0 a 360 = confirmar faixa no teste
    PrismAnimations.AnimationBase rainbow() {
        return new PrismAnimations.Rainbow(0f, 360f, 1f, Direction.Forward);
        // (startHue, stopHue, speed, direction)
    }

    // 7. Snakes: cobras de cores (vai bem com o nome da equipe)
    PrismAnimations.AnimationBase snakes() {
        Color[] cores = { Color.GREEN, Color.YELLOW };
        return new PrismAnimations.Snakes(3, 2, 12, Color.TRANSPARENT, 1f, Direction.Forward, cores);
        // (snakeLength, spacingBetween, repeatAfter, backgroundColor, speed, direction, colors)
    }

    // 8. Random: cores aleatorias dentro de uma faixa de matiz
    PrismAnimations.AnimationBase random() {
        PrismAnimations.Random a = new PrismAnimations.Random(0f, 360f, 1f);
        a.setHues(0f, 360f);                     // pode trocar depois
        return a;
    }

    // 9. Sparkle: brilhos aleatorios
    PrismAnimations.AnimationBase sparkle() {
        return new PrismAnimations.Sparkle(Color.WHITE, Color.BLUE);
    }

    // 10. SingleFill: enche a fita com cada cor da lista
    PrismAnimations.AnimationBase singleFill() {
        Color[] cores = { Color.RED, Color.GREEN, Color.BLUE };
        PrismAnimations.SingleFill a = new PrismAnimations.SingleFill(cores);
        a.setPeriod(3, TimeUnit.SECONDS);
        return a;
    }

    // 11. RainbowSnakes: cobras arco-iris
    PrismAnimations.AnimationBase rainbowSnakes() {
        return new PrismAnimations.RainbowSnakes(0f, 360f, 3, 3, 1);
        // (startHue, stopHue, numberOfSnakes, snakeLength, spacingBetween)
    }

    // 12. PoliceLights: vermelho / branco / azul
    PrismAnimations.AnimationBase policeLights() {
        PrismAnimations.PoliceLights a = new PrismAnimations.PoliceLights();
        a.setPrimaryColor(Color.RED);
        a.setSecondaryColor(Color.WHITE);
        a.setTertiaryColor(Color.BLUE);
        a.setPeriod(1, TimeUnit.SECONDS);
        return a;
    }
}
