package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: PushValidCode
 * @Description: push消息体
 * @Author: wangyuehui
 * @CreateDate: 2020/4/23 19:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/23 19:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PushValidCode {
    private int from;
    private IotChannel iot_chanel;
    private String msgType;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public IotChannel getIot_chanel() {
        return iot_chanel;
    }

    public void setIot_chanel(IotChannel iot_chanel) {
        this.iot_chanel = iot_chanel;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public static class IotChannel {
          private String cmd;
          private Valid data;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public Valid getData() {
            return data;
        }

        public void setData(Valid data) {
            this.data = data;
        }
    }
    public static class Valid {
        private String validCode;

        public String getValidCode() {
            return validCode;
        }

        public void setValidCode(String validCode) {
            this.validCode = validCode;
        }
    }

}
