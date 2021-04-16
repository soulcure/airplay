package com.coocaa.whiteboard.server;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class ServerCanvasInfo implements Serializable {
    //服务端画布信息数据
    public int maxWidth = WhiteBoardServerConfig.MAX_WIDTH;
    public int maxHeight = WhiteBoardServerConfig.MAX_HEIGHT;
    public float scale = WhiteBoardServerConfig.CURRENT_SCALE;
    public int x = WhiteBoardServerConfig.CURRENT_OFFSET_X;
    public int y = WhiteBoardServerConfig.CURRENT_OFFSET_Y;

    public transient static ServerCanvasInfo INFO = new ServerCanvasInfo();

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
