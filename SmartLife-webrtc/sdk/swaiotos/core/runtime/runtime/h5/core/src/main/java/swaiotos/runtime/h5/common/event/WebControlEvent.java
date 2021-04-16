package swaiotos.runtime.h5.common.event;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class WebControlEvent implements Serializable {
    public String cmd;
    public String extra;
    public IMMessage message;

    public WebControlEvent() {

    }

    public WebControlEvent(String cmd, String extra) {
        this.cmd = cmd;
        this.extra = extra;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
