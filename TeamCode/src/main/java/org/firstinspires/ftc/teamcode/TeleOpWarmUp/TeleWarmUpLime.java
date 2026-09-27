package org.firstinspires.ftc.teamcode.TeleOpWarmUp;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Mech.Mecanismos;

import java.util.List;

@TeleOp(name = "TeleWarmUpLimelight", group = "TeleOp")
public class TeleWarmUpLime extends LinearOpMode {

    //===========================================================================


    //INSTÂNCIA PARA IMPORT'S(Bastante)

    private Mecanismos mech = new Mecanismos();



    @Override
    public void runOpMode() {



        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        waitForStart();

    }
}
