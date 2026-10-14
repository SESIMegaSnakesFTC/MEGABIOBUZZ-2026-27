package org.firstinspires.ftc.teamcode.Mech;

import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class PinpointConf {

    public static final String HARDWARE_NAME = "pinpoint";


    public static final double X_POD_OFFSET_IN = 2.5061;
    public static final double Y_POD_OFFSET_IN = -5.6249;

    public static PinpointConfig config = new PinpointConfig(
            c -> {
                c.name.set(HARDWARE_NAME);
                c.xPodOffset.set(X_POD_OFFSET_IN);
                c.yPodOffset.set(Y_POD_OFFSET_IN);
                c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
                c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
            }
    );

    public static PinpointLocalizer create(HardwareMap hardwareMap) {
        return new PinpointLocalizer(hardwareMap, config);
    }

}