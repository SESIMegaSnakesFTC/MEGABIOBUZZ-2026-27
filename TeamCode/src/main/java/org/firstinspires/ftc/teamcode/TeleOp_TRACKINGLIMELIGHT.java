package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Codigo de Teleoperado para a equipe MEGA.
 * Inclui Mecanum com Compensação de Força (Bias) MANUAL no Strafe.
 * Implementação dos servos
 * Implementação do IMU
 */
@Disabled
public class TeleOp_TRACKINGLIMELIGHT extends LinearOpMode {

    // ==> TRACKING LIMELIGHT <==

    //
    private enum ACTIONS {LEFT, RIGHT, NONE}
    private ACTIONS acto = ACTIONS.NONE;
    private boolean ModoTracking = false;
    private boolean lastApressed = false;
    private final double Kp = 0.02;
    private final double SpeedProcura = 0.14;


    // Chassi
    private DcMotor leftFront, leftBack, rightBack, rightFront;

    // Mecanismos
    private DcMotor spindexer, feeder, shooter, limelightMotor;
    private Servo leftServo, rightServo;
    private Limelight3A limelight;
    private IMU imu;

    // --- CONFIGURAÇÃO DE COMPENSAÇÃO MANUAL (BIAS) ---
    // Ajuste este valor conforme seus testes para o robô andar reto.
    // Ex: 0.1, 0.2, 0.3... quanto maior, mais força o lado oposto ganha.
    private final double STRAFE_BIAS_FACTOR = 0.8;


    @Override
    public void runOpMode() {

    // ==>INIT CONFIG <==

        INITConfig();

    //=====================



        // Variáveis de Estado para o Shooter/Spindexer
        boolean lastRT            = false;
        boolean lastLT            = false;
        double shooterActivePower = 0;
        boolean rightServoOpen    = false;
        boolean leftServoOpen     = false;


        waitForStart();

        while (opModeIsActive()) {

            // ---|---Definindo Modo de operação ---|---

            boolean A_pressed = gamepad1.a;
            ModoTracking = (gamepad1.a && !lastApressed);
            lastApressed = A_pressed;

            // ==> __ANGLIN DO ROBÔ__ <==

            double angulo = imu.getRobotYawPitchRollAngles().getYaw();

            //==========================================

            // --- CONTROLE DE MOVIMENTAÇÃO (Gamepad 1) ---
            double y = -gamepad1.left_stick_y;
            double x = gamepad1.left_stick_x;
            double rx = gamepad1.right_stick_x;

            // --- Lógica de Multiplicador de Força MANUAL (Bias) ---
            double leftMultiplier = 1.0;
            double rightMultiplier = 1.0;

            // Se estiver indo para a ESQUERDA (x negativo), aumenta força na DIREITA
            if (x < -0.1) {
                rightMultiplier = 1.0 + (Math.abs(x) * STRAFE_BIAS_FACTOR);
            }
            // Se estiver indo para a DIREITA (x positivo), aumenta força na ESQUERDA
            else if (x > 0.1) {
                leftMultiplier = 1.0 + (Math.abs(x) * STRAFE_BIAS_FACTOR);
            }

            // Cálculo das potências aplicando os multiplicadores de compensação
            double frontLeft  = (y + x + rx) * leftMultiplier;
            double backLeft   = (y - x + rx) * leftMultiplier;
            double frontRight = (y - x - rx) * rightMultiplier;
            double backRight  = (y + x - rx) * rightMultiplier;

            // Normalização para não ultrapassar 1.0 (100% de força)
            double denominator = Math.max(Math.max(Math.abs(frontLeft), Math.abs(backLeft)),
                    Math.max(Math.abs(frontRight), Math.abs(backRight)));
            denominator = Math.max(denominator, 1.0);

            leftFront.setPower(frontLeft / denominator);
            leftBack.setPower(backLeft / denominator);
            rightFront.setPower(frontRight / denominator);
            rightBack.setPower(backRight / denominator);


            // --- CONTROLE DO SHOOTER E SPINDEXER (Gamepad 2)  Lógica de Toggle ---
            boolean currentRT = gamepad2.right_trigger > 0.5;
            boolean currentLT = gamepad2.left_trigger > 0.5;

            if (currentRT && !lastRT) {

                if (shooterActivePower == 1.0){
                    shooterActivePower = 0;
                    leftServoOpen = false;
                }
                else {
                    shooterActivePower = 1.0;
                    leftServoOpen = true;
                    rightServoOpen = false;
                }
            }
            if (currentLT && !lastLT) {

                if (shooterActivePower == -1.0){
                    shooterActivePower = 0;
                    rightServoOpen = false;
                }
                else {
                    shooterActivePower = -1.0;
                    rightServoOpen = true;
                    leftServoOpen = false;
                }
            }

            if (rightServoOpen){
                rightServo.setPosition(1.0);
                leftServo.setPosition(0.0);

            }
            else if(leftServoOpen){
                rightServo.setPosition(0.0);
                leftServo.setPosition(1.0);
            }
            else{
                leftServo.setPosition(0.0); rightServo.setPosition(0.0);
            }


            lastRT = currentRT;
            lastLT = currentLT;

            shooter.setPower(shooterActivePower);
            spindexer.setPower(shooterActivePower * 0.8);

            // --- CONTROLE DO FEEDER (Gamepad 2) ---
            if (gamepad2.right_bumper) feeder.setPower(1.0);
            else if (gamepad2.left_bumper) feeder.setPower(-1.0);
            else feeder.setPower(0);

            // ==> __LIMELIGHT__ <==


            if (ModoTracking){
                TRACKING_LIMELIGHT();
            }


            // Telemetria
            telemetry.addData("Angulo", angulo);
            telemetry.update();

        }
    }


