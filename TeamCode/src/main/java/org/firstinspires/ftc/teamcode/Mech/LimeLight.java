package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;

public class LimeLight {

    private Limelight3A limelight3A;

    public void init(HardwareMap hardwareMap){

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");

    }

    private int currentPipeline = -1;

    public void TurnONred(boolean RedSideUP){

        limelight3A.setPollRateHz(90);   // poll rate antes do start()
        limelight3A.start();
        setPipeline( RedSideUP ? 0 : 1 ); /**0 ->**/

    }

    public void TurnONblue(boolean BlueSideUP){

        limelight3A.setPollRateHz(90);
        limelight3A.start();
        setPipeline(BlueSideUP ? 2 : 3);
    }

    /** Liga a camera ja no pipeline certo (aliança + hive). Chamar uma vez no init. */
    public void TurnON(boolean red, boolean sideUp){
        if (red) TurnONred(sideUp); else TurnONblue(sideUp);
    }

    /** Troca o pipeline quando a hive muda. So manda o comando se mudou de fato. */
    public void setSide(boolean red, boolean sideUp){
        setPipeline(red ? (sideUp ? 0 : 1) : (sideUp ? 2 : 3));
    }

    private void setPipeline(int index){
        if (index == currentPipeline) return;
        limelight3A.pipelineSwitch(index);
        currentPipeline = index;
    }

    public int getRequestedPipeline(){ return currentPipeline; }

    public boolean isConnected(){ return limelight3A.isConnected(); }
    public boolean isRunning(){ return limelight3A.isRunning(); }
    /** ms desde o ultimo pacote que a Control Hub recebeu da camera. */
    public long msSinceLastUpdate(){ return limelight3A.getTimeSinceLastUpdate(); }
    /** Pedido HTTP bloqueante (ate ~100 ms) -> NAO chamar todo loop. */
    public LLStatus getStatus(){ return limelight3A.getStatus(); }

    public void TurnOFF(){
        limelight3A.stop();
        currentPipeline = -1;
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
