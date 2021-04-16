package com.coocaa.movie.util;

import com.coocaa.smartscreen.connect.SSConnectManager;

public class MovieConstant {

    public static int source_id = 1; //qq:5,iqiyi 1

    public static String PAY_DOMAIN = "http://business.video.tc.skysrt.com/";

    public static String getSource() {
        return SSConnectManager.getInstance().getVideoSource();
    }
}
