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
public class SSeInitError {
    public static final String EVENT_NAME = "sseInitError";
    private String sourceLsid;
    private String errorCode;//返回的错误码，或者异常；
    private String errorDsc;//错误描述（选填），若太大，则不建议上报；
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDsc() {
        return errorDsc;
    }

    public void setErrorDsc(String errorDsc) {
        this.errorDsc = errorDsc;
    }
}
