package com.coocaa.swaiotos.virtualinput.iot;

import android.view.MotionEvent;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * @Author: yuzhan
 */
class TouchData {
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

    public TouchData() {
    }

    public long getDownTime() {
        return this.downTime;
    }

    public void setDownTime(long downTime) {
        this.downTime = downTime;
    }

    public long getEventTime() {
        return this.eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getPointerCount() {
        return this.pointerCount;
    }

    public void setPointerCount(int pointerCount) {
        this.pointerCount = pointerCount;
    }

    public MotionEvent.PointerProperties[] getProperties() {
        return this.properties;
    }

    public void setProperties(MotionEvent.PointerProperties[] properties) {
        this.properties = properties;
    }

    public MotionEvent.PointerCoords[] getPointerCoords() {
        return this.pointerCoords;
    }

    public void setPointerCoords(MotionEvent.PointerCoords[] pointerCoords) {
        this.pointerCoords = pointerCoords;
    }

    public int getMetaState() {
        return this.metaState;
    }

    public void setMetaState(int metaState) {
        this.metaState = metaState;
    }

    public int getButtonState() {
        return this.buttonState;
    }

    public void setButtonState(int buttonState) {
        this.buttonState = buttonState;
    }

    public float getxPrecision() {
        return this.xPrecision;
    }

    public void setxPrecision(float xPrecision) {
        this.xPrecision = xPrecision;
    }

    public float getyPrecision() {
        return this.yPrecision;
    }

    public void setyPrecision(float yPrecision) {
        this.yPrecision = yPrecision;
    }

    public int getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getEdgeFlags() {
        return this.edgeFlags;
    }

    public void setEdgeFlags(int edgeFlags) {
        this.edgeFlags = edgeFlags;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String toJson() {
        return (new Gson()).toJson(this);
    }

    public String toString() {
        return "TouchData{downTime=" + this.downTime + ", eventTime=" + this.eventTime + ", action=" + this.action + ", pointerCount=" + this.pointerCount + ", properties=" + Arrays.toString(this.properties) + ", pointerCoords=" + Arrays.toString(this.pointerCoords) + ", metaState=" + this.metaState + ", buttonState=" + this.buttonState + ", xPrecision=" + this.xPrecision + ", yPrecision=" + this.yPrecision + ", deviceId=" + this.deviceId + ", edgeFlags=" + this.edgeFlags + ", source=" + this.source + ", flags=" + this.flags + '}';
    }
}
