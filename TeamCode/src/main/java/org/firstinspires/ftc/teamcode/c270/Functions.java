package org.firstinspires.ftc.teamcode.c270;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.firstinspires.ftc.vision.apriltag.AprilTagSingleDetection; // IMPORTANTE: Necessário para acessar o ID

import java.util.List;


@TeleOp(name = "C270", group = "Visão")
public class Functions extends LinearOpMode {

    // VisionPortal: gerencia a conexão com a câmera USB
    private VisionPortal visionPortal;

    // AprilTagProcessor: É o que procura as tags na imagem
    private AprilTagProcessor aprilTag;

    @Override
    public void runOpMode() {
        
        // 1. O processador de AprilTags.
        // Ele vem configurado por padrão para a família 36h11
        aprilTag = new AprilTagProcessor.Builder()
                .build();

        // 2. O portal da visão.
        // Qual câmera usar e qual processador deve rodar nela.
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .addProcessor(aprilTag)
                .build();



        waitForStart();
        while (opModeIsActive()) {
            
            // 3. Pegando a lista de todas as tags que a câmera está vendo agora
            List<AprilTagDetection> detections = aprilTag.getDetections();

            // 4. Vamos olhar cada detecção encontrada
            for (AprilTagDetection detection : detections) {

                if (detection instanceof AprilTagSingleDetection) {
                    
                    // Transformamos a detecção genérica em uma detecção específica de tag única (Cast)
                    AprilTagSingleDetection singleTag = (AprilTagSingleDetection) detection;

                    // Agora sim, conseguimos acessar o .id, .range, etc.
                    telemetry.addData("ID da Tag", singleTag.id);
                    telemetry.addData("Distância (Pol)", singleTag.ftcPose.range);
                    telemetry.addData("Ângulo (Bearing)", singleTag.ftcPose.bearing);
                }
            }

            // Mostra tudo na tela do Driver Station
            telemetry.update();
            
            // Pequena pausa para não sobrecarregar o processador
            sleep(20);
        }
        
        // 5. Quando o código para, fechamos o portal para liberar a câmera
        visionPortal.close();
    }
}
