package swaiotos.channel.iot.client;

import android.content.ComponentName;

public class Client {
    public static final int TYPE_ACTIVITY = 0;
    public static final int TYPE_SERVICE = 1;
    public final int type;
    public final ComponentName cn;
    public final int version;

    public Client(int type, ComponentName cn, int version) {
        this.type = type;
        this.cn = cn;
        this.version = version;
    }
}
