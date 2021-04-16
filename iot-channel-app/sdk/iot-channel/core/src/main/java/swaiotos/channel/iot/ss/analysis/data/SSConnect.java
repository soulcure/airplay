package swaiotos.channel.iot.ss.analysis.data;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.analysis.data
 * @ClassName: SSeMsgError
 * @Description: 发送sse消息失败消息体
 * @Author: wangyuehui
 * @CreateDate: 2020/12/23 17:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/23 17:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SSConnect {
    public static final String EVENT_NAME = "sseConnectTime";
    private String sourceLsid;
    private String targetLsid;
    private long time;                                      //消息来回耗时，毫秒；
    private String deviceType;//上报终端类型，mobile(手机）、dongle、panel；
    private String wifiSSID;  //wifi名称

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

    public String getTargetLsid() {
        return targetLsid;
    }

    public void setTargetLsid(String targetLsid) {
        this.targetLsid = targetLsid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
