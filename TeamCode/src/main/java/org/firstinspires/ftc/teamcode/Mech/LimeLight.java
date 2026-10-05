package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;

public class LimeLight {

    private Limelight3A limelight3A;

    public void init(HardwareMap hardwareMap){

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");

    }

    public void TurnONred(boolean RedSideUP){

        limelight3A.start();
        limelight3A.setPollRateHz(90);
        limelight3A.pipelineSwitch( RedSideUP ? 0 : 1 ); /**0 ->**/

    }

    public void TurnONblue(boolean BlueSideUP){

        limelight3A.start();
        limelight3A.setPollRateHz(90);
        limelight3A.pipelineSwitch(BlueSideUP ? 2 : 3);
    }
    public void TurnOFF(){
        limelight3A.stop();
    }

    public LLResult getResult(){ return limelight3A.getLatestResult(); }

    public double MediumTx(){
        double sum = 0;
        double CurrentTx = 0;

        LLResult result = getResult();


        //PEGA O TX DE TODAS AS APRIL TAGS
        List<Double> values = new ArrayList<>();

        for (LLResultTypes.FiducialResult tag : result.getFiducialResults()){

            double tx = tag.getTargetXDegrees();
            values.add(tx);

        }
        double quantidade = result.getFiducialResults().size();

        if (!values.isEmpty()){

            for (double v : values){
                sum += v;
            }
            CurrentTx = sum/quantidade;
        }else{
            return 0;
        }
        return CurrentTx;
    }
}
