package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.teamcode.Autonomo.pedro.Constants;

@TeleOp(name = "PodsTest", group = "Testes")
public class podsTest extends LinearOpMode {

    @Override
    public void runOpMode() {

        GoBildaPinpointDriver pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        // Configuração dos pods usando as constantes configuradas
        pinpoint.setOffsets(
                Constants.localizerConfig.xPodOffset.get(),
                Constants.localizerConfig.yPodOffset.get(),
                DistanceUnit.INCH
        );
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_SWINGARM_POD);
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );
        pinpoint.resetPosAndIMU();

        telemetry.addData("Status", "Inicializado com sucesso! Pressione START.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            // Atualiza as leituras do computador de odometria Pinpoint
            pinpoint.update();

            // Pressionar o botão A zera a posição e recalibra o IMU
            if (gamepad1.a) {
                pinpoint.resetPosAndIMU();
            }

            Pose2D pose = pinpoint.getPosition();


            telemetry.addLine("\n--- DICAS DE VERIFICAÇÃO ---");
            telemetry.addLine("• Mova o robô PARA FRENTE: Encoder X deve AUMENTAR.");
            telemetry.addLine("• Mova o robô PARA A ESQUERDA: Encoder Y deve AUMENTAR.");
            telemetry.addLine("• Pressione 'A' no Gamepad 1 para zerar a posição.");


            telemetry.addLine("\n--- ENCODERS BRUTOS (TICKS) ---");
            telemetry.addData("Encoder X (Frente)", pinpoint.getEncoderX());
            telemetry.addData("Encoder Y (Lateral)", pinpoint.getEncoderY());

            telemetry.addLine("\n--- POSIÇÃO CALCULADA ---");
            telemetry.addData("X (Polegadas)", "%.2f in", pose.getX(DistanceUnit.INCH));
            telemetry.addData("Y (Polegadas)", "%.2f in", pose.getY(DistanceUnit.INCH));
            telemetry.addData("Ângulo (Graus)", "%.2f°", pose.getHeading(AngleUnit.DEGREES));

            telemetry.addLine("\n--- VELOCIDADES ---");
            telemetry.addData("Velocidade X", "%.2f in/s", pinpoint.getVelX(DistanceUnit.INCH));
            telemetry.addData("Velocidade Y", "%.2f in/s", pinpoint.getVelY(DistanceUnit.INCH));
            telemetry.addData("Velocidade Angular", "%.2f deg/s", pinpoint.getHeadingVelocity(UnnormalizedAngleUnit.DEGREES));



            telemetry.update();
        }
    }
}
