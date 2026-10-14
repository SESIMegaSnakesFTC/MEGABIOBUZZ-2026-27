package org.firstinspires.ftc.teamcode.Mech.Servos;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;


public class HoodServo {

    // >>> Variáveis para ajuste de angulação <<<

    private final double Kp = 0.006;
    private final double Ki = 0.0001;
    private final double Kd = 0.0015;
    private double IntegralSUM = 0;
    private final double MAX_I = 0.023;
    private double LastTime = 0;
    private double LastError = 0;


    //Initial Position

    private final double InitialPos = 100;


    //MAX Position servo to TICKS:

    private final int MaxServoTICKS = 500; //Todo: Medir depois


    // >>> SERVO <<<
    private CRServo hoodServo;




    public void init(HardwareMap hwMap){

        hoodServo = hwMap.get(CRServo.class, "hoodServo");

    }

    public void hoodAjust(double posTICKS, double currentPos){

        // alvo invalido (tabela sem medir): para o servo
        if (Double.isNaN(posTICKS)){
            stop();
            return;
        }

        long now = System.nanoTime();
        double dt = LastTime == 0 ? 0 : (now - LastTime)/1e9;
        LastTime = now;

        double error = posTICKS - currentPos;


        if (Math.abs(error) < 15){
            hoodServo.setPower(0);
            IntegralSUM = 0;      // zera na zona morta, senao o valor velho volta depois
            LastError = error;    // evita pico no D ao sair da zona morta
        }

        else{

            if (dt > 0){

                IntegralSUM += error * dt;
                IntegralSUM = Clamp(MAX_I, -MAX_I, IntegralSUM);
            }

            double variationTime = !(dt > 0) ? 0 : (error - LastError)/dt;
            LastError = error;

            double potence = (Kp*error) + (Ki*IntegralSUM) + (Kd*variationTime);
            hoodServo.setPower(Clamp(1, -1, potence));//filtro -> se não o servo vai voar slk

        }
    }

    public double Clamp(double MAX, double MIN, double value){
        return Math.min(MAX, Math.max(MIN, value));
    }


    //Talvez só precise no autônomo
    // Um passo por loop (nao trava). Retorna true quando chegou na posicao inicial
    public boolean setInitPosition(double CurrentPosition){
        if (Math.abs(InitialPos - CurrentPosition) <= 30){
            hoodServo.setPower(0);
            return true;
        }
        hoodServo.setPower(CurrentPosition > InitialPos ? -0.2 : 0.2);
        return false;
    }
    public void stop(){
        hoodServo.setPower(0);
    }
}
