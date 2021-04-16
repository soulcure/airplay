package com.coocaa.tvpi.event;

/**
 * 电视机摄像头是否可旋转
 * Created by songxing on 2020/4/8
 */
public class TVCameraRotatableEvent {
    public boolean isRotatable;
    public TVCameraRotatableEvent(boolean isRotatable) {
        this.isRotatable = isRotatable;
    }
}
