package swaiotos.sensor.server;

import java.io.Serializable;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * @Author: yuzhan
 */
public class MessageEventData implements Serializable {

    public IMMessage message;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("MessageEventData{");
        sb.append("message=").append(message);
        sb.append('}');
        return sb.toString();
    }
}
