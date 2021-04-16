package com.coocaa.whiteboard.client;

import com.alibaba.fastjson.JSON;

public class ClientCanvasInfo {
    public int x = 0;
    public int y = 0;
    public float scale = 1f;

    public void set(ClientCanvasInfo info) {
        if(info == null)
            return ;
        this.x = info.x;
        this.y = info.y;
        this.scale = info.scale;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
