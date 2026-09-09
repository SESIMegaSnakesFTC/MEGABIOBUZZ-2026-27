package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import java.util.List;

/**
 * Código de Teleoperado Completo para a equipe MEGA.
 * Chassi Mecanum, Shooter, Spindexer, Feeder e Servos.
 * Logica de Timer para acionamento de alguns mecanismos
 * ==> Uso de servos como "catracas" <==
 *   Talvez seja necessário redefinir valores iniciais dos servos
 *   após os próximos testes
 */
@TeleOp(name = "Teleoperado/Servos", group = "TeleOp")
public class Teleop extends LinearOpMode {

    // Chassi
    private DcMotor leftFront, leftBack, rightBack, rightFront;

    // Mecanismos
    private DcMotor spindexer, feeder, shooter;
    private Servo servoLeft, servoRight;

    // --- CONFIGURAÇÃO DE COMPENSAÇÃO MANUAL (BIAS) ---
    private final double FATOR_COMPENSACAO_STRAFE = 0.80;

    // --- CONFIGURAÇÃO DOS SERVOS ---
    private final double posZeroEsquerda = 0.00;
    private final double posZeroDireita = 0.18;
    private final double SERVO_ATIVO = 0.48; // Aproximadamente 70 graus
    private final double TEMPO_ESPERA = 1.92; // 2 segundos conforme pedido

    @Override
    public void runOpMode() {

        // Hardware Map
        leftFront  = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack   = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack  = hardwareMap.get(DcMotor.class, "rightBack");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        spindexer  = hardwareMap.get(DcMotor.class, "Spindexer");
        feeder     = hardwareMap.get(DcMotor.class, "feeder");
        shooter    = hardwareMap.get(DcMotor.class, "shooter");
        servoLeft  = hardwareMap.get(Servo.class, "servoLeft");
        servoRight = hardwareMap.get(Servo.class, "servoRight");

        // Direção dos Motores
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);

        // Direção dos Servos
        servoLeft.setDirection(Servo.Direction.FORWARD);
        servoRight.setDirection(Servo.Direction.FORWARD);

        // Zero Power Behavior
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // --- OTIMIZAÇÃO  --> Leitura de Inputs ---
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        waitForStart();

        // Variáveis de Estado
        boolean lastRT = false;
        boolean lastLT = false;
        double shooterPower = 0;
        boolean hasRumbled = false;
        ElapsedTime timer = new ElapsedTime();

        // Variáveis para Otimização de Escrita
        double lastFL = 0, lastBL = 0, lastFR = 0, lastBR = 0;
        double lastShooterPower = 0, lastSpindexerPower = 0, lastFeederPower = 0;
        double lastPosL = -1, lastPosR = -1;

