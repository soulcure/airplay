package com.swaiotos.skymirror.sdk.reverse;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.Surface;


/**
 * 设备发现功能替换为云端push ,将ip、port回传，用于连接httpserver和socket
 * 反向投屏功能：
 * 此为server端，接收client端发送过来的屏幕数据、解码、播放
 * 开启反向投屏功能后service需要保证为存活状态去初始化 httpServer 等待着 httpclient 连接
 * 优化空间：
 * 1.目前开始的时候会开启多个 httpserver, 后期可以换为1个固定server通道实现连接操作
 * PS：
 */
public class ReverseCaptureService extends Service {

    private PlayerDecoder decoder;
    private IPlayerInitListener initListener;

    /**
     * activity和service通信接口
     */
    public class ReverseServiceBinder extends Binder {


        public PlayerDecoder getPlayerDecoder() {
            return decoder;
        }

        public void setInitListener(IPlayerInitListener listener) {
            initListener = listener;
        }

        public void startReverse(Surface surface, IPlayerListener playerListener,
                                 IDrawListener drawListener) {
            decoder.setSurface(surface);
            decoder.setPlayerListener(playerListener);
            decoder.setDrawListener(drawListener);

            if (initListener != null) {
                initListener.onInitStatus(true);
            }
        }

        public void stopReverse() {
            if (decoder != null) {
                decoder.mirServerStop(IPlayerListener.ERR_CODE_SERVICE_DESTROY,
                        IPlayerListener.ERR_MSG_SERVICE_DESTROY, false);
                decoder = null;
            }
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return new ReverseServiceBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        decoder = new PlayerDecoder(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (decoder != null) {
            decoder.mirServerStop(IPlayerListener.ERR_CODE_SERVICE_DESTROY,
                    IPlayerListener.ERR_MSG_SERVICE_DESTROY, false);
            decoder = null;
        }
    }


}
