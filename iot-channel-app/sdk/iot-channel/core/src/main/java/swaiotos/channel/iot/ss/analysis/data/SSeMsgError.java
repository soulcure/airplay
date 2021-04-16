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
public class SSeMsgError {
    public static final String EVENT_NAME = "sseMsgError";
    public static final String CONNECT = "Connect";
    public static final String SENDMSG = "SendMsg";
    private String sourceLsid;
    private String targetLsid;
    private String msgID;//消息ID；
    private String msgType;//消息类型；
    private String errorCode;//返回的错误码，或者异常；
    private String errorDsc;//错误描述（选填），若太大，则不建议上报；
    private String cmdType;//命令类型（Connect,SendMsg)
    private String deviceType;//上报终端类型，mobile(手机）、dongle、panel；
    private String wifiSSID;  //wifi名称
    private String content; //新增内容上报

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

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
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

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