        while (opModeIsActive()) {

            // --- 1. ENTRADA E MOVIMENTAÇÃO (Gamepad 1) ---
            double eixoY   = Range.clip(-gamepad1.left_stick_y, -1.0, 1.0);
            double eixoX   = Range.clip(gamepad1.left_stick_x, -1.0, 1.0);
            double rotacao = Range.clip(gamepad1.right_stick_x, -1.0, 1.0);

            // Deadzone para evitar drift ;(
            if (Math.abs(eixoY) < 0.01)   eixoY = 0;
            if (Math.abs(eixoX) < 0.01)   eixoX = 0;
            if (Math.abs(rotacao) < 0.01) rotacao = 0;

            // Lógica de Compensação de Strafe
            double compensacao = Math.abs(eixoX) * FATOR_COMPENSACAO_STRAFE;
            double multEsq = (eixoX > 0.1)  ? 1.0 + compensacao : 1.0;
            double multDir = (eixoX < -0.1) ? 1.0 + compensacao : 1.0;

            double fl = (eixoY + eixoX + rotacao) * multEsq;
            double bl = (eixoY - eixoX + rotacao) * multEsq;
            double fr = (eixoY - eixoX - rotacao) * multDir;
            double br = (eixoY + eixoX - rotacao) * multDir;

            double max = Math.max(Math.abs(fl), Math.max(Math.abs(bl), Math.max(Math.abs(fr), Math.abs(br))));
            if (max > 1.0) {
                fl /= max; bl /= max; fr /= max; br /= max;
            }

            // Otimização --> Envio de potência ao Chassi
            if (Math.abs(fl - lastFL) > 0.01) { leftFront.setPower(fl);  lastFL = fl; }
            if (Math.abs(bl - lastBL) > 0.01) { leftBack.setPower(bl);   lastBL = bl; }
            if (Math.abs(fr - lastFR) > 0.01) { rightFront.setPower(fr); lastFR = fr; }
            if (Math.abs(br - lastBR) > 0.01) { rightBack.setPower(br);  lastBR = br; }


            // ---  LOGICA DO SHOOTER (Gamepad 2) ---
            boolean triggerRT = gamepad2.right_trigger > 0.5;
            boolean triggerLT = gamepad2.left_trigger > 0.5;

            // Toggles para ligar/desligar o Shooter
            if (triggerRT && !lastRT) {
                shooterPower = (shooterPower == -1.0) ? 0 : -1.0;
                timer.reset();
                hasRumbled = false;
            }
            if (triggerLT && !lastLT) {
                shooterPower = (shooterPower == 1.0) ? 0 : 1.0;
                timer.reset();
                hasRumbled = false;
            }
            lastRT = triggerRT;
            lastLT = triggerLT;

            if (Math.abs(shooterPower - lastShooterPower) > 0.01) {
                shooter.setPower(shooterPower);
                lastShooterPower = shooterPower;
            }

            // ---  SINCRONIZAÇÃO (Spindexer + Servo) ---
            // Estados auxiliares para reduzir aninhamento
            boolean isShooting = (shooterPower != 0);
            boolean isReady    = (timer.seconds() >= TEMPO_ESPERA);

            double targetSpindexer = 0;
            double targetPosL      = posZeroEsquerda;
            double targetPosR      = posZeroDireita;

            // Se não estiver atirando, reseta rumble e ignora o resto da lógica de disparo
            if (!isShooting) {
                hasRumbled = false;
            }
            // Se estiver atirando e pronto (Timer OK)
            else if (isReady) {
                // Feedback tátil único
                if (!hasRumbled) {
                    gamepad2.rumble(500);
                    hasRumbled = true;
                }

                targetSpindexer = shooterPower * 0.8;
                targetPosL = (shooterPower == 1.0) ? Range.clip(posZeroEsquerda + SERVO_ATIVO, 0.0, 1.0) : posZeroEsquerda;
                targetPosR = (shooterPower == -1.0) ? Range.clip(posZeroDireita + SERVO_ATIVO, 0.0, 1.0) : posZeroDireita;
            }

            // Aplicar Otimizações de Escrita nos Mecanismos
            if (Math.abs(targetSpindexer - lastSpindexerPower) > 0.01) {
                spindexer.setPower(targetSpindexer);
                lastSpindexerPower = targetSpindexer;
            }
            if (Math.abs(targetPosL - lastPosL) > 0.005) {
                servoLeft.setPosition(targetPosL);
                lastPosL = targetPosL;
            }
            if (Math.abs(targetPosR - lastPosR) > 0.005) {
                servoRight.setPosition(targetPosR);
                lastPosR = targetPosR;
            }

            // ---  FEEDER ---
            double feederPower = 0;
            if (gamepad2.right_bumper)      feederPower = 1.0;
            else if (gamepad2.left_bumper) feederPower = -1.0;

            if (Math.abs(feederPower - lastFeederPower) > 0.01) {
                feeder.setPower(feederPower);
                lastFeederPower = feederPower;
            }
        }
        //Evitar conflito quando mudar para o Autônomo
        servoLeft.setPosition(posZeroEsquerda);
        servoRight.setPosition(posZeroDireita);
    }
}
