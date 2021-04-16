package swaiotos.channel.iot.common.push;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.push
 * @ClassName: PushMsg
 * @Description: push消息体
 * @Author: wangyuehui
 * @CreateDate: 2020/4/13 20:25
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/13 20:25
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PushMsg {
    private int from;
    private IotChannelMsg iot_chanel;
    private String msgType;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public IotChannelMsg getIot_chanel() {
        return iot_chanel;
    }

    public void setIot_chanel(IotChannelMsg iot_chanel) {
        this.iot_chanel = iot_chanel;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public static class IotChannelMsg {
        private String cmd;
        private IotPushMsg data;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public IotPushMsg getData() {
            return data;
        }

        public void setData(IotPushMsg data) {
            this.data = data;
        }
    }

    public static class IotPushMsg {
        private String activeId;
        private String lsid;
        private String nickname;
        private String pushToken;

        public String getActiveId() {
            return activeId;
        }

        public void setActiveId(String activeId) {
            this.activeId = activeId;
        }

        public String getLsid() {
            return lsid;
        }

        public void setLsid(String lsid) {
            this.lsid = lsid;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getPushToken() {
            return pushToken;
        }

        public void setPushToken(String pushToken) {
            this.pushToken = pushToken;
        }
    }
}
