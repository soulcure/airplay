package swaiotos.channel.iot.ss.analysis.data;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.analysis.data
 * @ClassName: ServerInterfaceMsg
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/12/23 19:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/23 19:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ServerInterfaceMsgError {
    public static final String EVENT_NAME = "serverInterfaceMsgError";
    private String sourceLsid;
    private String method;
    private String errorCode;
    private String errorDsc;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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
