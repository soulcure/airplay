package swaiotos.channel.iot.utils;

import android.os.Environment;

import java.io.File;

public class AppConfig {

    /**
     * pref文件名定义
     */
    public static final String SHARED_PREFERENCES = "nuu_info";

    public static String getIp() {//配置默认ip
        return "119.23.74.49";
    }

    public static int getPort() {//配置默认端口
        return 18990;
    }

    public static String getRouterHost() {
        return "http://47.91.250.107:80";
    }


    public static String getRouterPath() {
        return "/api/v1/api_device";
    }


    /**
     * 配置文件路径
     */
    public static String getConfigFilePath() {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "nuuinfo.json";
    }


    public static String getReportFilePath() {//配置默认日志目录
        return Environment.getExternalStorageDirectory().getPath() + File.separator + "nuu";
    }


    public static int getSendReportRate() {//默认10分钟上报一次
        /*if (BuildConfig.DEBUG) {
            return 60;
        }*/
        return 10 * 60;
    }

    public static int getReportStoreKeepDays() {//默认文件保存30天以内的
        /*if (BuildConfig.DEBUG) {
            return 2;
        }*/
        return 30;
    }

    public static int getObtainReportRate() {//默认2分钟,生成设备信息
        /*if (BuildConfig.DEBUG) {
            return 30;
        }*/
        return 2 * 60;
    }


}
