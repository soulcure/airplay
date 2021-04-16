package swaiotos.runtime.h5;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;

import swaiotos.runtime.h5.common.event.OnShakeEventCBData;
import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @Author: yuzhan
 */
public class H5NPAppletActivity extends BaseH5AppletActivity{
    @Override
    protected H5RunType.RunType runType() {
        return H5RunType.RunType.MOBILE_RUNTYPE_ENUM;
    }

    private SensorManager sensorManager;
    private SensorEventListener shakeListener;
    private boolean isRefresh = false;
    private ShakeHandler shakeHandler;

    @Override
    public void onBackPressed() {
        if (hasOverwriteBackAction) {
            h5core.onLeftBtnClick();
        } else if (!h5core.onBackPressed()) {
            finish();
            overridePendingTransition(0, swaiotos.runtime.base.R.anim.applet_exit);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shakeHandler = new ShakeHandler();
    }

    public void registerShakeListener(){
        if(shakeListener == null){
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            shakeListener = new ShakeSensorListener();
            sensorManager.registerListener(shakeListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onResume() {
        if(shakeListener!=null){
            sensorManager.registerListener(shakeListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // acitivity后台时取消监听
        if(shakeListener != null){
            sensorManager.unregisterListener(shakeListener);
        }
        super.onPause();
    }

    private class ShakeSensorListener implements SensorEventListener {
        private static final int ACCELERATE_VALUE = 35;

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 判断是否处于刷新状态(例如微信中的查找附近人)
            if (isRefresh) {
                return;
            }

            float[] values = event.values;

            /**
             * 一般在这三个方向的重力加速度达到20就达到了摇晃手机的状态 x : x轴方向的重力加速度，向右为正 y :
             * y轴方向的重力加速度，向前为正 z : z轴方向的重力加速度，向上为正
             */
            float x = Math.abs(values[0]);
            float y = Math.abs(values[1]);
            float z = Math.abs(values[2]);

//            LogUtil.d("x is :" + x + " y is :" + y + " z is :" + z);

            if (x >= ACCELERATE_VALUE || y >= ACCELERATE_VALUE
                    || z >= ACCELERATE_VALUE) {
//                Toast.makeText(H5NPAppletActivity.this,
//                        "accelerate speed :"
//                                + (x >= ACCELERATE_VALUE ? x
//                                : y >= ACCELERATE_VALUE ? y : z),
//                        Toast.LENGTH_SHORT).show();

                isRefresh = true;
                VibratorHelper.Vibrate(H5NPAppletActivity.this, 500);

                EventBus.getDefault().post(new OnShakeEventCBData("onShakeEvent"));

                Message msg = new Message();
                msg.what =1;
                shakeHandler.sendMessageDelayed(msg,1800);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

    }


    class ShakeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == 1){
                isRefresh = false;
            }
        }
    }

    @Override
    public int getSafeDistanceTop() {
        return (int) getResources().getDimension(R.dimen.h5_top_height);
    }
}
