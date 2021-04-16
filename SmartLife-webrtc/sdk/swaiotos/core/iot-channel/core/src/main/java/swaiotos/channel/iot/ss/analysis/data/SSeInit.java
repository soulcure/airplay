package swaiotos.channel.iot.ss.analysis.data;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.analysis
 * @ClassName: LocalConnectSuccessData
 * @Description: 本地连接失败信息
 * @Author: wangyuehui
 * @CreateDate: 2020/12/23 14:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/23 14:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SSeInit {
    public static final String EVENT_NAME = "sseInitTime";
    private String sourceLsid;
    private String deviceType;//上报终端类型，mobile(手机）、dongle、panel；
    private String wifiSSID;  //wifi名称
    private long time;


    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSourceLsid() {
        return sourceLsid;
    }

    public void setSourceLsid(String sourceLsid) {
        this.sourceLsid = sourceLsid;
    }


    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
