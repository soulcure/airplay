package swaiotos.sensor.touch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import swaiotos.sensor.client.IConnectClient;

/**
 * @Author: yuzhan
 */
public class InputTouchView extends FrameLayout {
    //用于发送event事件
    private IConnectClient client;
    //是否需要展示两个反馈 用于区分激光笔(false)和照片投屏(true)
    private boolean needTwoFinger;
    //激光笔是否已经展示，在激光笔界面如果有一个手指按下，其余按下手指都不发送event事件给dongle，也不展示图标
    private boolean isLaserPointShow = false;
    private static final String TAG = "InputTouchView";

    private SparseArray<TouchPoint> touchDrawableBeanSparseArray;

    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] sensorValues;
    private boolean enableSensor = false;
    private float sensorThreshold = 0;

    public InputTouchView(@NonNull Context context) {
        super(context);
        setBackgroundColor(Color.DKGRAY);
        setClickable(true);
        setOnTouchListener(onTouchListener);
        touchDrawableBeanSparseArray = new SparseArray<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    }

    public void setAnimatorValue(float value, int id) {
        TouchPoint touchPoint = touchDrawableBeanSparseArray.get(id);
        if (touchPoint != null) {
            touchPoint.setValue(value);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (touchDrawableBeanSparseArray.size() > 0) {
            for (int i = 0; i < touchDrawableBeanSparseArray.size(); i++) {
                TouchPoint touchPoint = touchDrawableBeanSparseArray.get(i);
                touchPoint.draw(canvas);
            }
        }
    }

    public void setSize(int w, int h) {
        setLayoutParams(new FrameLayout.LayoutParams(w, h));
    }

    public void setClient(IConnectClient client) {
        this.client = client;
    }

    public void setSensorEnable(boolean enable) {
        enableSensor = enable;
        if (!enable) {
            stopSensor();
        }
    }

    public void setSensorThreshold(float t) {
        sensorThreshold = t;
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (client == null) return false;
            controlTouchView(event);
            if (needTwoFinger) {
                client.sendMotionEvent(event, v);
            } else if (isLaserPointShow && !needTwoFinger) {
                client.sendMotionEvent(event, v);
            } else {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    client.sendMotionEvent(event, v);
                }
            }
            return false;
        }
    };

    private void controlTouchView(MotionEvent event) {
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);
        TouchPoint touchPoint = touchDrawableBeanSparseArray.get(pointerId);
        //激光笔只添加一个点
        if (touchPoint == null && ((needTwoFinger && touchDrawableBeanSparseArray.size() < 2)
                || (!needTwoFinger && touchDrawableBeanSparseArray.size() == 0))) {
            touchPoint = new TouchPoint(getContext(), this, pointerId);
            touchDrawableBeanSparseArray.put(pointerId, touchPoint);
        }
        if (touchPoint == null) return;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                stopSensor();
                if (pointerId == 0) {
                    isLaserPointShow = true;
                }
                if(pointerId < event.getPointerCount()) {
                    touchPoint.updatePosition(event.getX(pointerId), event.getY(pointerId));
                    touchPoint.addDrawableAnim();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    pointerId = event.getPointerId(i);
                    touchPoint = touchDrawableBeanSparseArray.get(pointerId);
                    if (touchPoint != null) {
                        touchPoint.updatePosition(event.getX(i), event.getY(i));
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == 0) {
                    isLaserPointShow = false;
                }
                touchPoint.removeDrawableAnim();
                startSensor();
                break;
        }
    }

    public void setNeedTwoFinger(boolean needTwoFinger) {
        this.needTwoFinger = needTwoFinger;
    }

    public void onStart() {
        startSensor();
    }

    public void onStop() {
        stopSensor();
    }

    private void startSensor() {
        if (enableSensor) {
            try {
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception e) {

            }
        }
    }

    private void stopSensor() {
        sensorValues = null;
        try {
            sensorManager.unregisterListener(sensorEventListener);
        } catch (Exception e) {

        }
    }

    boolean needSendSensorEvent = false;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() != Sensor.TYPE_ORIENTATION) {
                return;
            }
            needSendSensorEvent = false;
            if (sensorValues == null) {
                sensorValues = new float[3];
                sensorValues[0] = event.values[0];
                sensorValues[1] = event.values[1];
                sensorValues[2] = event.values[2];
            } else {
                if (Math.abs(event.values[0] - sensorValues[0]) > sensorThreshold) {
                    sensorValues[0] = event.values[0];
                    needSendSensorEvent = true;
                } else if (Math.abs(event.values[1] - sensorValues[1]) > sensorThreshold) {
                    sensorValues[1] = event.values[1];
                    needSendSensorEvent = true;
                } else if (Math.abs(event.values[2] - sensorValues[2]) > sensorThreshold) {
                    sensorValues[2] = event.values[2];
                    needSendSensorEvent = true;
                }
                if (client != null && needSendSensorEvent) {
                    client.sendSensorEvent(event, InputTouchView.this);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
