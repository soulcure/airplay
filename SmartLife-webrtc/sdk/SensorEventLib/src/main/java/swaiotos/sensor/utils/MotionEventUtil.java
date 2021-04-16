package swaiotos.sensor.utils;

import android.os.SystemClock;
import android.view.MotionEvent;

import swaiotos.sensor.input.TouchData;

/**
 * @ClassName: MotionEventUtil
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/20 14:14
 */
public class MotionEventUtil {

    public static MotionEvent formatMotionEvent(TouchData touchData) {

        long clientDownTime = touchData.getDownTime();
        long clientEventTime = touchData.getEventTime();

        long tempTime = clientEventTime - clientDownTime;


        long downTime = SystemClock.uptimeMillis();
        long eventTime = downTime + tempTime;


        MotionEvent event = MotionEvent
                .obtain(downTime,
                        eventTime,
                        touchData.getAction(),
                        touchData.getPointerCount(),
                        touchData.getProperties(),
                        touchData.getPointerCoords(),
                        touchData.getMetaState(),
                        touchData.getButtonState(),
                        touchData.getxPrecision(),
                        touchData.getyPrecision(),
                        touchData.getDeviceId(),
                        touchData.getEdgeFlags(),
                        touchData.getSource(),
                        touchData.getFlags());

        return event;
    }

    public static String formatTouchEvent(MotionEvent event) {
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
        int historySize = event.getHistorySize();
        data.historyX = new float[historySize];
        data.historyY = new float[historySize];
        for(int i=0; i<historySize; i++) {
            data.historyX[i] = event.getHistoricalX(i);
            data.historyY[i] = event.getHistoricalY(i);
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

}
