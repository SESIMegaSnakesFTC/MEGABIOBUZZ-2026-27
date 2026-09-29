package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Mech.Chassi;

import java.util.List;


@TeleOp(name = "TeleOpWarmUp", group = "TeleOp")
public class OficialTeleWarmUp extends LinearOpMode {

    boolean lastRT = false, RT_ON = false;
    boolean lastX = false, X_ON = true;
    boolean lastB = false, B_ON = false;



    //INSTÂNCIA PARA IMPORT'S(Bastante)

    private final Chassi chassi = new Chassi();




    @Override
    public void runOpMode() {


        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }



    }
}