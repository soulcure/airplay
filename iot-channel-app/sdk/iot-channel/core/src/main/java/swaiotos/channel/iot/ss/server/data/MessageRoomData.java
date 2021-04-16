package swaiotos.channel.iot.ss.server.data;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.server.data
 * @ClassName: MessageRoomData
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/23 17:31
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/23 17:31
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MessageRoomData {
    private String id;
    private String data;

    public static class MessageData {
        private String id;
        @JSONField(name="client-source")
        private String client_source;
        @JSONField(name="client-target")
        private String client_target;
        private String type;
        private String content;
        private Map<String, String> extra;
        private boolean reply;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClient_source() {
            return client_source;
        }

        public void setClient_source(String client_source) {
            this.client_source = client_source;
        }

        public String getClient_target() {
            return client_target;
        }

        public void setClient_target(String client_target) {
            this.client_target = client_target;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, String> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, String> extra) {
            this.extra = extra;
        }

        public boolean isReply() {
            return reply;
        }

        public void setReply(boolean reply) {
            this.reply = reply;
        }
        //        public int getReply() {
//            return reply;
//        }
//
//        public void setReply(byte reply) {
//            this.reply = reply;
//        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
