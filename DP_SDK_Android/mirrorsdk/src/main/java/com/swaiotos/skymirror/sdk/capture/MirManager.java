package com.swaiotos.skymirror.sdk.capture;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.Surface;

import com.swaiotos.skymirror.sdk.constant.Constants;
import com.swaiotos.skymirror.sdk.reverse.IDrawListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerInitListener;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;
import com.swaiotos.skymirror.sdk.reverse.PlayerDecoder;
import com.swaiotos.skymirror.sdk.reverse.ReverseCaptureService;


/**
 * @ClassName: DeviceControllerManager
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/3/18 13:42
 */
public class MirManager {


    private enum BIND_STATUS {
        IDLE, BINDING, BINDED
    }

    /**
     * SDK初始化结果监听器
     */
    public interface InitListener {
        void success();

        void fail();
    }

    @SuppressLint("StaticFieldLeak")
    private static MirManager instance;

    private MirManager() {
        isMirRunning = false;
        isReverseRunning = false;
    }


    private ReverseCaptureService.ReverseServiceBinder reverseService;

    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private InitListener mInitListener;

    private Context mContext;


    private IPlayerListener mirServiceListener;


    private boolean isMirRunning;
    private boolean isReverseRunning;

    /**
     * bind service callback
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof ReverseCaptureService.ReverseServiceBinder) {
                reverseService = (ReverseCaptureService.ReverseServiceBinder) service;
                bind = BIND_STATUS.BINDED;
                if (mInitListener != null) {
                    mInitListener.success();
                }
            }
        }

        // 连接服务失败后，该方法被调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            reverseService = null;
            bind = BIND_STATUS.IDLE;
            if (mInitListener != null) {
                mInitListener.fail();
            }
        }
    };


    public static MirManager instance() {
        if (instance == null) {
            instance = new MirManager();
        }
        return instance;
    }


    public void init(Context context) {
        init(context, null);
    }

    public void init(Context context, InitListener listener) {
        mContext = context;
        mInitListener = listener;

        if (bind == BIND_STATUS.BINDED) {
            if (mInitListener != null) {
                mInitListener.success();
            }
        } else if (bind == BIND_STATUS.BINDING) {
            //do nothing
        } else {
            Intent intent = new Intent(context.getApplicationContext(), ReverseCaptureService.class);
            context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }


    public boolean isMirRunning() {
        return isMirRunning;
    }

    public void setMirRunning(boolean mirRunning) {
        isMirRunning = mirRunning;
    }

    public boolean isReverseRunning() {
        return isReverseRunning;
    }

    public void setReverseRunning(boolean reverseRunning) {
        isReverseRunning = reverseRunning;
    }

    public void setDrawListener(IDrawListener listener) {
        if (reverseService != null) {
            PlayerDecoder decoder = reverseService.getPlayerDecoder();
            if (decoder != null) {
                decoder.setDrawListener(listener);
            }
        }
    }


    public void setPlayerInitListener(IPlayerInitListener listener) {
        if (reverseService != null) {
            reverseService.setInitListener(listener);
        }
    }


    /**
     * 发送触摸指令
     *
     * @param motionEvent
     */
    public void sendMotionEvent(MotionEvent motionEvent) {
        if (reverseService != null) {
            PlayerDecoder decoder = reverseService.getPlayerDecoder();
            if (decoder != null) {
                decoder.sendMotionEvent(motionEvent);
            }
        }
    }


    /**
     * 开始接收镜像（for 智屏app）
     *
     * @param surface
     */
    public void startReverseScreen(Surface surface, IPlayerListener playerListener,
                                   IDrawListener drawListener) {
        if (reverseService != null) {
            reverseService.startReverse(surface, playerListener, drawListener);
        }
    }


    public void stopReverseScreen() {
        if (reverseService != null) {
            reverseService.stopReverse();
        }
    }


    public void destroy() {
        if (mContext != null && bind == BIND_STATUS.BINDED) {
            bind = BIND_STATUS.IDLE;
            mContext.getApplicationContext().unbindService(serviceConnection);
        }

    }


    /**
     * 开始录屏服务
     *
     * @param context
     * @param ip
     */
    public void startScreenCapture(Context context, String ip) {
        stopAll(context);

        Intent startServerIntent = new Intent(context, MirClientService.class);
        startServerIntent.setAction("START");
        startServerIntent.putExtra(Constants.SERVER_IP, ip);

        context.startService(startServerIntent);
    }


    public void startScreenCapture(Context context, String ip, int resultCode, Intent data) {
        stopAll(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent startServerIntent = new Intent(context, MirClientService.class);
            startServerIntent.setAction("START");
            startServerIntent.putExtra("resultCode", resultCode);
            startServerIntent.putExtra("intent", data);
            startServerIntent.putExtra("serverip", ip);
            context.startService(startServerIntent);
        }

    }

    /**
     * 开始录屏服务(for demo)
     *
     * @param context
     * @param ip
     * @param width
     * @param height
     * @param resultCode
     * @param data
     */
    public void startScreenCapture(Context context, String ip, int width, int height, int resultCode, Intent data) {
        stopAll(context);

        Intent startServerIntent = new Intent(context, MirClientService.class);
        startServerIntent.setAction("START");
        startServerIntent.putExtra("resultCode", resultCode);
        startServerIntent.putExtra("intent", data);
        startServerIntent.putExtra("width", width);
        startServerIntent.putExtra("height", height);
        startServerIntent.putExtra("serverip", ip);
        context.startService(startServerIntent);
    }


    /**
     * 停止录屏服务
     *
     * @param context
     */
    public void stopScreenCapture(Context context) {
        Intent stopServerIntent = new Intent(context, MirClientService.class);
        stopServerIntent.setAction("STOP");
        context.startService(stopServerIntent);
    }


    public IPlayerListener getMirServiceListener() {
        return mirServiceListener;
    }

    public void setMirServiceListener(IPlayerListener mirServiceListener) {
        this.mirServiceListener = mirServiceListener;
    }

    public void stopAll(Context context) {
        context.stopService(new Intent(context, ReverseCaptureService.class));
        context.stopService(new Intent(context, MirClientService.class));
    }


}
