package com.coocaa.smartscreen.repository.http.home;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: yuzhan
 */
public class CcLogData implements Serializable {

    public XHeader header;
    public XPlayload payload;

    public static class XHeader implements Serializable{
        public XClient client;
        public String tag;
        public long timestamp;
    }

    public static class XClient implements Serializable {
        public String appVersion;
        public String sysVersion;
        public String udid;
        public String brand;
        public String appVersionName;
    }

    public static class XPlayload implements Serializable {
        public List<XEvents> events;
    }

    public static class XEvents implements Serializable {
        public Object data;
        public String eventName;
        public long eventTime;
    }
}
