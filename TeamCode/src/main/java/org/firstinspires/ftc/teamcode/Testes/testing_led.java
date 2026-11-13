package org.firstinspires.ftc.teamcode.Testes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Mech.Led.ColorPrism;
import org.firstinspires.ftc.teamcode.Mech.Led.GoBildaPrismDriver;
import org.firstinspires.ftc.teamcode.Mech.Led.PrismAnimations;
import org.firstinspires.ftc.teamcode.Mech.Led.RGB_Indicator;

@TeleOp(name = "Led - Test")
public class testing_led extends LinearOpMode {

    public void runOpMode(){


        GoBildaPrismDriver prism = hardwareMap.tryGet(GoBildaPrismDriver.class, "prism");
        RGB_Indicator indicator = new RGB_Indicator();
        indicator.init(hardwareMap);

        if (prism == null) {
            telemetry.addLine("ERRO: nao achei 'prism' como goBILDA Prism na config.");
            telemetry.addLine("Configure Robot > I2C Bus > goBILDA® Prism RGB LED Driver, nome: prism");
            HardwareDevice outro = hardwareMap.tryGet(HardwareDevice.class, "prism");
            telemetry.addData("'prism' configurado como", outro == null ? "nada" : outro.getDeviceName());
            telemetry.update();
            waitForStart();
        }

        prism.clearAllAnimations();
        telemetry.addData("Firmware", prism.getFirmwareVersionString());
        telemetry.addData("LEDs", prism.getNumberOfLEDs());
        telemetry.update();

        waitForStart();

        ColorPrism cor = ColorPrism.TRANSPARENT;
        int brilho = 100;
        boolean upAntes = false, downAntes = false;

        while (opModeIsActive()){

            //INDICATOR
            indicator.setColor(gamepad1.left_stick_button, gamepad2.b, gamepad2.y, gamepad2.a, gamepad2.x);

            //PRISM
            ColorPrism nova = cor;
            int novoBrilho = brilho;

            if (gamepad1.a)       nova = ColorPrism.BLUE;
            if (gamepad1.y)       nova = ColorPrism.GREEN;
            if (gamepad1.x)       nova = ColorPrism.RED;
            if (gamepad1.b)       nova = ColorPrism.YELLOW;
            if (gamepad1.left_stick_button) nova = ColorPrism.TRANSPARENT;
            if (gamepad1.dpad_up && !upAntes)     novoBrilho = Math.min(100, brilho + 10);
            if (gamepad1.dpad_down && !downAntes) novoBrilho = Math.max(0, brilho - 10);
            upAntes = gamepad1.dpad_up;
            downAntes = gamepad1.dpad_down;

            // só manda pelo I2C quando algo muda
            if (nova != cor || novoBrilho != brilho) {
                cor = nova;
                brilho = novoBrilho;
                prism.insertAndUpdateAnimation(GoBildaPrismDriver.LayerHeight.LAYER_0, new PrismAnimations.Solid(cor, brilho));
            }

            telemetry.addData("Brilho", brilho);
            telemetry.update();
        }
    }
}
