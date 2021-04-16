package swaiotos.channel.iot.ss.server.data.log;

import java.io.Serializable;
import java.util.List;

/**
 */
public class ReportData implements Serializable {

    public Header header;
    public PayLoadData payload;

    public static class Header implements Serializable{
        public String tag;
        public long timestamp;
        public ClientData client;
    }

    public static class ClientData implements Serializable{
        public String udid;
        public String mac;
        public String chip;
        public String model;
        public String brand;
        public String sysVersion;
        public String appVersion;
        public String skyform; //产品形态
    }

    public static class PayLoadData<T> implements Serializable{
        public List<EventData<T>> events;
    }

    public static class EventData<T> implements Serializable{
        public String eventName;
        public long eventTime;
        public T data;
    }

}
