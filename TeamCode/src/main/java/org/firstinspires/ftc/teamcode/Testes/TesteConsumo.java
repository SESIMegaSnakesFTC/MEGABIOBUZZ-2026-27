package org.firstinspires.ftc.teamcode.Testes;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.teamcode.Mech.LimeLight;
import org.firstinspires.ftc.teamcode.Mech.PinpointConf;


@TeleOp(name= "Teste de consumo", group = "Consume")
public class TesteConsumo extends LinearOpMode {

    private DcMotor motor1, motor2, motor3, motor4, motor5, motor6, motor7, motor8;
    private CRServo servo1, servo2, servo3, servo4;
    private PinpointLocalizer pimpas;
    private final LimeLight limeLight = new LimeLight();





    public void runOpMode(){

        VoltageSensor vs = hardwareMap.voltageSensor.iterator().hasNext()
                ? hardwareMap.voltageSensor.iterator().next() : null;


        iniciandoTudo();
        limeLight.init(hardwareMap);
        limeLight.TurnON(true, true);
        limeLight.setSide(true, true);


        waitForStart();
        while(opModeIsActive()){

            explodir();


            Pose pose = pimpas.pose();
            double x = pimpas.pose().x();
            double y = pimpas.pose().y();
            double voltage = (vs != null) ? vs.getVoltage() : 12;
            LLResult result  =  limeLight.getResult();
            LLStatus status =  limeLight.getStatus();

            telemetry.addData("Tensão/Corrente", voltage);
            telemetry.addData("result", result);
            telemetry.addData("Status", status);
            telemetry.addData("X", x);
            telemetry.addData("Y", y);
            telemetry.update();
        }
    }
    private void iniciandoTudo(){
        motor1 = hardwareMap.get(DcMotor.class, "motor1");
        motor2 = hardwareMap.get(DcMotor.class, "motor2");
        motor3 = hardwareMap.get(DcMotor.class, "motor3");
        motor4 = hardwareMap.get(DcMotor.class, "motor4");
        motor5 = hardwareMap.get(DcMotor.class, "motor5");
        motor6 = hardwareMap.get(DcMotor.class, "motor6");
        motor7 = hardwareMap.get(DcMotor.class, "motor7");
        motor8 = hardwareMap.get(DcMotor.class, "motor8");

        servo1 = hardwareMap.get(CRServo.class, "servo1");
        servo2 = hardwareMap.get(CRServo.class, "servo2");
        servo3 = hardwareMap.get(CRServo.class, "servo3");
        servo4 = hardwareMap.get(CRServo.class, "servo4");

        pimpas = PinpointConf.create(hardwareMap);


    }

    private void explodir(){
        motor1.setPower(0.9);
        motor2.setPower(0.9);
        motor3.setPower(0.9);
        motor4.setPower(0.9);
        motor5.setPower(0.9);
        motor6.setPower(0.9);
        motor7.setPower(0.9);
        motor8.setPower(0.9);

        servo1.setPower(1);
        servo2.setPower(1);
        servo3.setPower(1);
        servo4.setPower(1);
    }

}