    public void INITConfig(){

        // Mapeamento de Hardware
        leftFront      = hardwareMap.get(DcMotor.class, "leftFront");
        leftBack       = hardwareMap.get(DcMotor.class, "leftBack");
        rightBack      = hardwareMap.get(DcMotor.class, "rightBack");
        rightFront     = hardwareMap.get(DcMotor.class, "rightFront");
        spindexer      = hardwareMap.get(DcMotor.class, "Spindexer");
        feeder         = hardwareMap.get(DcMotor.class, "feeder");
        shooter        = hardwareMap.get(DcMotor.class, "shooter");
        leftServo      = hardwareMap.get(Servo.class, "servoLeft");
        rightServo     = hardwareMap.get(Servo.class, "servoRight");
        imu            = hardwareMap.get(IMU.class, "imu");
        limelight      = hardwareMap.get(Limelight3A.class, "limelight");
        limelightMotor = hardwareMap.get(DcMotor.class, "limelightMotor");

        rightServo.setDirection(Servo.Direction.REVERSE);

        // Direção dos motores
        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftBack.setDirection(DcMotor.Direction.REVERSE);
        rightFront.setDirection(DcMotor.Direction.FORWARD);
        rightBack.setDirection(DcMotor.Direction.FORWARD);

        // Comportamento Zero Power
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        spindexer.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //limelightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // ==> LIMELIGHT CONFIG <==

        limelight.setPollRateHz(90);
        limelight.pipelineSwitch(9); //APRIL TAG
        limelight.start();


        // ==>IMU CONFIG <==

        RevHubOrientationOnRobot revHubOrientationOnRobot =
                new RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.UP
                );
        imu.initialize(new IMU.Parameters(revHubOrientationOnRobot));
        imu.resetYaw();
    }
    private void TRACKING_LIMELIGHT(){

        LLResult result = limelight.getLatestResult();

        if (result != null && result.isValid()){

            double TX = result.getTx();

            acto = (TX > 0) ? ACTIONS.RIGHT : ( TX < 0 ? ACTIONS.LEFT : ACTIONS.NONE);

            double correction = -TX *Kp;

            correction = Math.max(-0.3987, Math.min(0.3987, correction));

            limelightMotor.setPower(correction);

        }
        else{

            //PROCURA POR ONDE ELA FOI VISTA PELA ÚLTIMA VEZ

            switch (acto){

                case LEFT: // por não ter break ele executa o debaixo
                case NONE:
                    limelightMotor.setPower(SpeedProcura);
                    break;

                case RIGHT:
                    limelightMotor.setPower(-SpeedProcura);
                    break;
            }
        }
        telemetry.addData("Seeing april tag", result);

    }
}