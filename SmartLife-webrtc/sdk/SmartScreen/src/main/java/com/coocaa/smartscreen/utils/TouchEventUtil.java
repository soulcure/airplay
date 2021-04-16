package com.coocaa.smartscreen.utils;

import android.view.MotionEvent;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @Author: yuzhan
 */
public class TouchEventUtil {

    public static String formatTouchEvent(MotionEvent event, int or) {
        TouchData data = new TouchData();
        data.setDownTime(event.getDownTime());
        data.setEventTime(event.getEventTime());
        data.setAction(event.getAction());
        int count = event.getPointerCount();
        data.setPointerCount(count);
        MotionEvent.PointerProperties[] mProperties = new MotionEvent.PointerProperties[count];
        MotionEvent.PointerCoords[] mCoords = new MotionEvent.PointerCoords[count];
        for (int i = 0; i < count; i++) {
            //create properties
            MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
            properties.id = event.getPointerId(i);
            properties.toolType = event.getToolType(i);
            mProperties[i] = properties;
            //create coords
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.orientation = event.getOrientation(i);
            coords.pressure = event.getPressure(i);
            coords.size = event .getSize(i);
            coords.toolMajor = event.getToolMajor(i);
            coords.touchMajor = event.getTouchMajor(i);
            coords.touchMinor = event.getTouchMinor(i);
            coords.toolMinor = event.getToolMinor(i);
            coords.y = event.getY(i);
            coords.x = event.getX(i);
            mCoords[i] = coords;
        }
        data.setProperties(mProperties);
        data.setPointerCoords(mCoords);
        data.setMetaState(event.getMetaState());
        data.setButtonState(event.getButtonState());
        data.setxPrecision(event.getXPrecision());
        data.setyPrecision(event.getYPrecision());
        data.setDeviceId(event.getDeviceId());
        data.setEdgeFlags(event.getEdgeFlags());
        data.setSource(event.getSource());
        data.setFlags(event.getFlags());
        return data.toJson();
    }


    public static class TouchData implements Serializable {
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
}
