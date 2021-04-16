package swaiotos.channel.iot.ss.config;

import android.text.TextUtils;
import android.util.Log;

public class PortConfig {
    private static final int FILE_SERVER_PORT = 37019;  //文件web服务器默认端口
    private static final int STREAM_PORT = 34000;  //本地im server 默认端口


    public static int getWebServerPort(String packageName) {
        int port = FILE_SERVER_PORT;
        if (!TextUtils.isEmpty(packageName)) {
            if (packageName.equals("swaiotos.channel.iot")
                    || packageName.equals("com.coocaa.smartscreen")) {
                port = FILE_SERVER_PORT;
            } else if (packageName.equals("com.skyworth.smartsystem.vhome")) {
                port = FILE_SERVER_PORT + 10000;
            }
        }

        Log.d("port", packageName + " getWebServerPort port=" + port);
        return port;

    }

    public static int getLocalServerPort(String packageName) {
        int port = STREAM_PORT;
        if (!TextUtils.isEmpty(packageName)) {
            if (packageName.equals("swaiotos.channel.iot")
                    || packageName.equals("com.coocaa.smartscreen")) {
                port = STREAM_PORT;
            } else if (packageName.equals("com.skyworth.smartsystem.vhome")) {
                port = STREAM_PORT + 10000;
            }
        }

        Log.d("port", packageName + " getLocalServerPort port=" + port);
        return port;

    }


}
