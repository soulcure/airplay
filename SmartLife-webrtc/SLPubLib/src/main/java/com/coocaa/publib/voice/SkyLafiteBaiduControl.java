package com.coocaa.publib.voice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.coocaa.publib.data.def.IBaseMobileLafites;
import com.coocaa.publib.data.def.SkyLafiteMobileInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by WHY on 2018/3/26.
 */

public class SkyLafiteBaiduControl implements SkyAudioTask.OnRawDataSendCallback{
    private static final String TAG = SkyLafiteBaiduControl.class.getSimpleName();

    private Context mContext;
    //百度识别方案采用语音流传输给电视端
    private ExecutorService mSingleThreadExecutor;
    private SkyAudioTask mSkyAudioTask;
    private SkyLafiteBaiduControlCallback mCallback;

    private final static int MSG_START_RAWDATA = 0x1001;
    private final static int MSG_STOP_RAWDATA = 0x1002;
    private final static int MSG_SEND_RAWDATA = 0x1003;
    private final static int MSG_CANCEL_RAWDATA = 0x1004;

    public SkyLafiteBaiduControl(Context context) {
        mContext = context;
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        initHandler();
    }

    public void destroy() {
        mHandlerThread.quit();
    }

    public interface SkyLafiteBaiduControlCallback {
        void sendRecognition(String str);
        void onVolume(int volMax);
    }

    public void setSkyLafiteBaiduControlCallback(SkyLafiteBaiduControlCallback callback) {
        mCallback = callback;
    }

    public void startRun() {
        mHandler.sendEmptyMessage(MSG_START_RAWDATA);
    }

    public void stopRun() {
        mHandler.sendEmptyMessage(MSG_STOP_RAWDATA);
    }

    public void cancelRun() {
        mHandler.sendEmptyMessage(MSG_CANCEL_RAWDATA);
    }

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private void initHandler() {
        mHandlerThread = new HandlerThread("voice_handler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();

                switch (msg.what) {
                    case MSG_START_RAWDATA: {
                        if (mCallback == null) return;
                        if(mSkyAudioTask != null){
                            mSkyAudioTask.setAudioTaskIsWorking(false);
                        }

                        mSkyAudioTask = new SkyAudioTask(mContext, SkyLafiteBaiduControl.this);
                        mSkyAudioTask.setAudioTaskIsWorking(true);
                        mSingleThreadExecutor.execute(mSkyAudioTask);

                        SkyLafiteMobileInfo skyLafiteMobileInfo = new SkyLafiteMobileInfo();
                        skyLafiteMobileInfo.setType(IBaseMobileLafites.PhoneDataType.DUEROS_REQUEST_START_RAWDATA.toString());
                        mCallback.sendRecognition(new Gson().toJson(skyLafiteMobileInfo));

                        break;
                    }
                    case MSG_STOP_RAWDATA: {
                        if (mCallback == null) return;
                        if(mSkyAudioTask != null){
                            mSkyAudioTask.setAudioTaskIsWorking(false);
                        }

                        SkyLafiteMobileInfo skyLafiteMobileInfo = new SkyLafiteMobileInfo();
                        skyLafiteMobileInfo.setType(IBaseMobileLafites.PhoneDataType.DUEROS_REQUEST_STOP_RAWDATA.toString());
                        mCallback.sendRecognition(new Gson().toJson(skyLafiteMobileInfo));

                        break;
                    }
                    case MSG_SEND_RAWDATA: {
                        if (mCallback == null) return;
                        if (bundle == null)
                            break;
                        SkyLafiteMobileInfo skyLafiteMobileInfo = new SkyLafiteMobileInfo();
                        skyLafiteMobileInfo.setType(IBaseMobileLafites.PhoneDataType.DUEROS_REQUEST_RAWDATA.toString());
                        //之前的fastjson 会自动把byte[]转成base64， Base64.NO_WRAP：不插入换行符
                        String strBase64 = new String(Base64.encode(bundle.getByteArray("rawdata"), Base64.NO_WRAP));
                        skyLafiteMobileInfo.setContent(strBase64);
                        //防止Gson将对象类转成Json对象时出现\u003d 、\u0027等情况
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        String gsonString = gson.toJson(skyLafiteMobileInfo);

                        mCallback.sendRecognition(gsonString);
                        mCallback.onVolume(bundle.getInt("volMax"));
                        break;
                    }
                    case MSG_CANCEL_RAWDATA:
                        if (mCallback == null) return;
                        if (bundle == null)
                            break;
                        SkyLafiteMobileInfo skyLafiteMobileInfo = new SkyLafiteMobileInfo();
                        skyLafiteMobileInfo.setType(IBaseMobileLafites.PhoneDataType.DUEROS_REQUEST_CANCEL_RAWDATA.toString());
                        mCallback.sendRecognition(new Gson().toJson(skyLafiteMobileInfo));
                        break;
                    default:
                        break;
                }
            }
        };
    }


    @Override
    public void onRawDataSend(byte[] data, int volMax) {
        Log.d(TAG, "onRawDataSend: ");
        Message msg = mHandler.obtainMessage(MSG_SEND_RAWDATA);
        Bundle b = new Bundle();
        b.putByteArray("rawdata", data);
        b.putInt("volMax", volMax);
        msg.setData(b);
        msg.sendToTarget();
    }
}
