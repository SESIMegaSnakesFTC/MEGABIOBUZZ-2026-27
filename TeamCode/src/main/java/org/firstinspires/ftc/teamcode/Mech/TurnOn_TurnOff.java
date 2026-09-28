package org.firstinspires.ftc.teamcode.Mech;

public class TurnOn_TurnOff {


    public boolean IsActivate(boolean CurrentPressed,
                              boolean LastPressed,
                              boolean Status){

        if (CurrentPressed && !LastPressed){
            return !Status;
        }
        return Status;
    }
}
