package com.coocaa.whiteboard.server;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import swaiotos.sensor.data.ClientCmdInfo;

public class WhiteBoardServerSSEvent implements Serializable {
    public String clientSource;
    public ClientCmdInfo info;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
