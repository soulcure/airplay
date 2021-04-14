package com.swaiotos.skymirror.sdk.data;

import android.view.MotionEvent;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * @ClassName: TouchData
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/16 15:55
 */
public class TouchData {

    private long downTime;
    private long eventTime;
    private int action;
    private int pointerCount;
    private MotionEvent.PointerProperties[] properties;
    private MotionEvent.PointerCoords[] pointerCoords;
    private int metaState;
    private int buttonState;
    private float xPrecision;
    private float yPrecision;
    private int deviceId;
    private int edgeFlags;
    private int source;
    private int flags;

    public long getDownTime() {
        return downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getPointerCount() {
        return pointerCount;
    }

    public void setPointerCount(int pointerCount) {
        this.pointerCount = pointerCount;
    }

    public MotionEvent.PointerProperties[] getProperties() {
        return properties;
    }

    public void setProperties(MotionEvent.PointerProperties[] properties) {
        this.properties = properties;
    }

    public MotionEvent.PointerCoords[] getPointerCoords() {
        return pointerCoords;
    }

    public void setPointerCoords(MotionEvent.PointerCoords[] pointerCoords) {
        this.pointerCoords = pointerCoords;
    }

    public int getMetaState() {
        return metaState;
    }

    public void setMetaState(int metaState) {
        this.metaState = metaState;
    }

    public int getButtonState() {
        return buttonState;
    }

    public void setButtonState(int buttonState) {
        this.buttonState = buttonState;
    }

    public float getxPrecision() {
        return xPrecision;
    }

    public void setxPrecision(float xPrecision) {
        this.xPrecision = xPrecision;
    }

    public float getyPrecision() {
        return yPrecision;
    }

    public void setyPrecision(float yPrecision) {
        this.yPrecision = yPrecision;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getEdgeFlags() {
        return edgeFlags;
    }

    public void setEdgeFlags(int edgeFlags) {
        this.edgeFlags = edgeFlags;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }


    @Override
    public String toString() {
        return "TouchData{" +
                "downTime=" + downTime +
                ", eventTime=" + eventTime +
                ", action=" + action +
                ", pointerCount=" + pointerCount +
                ", properties=" + Arrays.toString(properties) +
                ", pointerCoords=" + Arrays.toString(pointerCoords) +
                ", metaState=" + metaState +
                ", buttonState=" + buttonState +
                ", xPrecision=" + xPrecision +
                ", yPrecision=" + yPrecision +
                ", deviceId=" + deviceId +
                ", edgeFlags=" + edgeFlags +
                ", source=" + source +
                ", flags=" + flags +
                '}';
    }
}
