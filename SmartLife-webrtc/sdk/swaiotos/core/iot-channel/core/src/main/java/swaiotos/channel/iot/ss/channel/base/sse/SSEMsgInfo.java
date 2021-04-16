package swaiotos.channel.iot.ss.channel.base.sse;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.channel.base.sse
 * @ClassName: SSEMsgInfo
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2021/1/22 17:06
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/1/22 17:06
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SSEMsgInfo {
    private SSEChannel.SendMessageCallBack messageCallBack;
    private long time;

    public SSEChannel.SendMessageCallBack getMessageCallBack() {
        return messageCallBack;
    }

    public void setMessageCallBack(SSEChannel.SendMessageCallBack messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
