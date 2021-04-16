package swaiotos.sensor.data;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class ChannelEvent implements Serializable {
    public String content;

    public ChannelEvent() {

    }

    public ChannelEvent(String c) {
        this.content = c;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ChannelEvent{");
        sb.append("content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
