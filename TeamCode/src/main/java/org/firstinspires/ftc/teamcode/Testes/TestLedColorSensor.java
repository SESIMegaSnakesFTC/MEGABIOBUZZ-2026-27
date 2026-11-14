package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Mech.Color_Sensor;
import org.firstinspires.ftc.teamcode.Mech.Led.RGB_Indicator;
import org.firstinspires.ftc.teamcode.Mech.TurnOn_TurnOff;

@TeleOp(name = "Led&ColorSensor")
public class TestLedColorSensor extends LinearOpMode {

    public void runOpMode(){

        Color_Sensor sensor = new Color_Sensor();
        RGB_Indicator indicator = new RGB_Indicator();
        TurnOn_TurnOff logicButton = new TurnOn_TurnOff();


        sensor.init(hardwareMap,0.5f);
        indicator.init(hardwareMap);

        indicator.setColor(true, false, false, false, false);

        boolean A_ON = false, lastA = false;
        boolean lastOptions = false;

        waitForStart();

        while(opModeIsActive()){

            A_ON = logicButton.IsActivate(gamepad1.a, lastA, A_ON);
            lastA = gamepad1.a;

            if (gamepad1.options && !lastOptions) sensor.ResetBallCounter();
            lastOptions = gamepad1.options;

            int Qnt_Ball = sensor.CountBall(A_ON, 40, 70, 0.5f);

            switch (Qnt_Ball){

                case 0:

                    indicator.setColor(true, false, false, false, false);
                    break;

                case 1:

                    indicator.setColor(false, false, false, false, true);
                    break;

                case 2:

                    indicator.setColor(false, true, false, false, false);
                    break;

                case 3:

                    indicator.setColor(false, false, true, false, false);
                    break;

                case 4:

                    indicator.setColor(false, false, false, true, false);
                    break;
            }



        }
    }
}
