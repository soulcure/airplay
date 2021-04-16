package swaiotos.channel.iot.ss.server.data;

import java.util.ArrayList;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.server.data
 * @ClassName: ReportLog
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/10/22 17:47
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/10/22 17:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ReportLog {

    private Header header;
    private ArrayList<Logs> logs;

    public static class Header {
        private String event_type;
        private String client;
        private String platform;
        private int timestamp;
        private String tag;

        public String getEvent_type() {
            return event_type;
        }

        public void setEvent_type(String event_type) {
            this.event_type = event_type;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(int timestamp) {
            this.timestamp = timestamp;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static  class Logs {
        private String message;
        private String level;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public ArrayList<Logs> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<Logs> logs) {
        this.logs = logs;
    }
}
