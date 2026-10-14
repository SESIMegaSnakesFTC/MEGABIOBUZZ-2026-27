package org.firstinspires.ftc.teamcode.Mech;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimeLight {


    private final double heightLimelight = 25; //CM
    private final double inclinationLimelight = 55;//Graus
    private final double heightHive = 110;
    private Limelight3A limelight3A;

    private static final Map<Integer, Double> SENO = new HashMap<>();

    // Bloco estático: roda uma única vez, quando a classe é carregada pela JVM,
    // antes de qualquer uso do mapa. Aqui é onde preenchemos todos os valores.
    static {
        SENO.put(45, 0.707);
        SENO.put(46, 0.719);
        SENO.put(47, 0.731);
        SENO.put(48, 0.743);
        SENO.put(49, 0.755);
        SENO.put(50, 0.766);
        SENO.put(51, 0.777);
        SENO.put(52, 0.788);
        SENO.put(53, 0.799);
        SENO.put(54, 0.809);
        SENO.put(55, 0.819);
        SENO.put(56, 0.829);
        SENO.put(57, 0.839);
        SENO.put(58, 0.848);
        SENO.put(59, 0.857);
        SENO.put(60, 0.866);
        SENO.put(61, 0.875);
        SENO.put(62, 0.883);
        SENO.put(63, 0.891);
        SENO.put(64, 0.899);
        SENO.put(65, 0.906);
        SENO.put(66, 0.914);
        SENO.put(67, 0.921);
        SENO.put(68, 0.927);
        SENO.put(69, 0.934);
        SENO.put(70, 0.940);
    }

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

    public double getDistance(){
        double degree = inclinationLimelight + limelight3A.getLatestResult().getTy();
        return (heightHive - heightLimelight)/Math.sin(degree)/2.54;//Hipotenusa
    }
    private double getSin(double angle){
        angle = Math.round(angle);
        double value = SENO.get(angle);

        return  SENO.get(angle) != null ? SENO.get(angle) : 1;
    }
}
