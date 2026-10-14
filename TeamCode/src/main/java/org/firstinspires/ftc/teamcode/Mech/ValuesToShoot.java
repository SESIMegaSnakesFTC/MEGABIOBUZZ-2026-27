package org.firstinspires.ftc.teamcode.Mech;

import java.util.Map;
import java.util.TreeMap;


public class ValuesToShoot {

    // Chave: distancia do eixo da turret ate a hive -> polegadas
    // Valor: {posicao do servo do hood, potencia do shooter}
    public static final TreeMap<Double, double[]> ValoresShooting = new TreeMap<>();

    static {
        // Medir depois --> quand o shooter estiver pronto
        dist(40.0,    Double.NaN, 1350);
        dist(51.1811, Double.NaN, 1570);   // distancia maxima reto para a hive
        dist(60.0,    Double.NaN, Double.NaN);
    }

    public static void dist(double distance, double ServoPosition, double MotorPower){
        ValoresShooting.put(distance, new double[]{ServoPosition, MotorPower});
    }


    public static double servoPosition(double distance){
        return interpola(distance)[0];
    }


    public static double motorPower(double distance){
        return interpola(distance)[1];
    }

    private static double[] interpola(double distance){
        Map.Entry<Double, double[]> baixo = ValoresShooting.floorEntry(distance);
        Map.Entry<Double, double[]> cima  = ValoresShooting.ceilingEntry(distance);

        // Fora da tabela: usa o ponto mais proximo (nao extrapola)
        if (baixo == null) return cima.getValue();
        if (cima == null)  return baixo.getValue();
        if (baixo.getKey().equals(cima.getKey())) return baixo.getValue();


        double t = (distance - baixo.getKey()) / (cima.getKey() - baixo.getKey());
        double[] a = baixo.getValue(), b = cima.getValue();
        return new double[]{
                a[0] + t * (b[0] - a[0]),
                a[1] + t * (b[1] - a[1])
        };
    }
}
