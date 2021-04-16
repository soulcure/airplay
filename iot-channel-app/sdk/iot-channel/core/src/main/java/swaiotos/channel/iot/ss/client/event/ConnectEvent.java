package swaiotos.channel.iot.ss.client.event;

import java.io.Serializable;

public class ConnectEvent implements Serializable {
    public boolean isConnect;  //true 重连成功  //false 重连失败

    public ConnectEvent(boolean isConnect) {
        this.isConnect = isConnect;
    }

}
