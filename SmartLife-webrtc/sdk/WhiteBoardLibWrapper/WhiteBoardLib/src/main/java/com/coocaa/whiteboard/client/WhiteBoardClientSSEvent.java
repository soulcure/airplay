package com.coocaa.whiteboard.client;

import com.alibaba.fastjson.JSON;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;

import java.io.Serializable;

import swaiotos.sensor.data.ClientCmdInfo;

public class WhiteBoardClientSSEvent implements Serializable {
    public String clientSource;
    public WhiteBoardServerCmdInfo info;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
