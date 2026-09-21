package org.firstinspires.ftc.teamcode.PIDS;

public class ShootersPIDF {

    private double Kp = 0.05;
    private double Ki = 0.00091;
    private double Kd = 0.0091;
    private double Kf = 0.000398;
    private double IntegralSUM = 0;
    private final double MAX_I = 0.2;
    private double LastTimeNANO = 0;
    private double LastError = 0;


    public ShootersPIDF(double Kp, double Ki, double Kd, double Kf){
        this.Kp = Kp;
        this.Ki = Ki;
        this.Kd = Kd;
        this.Kf = Kf;
    }

    public double Calculate(double TargetTicks, double CurrentTicks){

        long TIMER = System.nanoTime();
        double variationTIME = (LastTimeNANO > 0) ? (TIMER - LastTimeNANO)/1e9 : 0;

        LastTimeNANO = TIMER;

        double error = TargetTicks - CurrentTicks;

        if (Math.abs(error) < 2.857){
            reset(); return 0;
        }

        if (variationTIME > 0){

            IntegralSUM += variationTIME*error;
            IntegralSUM = LimiterPotence(-MAX_I, MAX_I, IntegralSUM);
        }

        double feedFoward = Kf * TargetTicks;

        double derivate = (variationTIME > 0) ? (error - LastError)/variationTIME : 0;

        double output = (Kp*error) + (Ki*IntegralSUM) + ( Kd * derivate) + feedFoward;

        return LimiterPotence(0,0.8, output);
    }

    private double NormalizeAngle(double Angle){
        while(Angle >  180) Angle -= 360;
        while(Angle < -180) Angle += 360;

        return Angle;
    }

    public void reset(){

        IntegralSUM  = 0;
        LastError    = 0;
        LastTimeNANO = 0;
    }

    public double LimiterPotence(double min, double max, double CurrentPOtence){

        return Math.max(min, Math.min(max, CurrentPOtence));
    }
}
