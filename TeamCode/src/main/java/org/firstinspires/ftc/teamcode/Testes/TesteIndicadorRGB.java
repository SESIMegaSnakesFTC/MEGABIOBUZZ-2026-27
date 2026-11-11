package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * goBILDA RGB Indicator Light (PWM). Nao tem lib propria: e um Servo do SDK.
 *
 * Config Driver Hub: porta servo -> "Servo", nome "luz1" (e "luz2" se tiver a segunda).
 * setPosition(0..1) escolhe a cor. Valores abaixo sao APROXIMADOS:
 * confirmar no robo (dpad cima/baixo varre o espectro).
 */
@TeleOp(name = "Indicador RGB", group = "Exemplos")
public class TesteIndicadorRGB extends LinearOpMode {

    static final double DESLIGADO = 0.0;
    static final double VERMELHO  = 0.277;
    static final double LARANJA   = 0.333;
    static final double AMARELO   = 0.388;
    static final double VERDE     = 0.500;
    static final double AZUL      = 0.611;
    static final double VIOLETA   = 0.722;
    static final double BRANCO    = 1.0;

    @Override
    public void runOpMode() {
        Servo luz1 = hardwareMap.get(Servo.class, "luz1");
        Servo luz2 = hardwareMap.get(Servo.class, "luz2");

        luz1.setPosition(DESLIGADO);
        luz2.setPosition(DESLIGADO);

        waitForStart();

        double varredura = 0.0;

        while (opModeIsActive()) {
            // luz1: cores fixas
            if (gamepad1.a) luz1.setPosition(VERDE);
            if (gamepad1.b) luz1.setPosition(VERMELHO);
            if (gamepad1.x) luz1.setPosition(AZUL);
            if (gamepad1.y) luz1.setPosition(AMARELO);

            // luz2: varredura para calibrar
            if (gamepad1.dpad_up)   varredura = Math.min(1.0, varredura + 0.002);
            if (gamepad1.dpad_down) varredura = Math.max(0.0, varredura - 0.002);
            luz2.setPosition(varredura);

            if (gamepad1.back) {
                luz1.setPosition(DESLIGADO);
                luz2.setPosition(DESLIGADO);
                varredura = 0.0;
            }

            telemetry.addData("luz1", "%.3f", luz1.getPosition());
            telemetry.addData("luz2", "%.3f", varredura);
            telemetry.update();
        }
    }
}
