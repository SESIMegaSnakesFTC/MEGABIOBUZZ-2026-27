package org.firstinspires.ftc.teamcode.PIDS;

public class RobotPID {

    private double Kp = 0.05;
    private double Ki = 0.002;
    private double Kd = 0.0091;
    private double IntegralSUM = 0;
    private long LastTIMEnano = 0;
    private double LastError = 0;
    private final double MaxI = 0.05;


    public RobotPID(double Kp, double Ki, double Kd){
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
    }



    public double Calculate(double AngleAlvo, double currentAngle){

        long now = System.nanoTime();
        double variationTime = LastTIMEnano == 0 ? 0 : (now - LastTIMEnano)/ 1e9;//Dividido por 1 segundo 1b de nano
        LastTIMEnano = now;


        double error = RealAngle(AngleAlvo - currentAngle);

        if (Math.abs(error) < 2.8567){ reset(); return 0;}

        if (variationTime > 0){

            IntegralSUM += variationTime*error;
            IntegralSUM  = LimiterPotence(-MaxI, MaxI, IntegralSUM);
        }

        double derivate = (variationTime > 0) ? (error - LastError)/variationTime : 0;
        LastError = error;

        double potent = (Kp*error) + (Kd*derivate) + (Ki*IntegralSUM);


        return LimiterPotence(-0.9, 0.9, potent);
    }


    public double RealAngle(double NowAngle){

        while (NowAngle >  180) NowAngle-= 360;
        while (NowAngle < -180) NowAngle+= 360;

        return NowAngle;
    }

    public void reset(){
        IntegralSUM  = 0;
        LastError    = 0;
        LastTIMEnano = 0;
    }

    private double LimiterPotence(double min, double max, double potence){ return Math.max(min, Math.min(max, potence)); }
}