package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.PIDS.RobotPID;

import java.lang.annotation.Target;
import java.util.Map;


public class UsualFunctions extends LinearOpMode {

    Map<Double, Double> tabelaValorSin = Map.ofEntries(
            Map.entry(40.0, 0.643), Map.entry(41.0, 0.656),
            Map.entry(42.0, 0.669), Map.entry(43.0, 0.682),
            Map.entry(44.0, 0.695), Map.entry(45.0, 0.707),
            Map.entry(46.0, 0.719), Map.entry(47.0, 0.731),
            Map.entry(48.0, 0.734), Map.entry(49.0, 0.755),
            Map.entry(50.0, 0.766), Map.entry(51.0, 0.777),
            Map.entry(52.0, 0.788), Map.entry(53.0, 0.799),
            Map.entry(54.0, 0.809), Map.entry(55.0, 0.819),
            Map.entry(56.0, 0.829), Map.entry(57.0, 0.839),
            Map.entry(58.0, 0.848), Map.entry(59.0, 0.857),
            Map.entry(60.0, 0.866), Map.entry(61.0, 0.875),
            Map.entry(62.0, 0.883), Map.entry(63.0, 0.891),
            Map.entry(64.0, 0.899), Map.entry(65.0, 0.906),
            Map.entry(66.0, 0.914), Map.entry(67.0, 0.921),
            Map.entry(68.0, 0.927), Map.entry(69.0, 0.934),
            Map.entry(70.0, 0.94),  Map.entry(71.0, 0.946),
            Map.entry(72.0, 0.951), Map.entry(73.0, 0.956),
            Map.entry(74.0, 0.961), Map.entry(75.0, 0.966),
            Map.entry(76.0, 0.97 ), Map.entry(77.0, 0.974),
            Map.entry(78.0, 0.978), Map.entry(79.0, 0.982),
            Map.entry(80.0, 0.985), Map.entry(81.0, 0.988)
    );



    private Limelight3A limelight3A;
    public enum LimeStatus {RIGHT, LEFT, NONE, SEEING}
    public LimeStatus act;

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor shooter, feeder, MidTake;

    public final float PotentShooter = 0.60f, PotentMidTake = 0.60f;
    private final double PosEquals90Degree = 0; // TESTAR

    private final int TimeUntillShoot = 1500;


    //Servos
    private Servo RampServo, FeederServo;


    //Feeder Servo
    public final float InitPosFeeder = -0.40f, ClosedPosFeeder = -0.15f, FeederCatchPos = 0.20f; //MUDAR DEPOIS


    //===========================================================================

    //Limelight + Turret Servo
    private Servo TurretLimeServo;
    public final float LimeAligment = 0.02f; //MUDAR DEPOIS
    public final float LimeInitPos = 0f; //AJUSTAR DEPOIS -> TESTE
    double potence = 0;
    //===========================================================================


    //Ramp Servo
    public final float RampCatchPos = 0.3f, RampClosedPos = 0.0f;


    //Gate Servo
    private Servo GateServo;
    public final float GateOpen = 0.5f, GateClosed = 0f; //AJUSTAR


    //MEDIDAS PARA ATIRAR PRECISAMENTE

    final float Hrobot = 31.3f, HAprilTag = 130f;


        //OUTRA CLASSE

    private double Kp = 0.05;
    private double Ki = 0.0002;
    private double Kd = 0.0031;
    private RobotPID FunctionsPID = new RobotPID(Kp,Ki, Kd);


    public void runOpMode(){

    }

    public void LimelightTrakingServo(LLResult resultado, double Tx){


        //>>Defining Position of Turret's using LimeServo<<

        double CurrentPoslime = TurretLimeServo.getPosition();

        act = LimeStatus.NONE;

        if (resultado != null && resultado.isValid() ) {

            if (Tx < -5){ act = LimeStatus.LEFT; }
            else if (Tx >  5){ act = LimeStatus.RIGHT;}
            else{ act = LimeStatus.SEEING; }

        }


        //Ver o lado correto de giro do servo
        switch (act){

            case LEFT:

                TurretLimeServo.setPosition(CurrentPoslime + LimeAligment);

                break;

            case RIGHT:

                TurretLimeServo.setPosition(CurrentPoslime - LimeAligment);

                break;

            case SEEING:

                //===>AVISO PARA ATIRAR<===
                gamepad1.rumble(0.5, 0.5, 700);
                gamepad2.rumble(0.5, 0.5, 700);

                break;

            case NONE:

                break;

        }
    }
    public double PotentShot( LLResult llResult){

        double Angle = 45 + llResult.getTy();

        return 0.6 + (HAprilTag - Hrobot)/Sin(Angle)/11.2;

    }
    public double Sin(double anglin){

        Double value = tabelaValorSin.get((double) Math.round(anglin));
        if ( value == null ){
            return 0;
        }

        return value; }

    public double ConvertPosServoToDegree(double CurrentPosServo){

        return 90*CurrentPosServo/4;

    }

    public void PID_Spin(boolean PositionOfServo, double TargetAngle,
                         DcMotor leftFront, DcMotor leftBack,
                         DcMotor rightFront, DcMotor rightBack, IMU imu
    ) {

        if (PositionOfServo) {
            while (opModeIsActive()) {

                double CurrentAngle = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);

                double potence = FunctionsPID.Calculate(TargetAngle, CurrentAngle);

                leftFront.setPower(potence);
                leftBack.setPower(potence);
                rightFront.setPower(-potence);
                rightBack.setPower(-potence);
                if (Math.abs(FunctionsPID.RealAngle(TargetAngle - CurrentAngle)) < 1.5) {
                    break;
                }
            }
        }else{
            while (opModeIsActive()){

                double CurrentAngle = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);

                double potence = FunctionsPID.Calculate(TargetAngle, CurrentAngle);

                leftFront.setPower(potence);
                leftBack.setPower(potence);
                rightFront.setPower(-potence);
                rightBack.setPower(-potence);
                if (Math.abs(FunctionsPID.RealAngle(TargetAngle - CurrentAngle)) < 1.5){
                    break;
                }
            }
        }
    }
}

