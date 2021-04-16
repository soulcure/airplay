package swaiotos.sensor.utils;

import android.hardware.SensorEvent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

import swaiotos.sensor.input.SensorData;

/**
 * @ClassName: MotionEventUtil
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/20 14:14
 */
public class SensorEventUtil {

    public static SensorData formatSensorEvent(String json) {
        return JSON.parseObject(json, SensorData.class);
    }

    public static String formatSensorEvent(SensorEvent event) {
        SensorData data = new SensorData();
        data.accuracy = event.accuracy;
        data.mFifoMaxEventCount = event.sensor.getFifoMaxEventCount();
        data.mFifoReservedEventCount = event.sensor.getFifoReservedEventCount();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.mId = event.sensor.getId();
        }
//        data.mMaxDelay = event.sensor.getMaxDelay();
        data.mMaxRange = event.sensor.getMaximumRange();
        data.mName = event.sensor.getName();
        data.mPower = event.sensor.getPower();
        data.mResolution = event.sensor.getResolution();
        data.mType = event.sensor.getType();
//        data.mStringType = event.sensor.getStringType();
        data.mVendor = event.sensor.getVendor();
        data.mVersion = event.sensor.getVersion();
        data.mMinDelay = event.sensor.getMinDelay();
        data.timestamp = event.timestamp;
        data.values = event.values;

        return JSON.toJSONString(data);
    }

    public static void analogUserScroll(View view, View.OnTouchListener listener, final int type, final float p1x, final float p1y, final float p2x, final float p2y) {
        Log.d("SSCClient", "正在模拟滑屏操作：p1->" + p1x + "," + p1y + ";p2->" + p2x + "," + p2y);
        if (view == null) {
            return;
        }
        long downTime = SystemClock.uptimeMillis();//模拟按下去的时间

        long eventTime = downTime;

        float pX = p1x;
        float pY = p1y;
        int speed = 0;//快速滑动
        float touchTime = 116;//模拟滑动时发生的触摸事件次数

        //平均每次事件要移动的距离
        float perX = (p2x - p1x) / touchTime;
        float perY = (p2y - p1y) / touchTime;

        boolean isReversal = perX < 0 || perY < 0;//判断是否反向：手指从下往上滑动，或者手指从右往左滑动
        boolean isHandY = Math.abs(perY) > Math.abs(perX);//判断是左右滑动还是上下滑动

        if (true) {//加速滑动
            touchTime = 10;//如果是快速滑动，则发生的触摸事件比均匀滑动更少
            speed = isReversal ? -20 : 20;//反向移动则坐标每次递减
        }

        //模拟用户按下
        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, pX, pY, 0);
        listener.onTouch(view, downEvent);
//        view.onTouchEvent(downEvent);

        //模拟移动过程中的事件
        List<MotionEvent> moveEvents = new ArrayList<>();
        boolean isSkip = false;
        for (int i = 0; i < touchTime; i++) {

            pX += (perX + speed);
            pY += (perY + speed);
            if ((isReversal && pX < p2x) || (!isReversal && pX > p2x)) {
                pX = p2x;
                isSkip = !isHandY;
            }

            if ((isReversal && pY < p2y) || (!isReversal && pY > p2y)) {
                pY = p2y;
                isSkip = isHandY;
            }
            eventTime += 20.0f;//事件发生的时间要不断递增
            MotionEvent moveEvent = MotionEvent.obtain(downTime, eventTime,
                    MotionEvent.ACTION_MOVE, pX, pY, 0);
            moveEvents.add(moveEvent);
//            view.onTouchEvent(moveEvent);
            listener.onTouch(view, moveEvent);
            if (true) {//加速滑动
                speed += (isReversal ? -70 : 70);
            }
            if (isSkip) {
                break;
            }
        }

        //模拟手指离开屏幕
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, pX, pY, 0);
//        view.onTouchEvent(upEvent);
        listener.onTouch(view, upEvent);

        //回收触摸事件
        downEvent.recycle();
        for (int i = 0; i < moveEvents.size(); i++) {
            moveEvents.get(i).recycle();
        }
        upEvent.recycle();
    }
}
