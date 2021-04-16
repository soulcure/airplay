package swaiotos.channel.iot.ss.client.event;

import java.io.Serializable;

public class BindCodeEvent implements Serializable {
    public String bindCode;
    public String tempUrl;

    public BindCodeEvent(String bindCode) {
        this.bindCode = bindCode;
    }

    public BindCodeEvent(String bindCode,String tempUrl) {
        this.bindCode = bindCode;
        this.tempUrl = tempUrl;
    }

}
