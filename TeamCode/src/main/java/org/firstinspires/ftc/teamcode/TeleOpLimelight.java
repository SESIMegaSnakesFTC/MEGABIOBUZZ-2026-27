package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.List;
import java.util.Map;


@TeleOp(name = "TeleOpLimelight", group = "TeleOp")
public class TeleOpLimelight extends LinearOpMode {


    //LIMELIGHT

    private Limelight3A limelight3A;
    enum LimeStates {RIGHT, LEFT, NONE, SEEING}
    private LimeStates act;

    //==========================================================

    //MOTOR
    private DcMotor LeftBack, LeftFront, RightFront, RightBack;

    //==========================================================

    //Mech

    private DcMotor shooter, feeder;

    private final int TimeToShoot = 1500;

    boolean LastRT = false;
    boolean LastLT = false;
    boolean LastA = false;
    boolean LastB = false;

    //Servos
    private Servo LeftrampServo, RightrampServo, FeederServo;

            //Feeder Servo
    private final float InitPosFeeder = -0.50f, ClosedPosFeeder = 0.30f, FeederCatchPos = 0.20f; //MUDAR DEPOIS


    //===========================================================================

            //Limelight + Turret Servo
    private Servo TurretLimeServo;
    boolean LimelightON = false;
    private final float AligmentLime = 0.5f; //MUDAR DEPOIS
    private final float InitPosLime = 0f; //AJUSTAR DEPOIS -> TESTE
    private double potence = 0;
    //===========================================================================

            //Ramp Servo
    private final float RampCatchPos = 0.3f, RampClosedPos = 0.0f;

            //Gate Servo
    private Servo GateServo;
    private final float GateOpen = 0.5f, GateClosed = 0f; //AJUSTAR


    //MEDIDAS PARA ATIRAR PRECISAMENTE

    final float Hrobot = 31.3f, HAprilTag = 130f;


            //VALORES SENO

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


    @Override
    public void runOpMode(){

        INIT();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        waitForStart();

        while(opModeIsActive()){


            boolean RT = gamepad2.right_trigger > 0.5;
            boolean LT = gamepad2.left_trigger > 0.5;
            boolean  A = gamepad2.a;
            boolean  B = gamepad2.b;

            double x = gamepad1.left_stick_x * 1.09;
            double y = -gamepad1.left_stick_y;
            double rx = gamepad1.right_stick_x;


            double Median = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 0.9);

            double rfp = ( y + x + rx) / Median;
            double rbp = ( y - x + rx) / Median;
            double lfp = ( y - x - rx) / Median;
            double lbp = ( y + x - rx) / Median;

            LeftFront.setPower(rfp);LeftBack.setPower(rbp);
            RightFront.setPower(lfp);RightFront.setPower(lbp);


            //TURN ON && OFF LIMELIGHT
            if ( B && !LastB){


                limelight3A.start();
                limelight3A.setPollRateHz(75);
                limelight3A.pipelineSwitch(9); //AprilTag
                LimelightON = true;

            }


            //Limelight
            if (LimelightON){

                LLResult result = limelight3A.getLatestResult();
                double tx       = result.getTx();
                Trackinglimelight(result, tx);
                potence = PotentShot(result);

            }



            else{
                limelight3A.stop(); LimelightON = false;
            }



            if (RT && !LastRT){
                LeftrampServo.setPosition(RampClosedPos);
                RightrampServo.setPosition(RampClosedPos);
                FeederServo.setPosition(ClosedPosFeeder);
                shooter.setPower(potence);
            }



            if (LT && !LastLT){
                LeftrampServo.setPosition(RampClosedPos);
                RightrampServo.setPosition(RampClosedPos);
                FeederServo.setPosition(ClosedPosFeeder);
                shooter.setPower(potence);
            }
            else { shooter.setPower(0);  }



            if (gamepad2.right_bumper || gamepad2.left_bumper){

                FeederServo.setPosition(FeederCatchPos);
                feeder.setPower(0.9);

            } else{
                FeederServo.setPosition(ClosedPosFeeder);
                feeder.setPower(0);
            }



            if ( A && !LastA){
                GateServo.setPosition(GateOpen);
            }
            else{ GateServo.setPosition(GateClosed); }



            LastRT = RT; LastLT = LT; LastB = B; LastA = A;


        }
        RightrampServo.setPosition(RampCatchPos);
        LeftrampServo.setPosition(RampClosedPos);
        FeederServo.setPosition(ClosedPosFeeder);
        GateServo.setPosition(GateClosed);

    }


    public void INIT(){

        LeftBack = hardwareMap.get(DcMotor.class, "leftBack");
        LeftFront = hardwareMap.get(DcMotor.class, "leftFront");
        RightBack = hardwareMap.get(DcMotor.class, "rightBack");
        RightFront = hardwareMap.get(DcMotor.class,"rightFront");

        RightFront.setDirection(DcMotor.Direction.REVERSE);
        RightBack.setDirection(DcMotor.Direction.REVERSE);
        LeftBack.setDirection(DcMotorSimple.Direction.FORWARD);
        LeftFront.setDirection(DcMotorSimple.Direction.FORWARD);

        RightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LeftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Mech
        shooter = hardwareMap.get(DcMotor.class, "shooter");
        feeder  = hardwareMap.get(DcMotor.class, "feeder");
        feeder.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        RightrampServo   = hardwareMap.get(Servo.class, "rightRampServo");
        RightrampServo.setDirection(Servo.Direction.REVERSE);
        LeftrampServo    = hardwareMap.get(Servo.class, "leftRampServo");
        FeederServo      = hardwareMap.get(Servo.class, "feederServo");
        TurretLimeServo  = hardwareMap.get(Servo.class, "limeServo");
        GateServo        = hardwareMap.get(Servo.class, "gateServo");

        //FEEDER PARA DENTRO && LIMELIGHT FORWARD

        LeftrampServo.setPosition(InitPosFeeder);
        RightrampServo.setPosition(InitPosFeeder);
        TurretLimeServo.setPosition(InitPosLime);
        GateServo.setPosition(GateClosed);

        //CAM

        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
    }

    public void Trackinglimelight(LLResult resultado, double Tx){


        //>>Defining Position of Turret's using LimeServo<<

        double CurrentPoslime = TurretLimeServo.getPosition();

        act = LimeStates.NONE;

        if (resultado != null && resultado.isValid() ) {

            if (Tx < -5){ act = LimeStates.LEFT; }
            else if (Tx >  5){ act = LimeStates.RIGHT;}
            else{ act = LimeStates.SEEING; }

        }


            //Ver o lado correto de giro do servo
        switch (act){

           case LEFT:

                TurretLimeServo.setPosition(CurrentPoslime + AligmentLime);

                break;

           case RIGHT:

                TurretLimeServo.setPosition(CurrentPoslime - AligmentLime);

                break;

           case SEEING:

                shooter.setPower(1);
                sleep(TimeToShoot);

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


        return 1 * (1.63-Sin(Angle) - Hrobot/HAprilTag);

    }
    public double Sin(double anglin){

        Double value = tabelaValorSin.get((double) Math.round(anglin));
        if ( value == null ){
            return 0;
        }

        return value; }

}
