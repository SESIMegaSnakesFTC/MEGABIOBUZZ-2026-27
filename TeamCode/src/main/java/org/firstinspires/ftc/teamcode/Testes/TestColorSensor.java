package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Mech.Color_Sensor;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;


@TeleOp(name = "Test Color sensor")
public class TestColorSensor extends LinearOpMode {

    public void runOpMode(){

        double min = 40;
        double max = 70;
        double saturation = 0.5f;

        Color_Sensor sensorCor = new Color_Sensor();
        sensorCor.init(hardwareMap, 2);
        TurnOn_TurnOff logicButton = new TurnOn_TurnOff();

        //Inverter

        boolean lastA = false, A_ON = false;

        //Resetar contagem
        boolean lastOptions = false;

        //Alterar valores

        boolean lastUp1 = false;
        boolean lastDo1 = false;
        boolean lastLe1 = false;
        boolean lastRi1 = false;
        boolean lastX   = false;
        boolean lastB   = false;

        waitForStart();
        while (opModeIsActive()){

            boolean A = gamepad1.a;
            A_ON = logicButton.IsActivate(A, lastA, A_ON);
            lastA = A;

            boolean options = gamepad1.options;
            if (options && !lastOptions) sensorCor.ResetBallCounter();



            //Changing Values

            if (gamepad1.dpad_down && !lastDo1) min += 0.5;
            if (gamepad1.dpad_up   && !lastUp1) min -= 0.5;

            lastUp1 = gamepad1.dpad_up; lastDo1 = gamepad1.dpad_down;


            if (gamepad1.dpad_right && !lastRi1) max += 0.5;
            if (gamepad1.dpad_left  && !lastLe1) max -= 0.5;

            lastRi1 = gamepad1.dpad_right; lastLe1 = gamepad1.dpad_left;

            if (gamepad1.b && !lastB) saturation -= 0.5;
            if (gamepad1.x && !lastX) saturation += 0.5;

            lastX = gamepad1.x; lastB = gamepad1.b;

            //=============================================

            int countBalls = sensorCor.CountBall(A_ON /*Invertido*/,min, max, (float) saturation);
            boolean SeeingBall = sensorCor.IsThereBall(min, max, (float) saturation);


            telemetry.addData("Bolinhas", countBalls);
            telemetry.addData("Invertido", A_ON ? "Invertido" : "Normal");
            telemetry.addData("Vendo Bola", SeeingBall?"Sim":"Não");
            telemetry.addLine("==========================================");
            telemetry.addData("MIN", min);
            telemetry.addData("MAX", max);
            telemetry.addData("Sat.", saturation);
            telemetry.update();

        }
    }
}
