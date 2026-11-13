package org.firstinspires.ftc.teamcode.Mech.Led;

import com.qualcomm.robotcore.hardware.HardwareMap;

public class Prism {

    GoBildaPrismDriver prism;

    ColorPrism cor = ColorPrism.TRANSPARENT;
    public int brilho = 100;
    boolean upAntes = false, downAntes = false;


    public void init(HardwareMap hardwareMap){

        prism = hardwareMap.get(GoBildaPrismDriver.class, "prism");
        prism.clearAllAnimations();
    }

    public void setColor(boolean Blue, boolean Yellow, boolean Red, boolean Green, boolean Off, boolean up, boolean down){

        ColorPrism nova = cor;
        int novoBrilho = brilho;

        if (Blue)   nova = ColorPrism.BLUE;
        if (Green)  nova = ColorPrism.GREEN;
        if (Red)    nova = ColorPrism.RED;
        if (Yellow) nova = ColorPrism.YELLOW;
        if (Off)    nova = ColorPrism.TRANSPARENT;
        // brilho muda 10 por clique (só na borda de subida)
        if (up && !upAntes)     novoBrilho = Math.min(100, brilho + 10);
        if (down && !downAntes) novoBrilho = Math.max(0, brilho - 10);
        upAntes = up;
        downAntes = down;

        // só manda pelo I2C quando algo muda, pra não travar o loop
        if (nova != cor || novoBrilho != brilho) {
            cor = nova;
            brilho = novoBrilho;
            prism.insertAndUpdateAnimation(GoBildaPrismDriver.LayerHeight.LAYER_0, new PrismAnimations.Solid(cor, brilho));
        }
    }
}
