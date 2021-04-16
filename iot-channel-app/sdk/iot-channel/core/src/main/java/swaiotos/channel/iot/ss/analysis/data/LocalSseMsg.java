package swaiotos.channel.iot.ss.analysis.data;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.analysis
 * @ClassName: LocalConnectSuccessData
 * @Description: 本地通道消息事件耗时
 * @Author: wangyuehui
 * @CreateDate: 2020/12/23 14:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/23 14:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class LocalSseMsg {
    public static final String EVENT_NAME_LOCAL = "localMsgTime";
    public static final String EVENT_NAME_SSE = "sseMsgTime";
    private String sourceLsid;
    private float time;
    private float lost;
    private String deviceType;  //上报终端类型，mobile(手机）、dongle、panel；
    private int count;          //连接个数
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

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getLost() {
        return lost;
    }

    public void setLost(float lost) {
        this.lost = lost;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
