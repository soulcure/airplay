package swaiotos.channel.iot.ss.server.data;

import java.io.Serializable;
import java.util.List;

public class ScreenApps implements Serializable {
    public long timestamp;
    public List<AppItem> app_list;

    public static class AppItem implements Serializable {
        public String pkgname;
        public List<AppWay> app_way;
    }

    public static class AppWay implements Serializable {
        public String pkgname;
        public String type;
        public String className;
        public String mediaType;
    }
}
