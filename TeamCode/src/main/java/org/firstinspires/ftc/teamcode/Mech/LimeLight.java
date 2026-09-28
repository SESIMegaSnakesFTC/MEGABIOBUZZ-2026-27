package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class LimeLight {

    private Limelight3A limelight3A;

    public void init(HardwareMap hardwareMap){

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");

    }

    public void TurnON(){

        limelight3A.start();
        limelight3A.setPollRateHz(70);
        limelight3A.pipelineSwitch(8);

    }

    public LLResult getResult(){ return limelight3A.getLatestResult(); }

}
