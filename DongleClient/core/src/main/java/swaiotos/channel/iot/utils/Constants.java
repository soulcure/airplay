package swaiotos.channel.iot.utils;

import android.os.Build;

public class Constants {

    private static final String HDD500 = "HDD500";
    private static final String HDD600 = "HDD600";
    private static final String FARADAY = "faraday";
    private static final String CC2001 = "2A08_CC2001";


    public static boolean isDongle() {
        return Build.MODEL.equals(FARADAY)
                || Build.MODEL.equals(HDD500)
                || Build.MODEL.equals(CC2001)
                || Build.MODEL.equals(HDD600);
    }


}
