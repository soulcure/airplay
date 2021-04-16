package com.coocaa.whiteboard.server;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class ClientInServerCanvasInfo implements Serializable {
    //该手机在服务端画布中的信息
    public int cX = 0;
    public int cY = 0;
    public int cWidth = 1920;
    public int cHeight = 1080;
    public float cScale = 1f;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
