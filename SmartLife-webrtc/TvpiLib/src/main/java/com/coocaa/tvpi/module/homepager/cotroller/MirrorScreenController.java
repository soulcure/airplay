package com.coocaa.tvpi.module.homepager.cotroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.MirrorScreenParams;
import com.coocaa.smartscreen.data.channel.events.MirrorScreenEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartscreen.utils.NetworkUtils;
import com.coocaa.tvpi.broadcast.NetWorkStateReceiver;
import com.coocaa.tvpi.event.NetworkEvent;
import com.coocaa.tvpilib.R;
import com.swaiot.webrtcc.config.Constant;
import com.swaiot.webrtcc.entity.Model;
import com.swaiot.webrtcc.entity.SSEEvent;
import com.swaiot.webrtcc.video.WebRTCVideoManager;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.swaiotos.skymirror.sdk.reverse.IPlayerListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import swaiotos.channel.iot.ss.SSChannel;


/**
 * 屏幕镜像
 * 初始化 {@link #MirrorScreenController(Fragment, MirrorScreenListener)}
 * 注销 {@link #destroy()}
 * 需要将Fragment或者Activity的onActivityResult转发到这个类{@link #onActivityResult(int, int, Intent)} ()}
 * 开始结束镜像开关{@link #switchMirrorScreen()}
 * 镜像状态回调{@link MirrorScreenListener}
 * Created by songxing on 2020/3/25
 */
public class MirrorScreenController {
    private static final String TAG = MirrorScreenController.class.getSimpleName();
    public static final int MIRROR_SCREEN_REQUEST_CODE = 1;
    private static final int STATE_NORMAL = 0;  //未镜像
    private static final int STATE_CONNECTING = 1;  //镜像连接中
    private static final int STATE_MIRRORING = 2;   //正在镜像

    @IntDef({STATE_NORMAL, STATE_CONNECTING, STATE_MIRRORING})
    @Retention(RetentionPolicy.SOURCE)
    private @interface MirrorState {
    }

    private @MirrorState static int mirrorState = STATE_NORMAL;

    private Fragment hostFragment;
    private Intent intent;
    private int resultCode;
    private SDialog dialog;

    private NetWorkStateReceiver netWorkStateReceiver;
    private MirrorScreenListener mirrorScreenListener;

    private Handler mHandler;
    private Context mContext;
    private final WebRTCVideoManager.SenderImpl sender = content -> SSConnectManager.getInstance().sendWebRTCMessage(content);


    private final WebRTCVideoManager.WebRtcResult result = (i, s) -> {
        if (i == 0) {  //成功
            mirrorState = STATE_MIRRORING;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mirrorScreenListener != null) {
                        Log.d(TAG, "onEvent: onMirroringScreen");
                        mirrorScreenListener.onMirroringScreen();
                    }
                }
            });


        } else if (i == 1) { //正在连接
            mirrorState = STATE_CONNECTING;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mirrorScreenListener != null) {
                        Log.d(TAG, "onEvent: onMirroringScreen");
                        mirrorScreenListener.onMirroringScreen();
                    }
                }
            });
        } else {  //失败
            mirrorState = STATE_NORMAL;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mirrorScreenListener != null) {
                        Log.d(TAG, "onEvent: onMirroringScreen stop");
                        mirrorScreenListener.onStopMirrorScreen();
                        stopWebRtc();
                    }
                }
            });
        }
    };


    public MirrorScreenController(Fragment hostFragment, MirrorScreenListener mirrorScreenListener) {
        this.hostFragment = hostFragment;
        this.mirrorScreenListener = mirrorScreenListener;
        mHandler = new Handler(Looper.getMainLooper());

        MirManager.instance().setMirServiceListener(iPlayerListener);
        if (hostFragment != null && hostFragment.getContext() != null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            hostFragment.getContext().registerReceiver(netWorkStateReceiver, filter);

            mContext = hostFragment.getContext();
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    private void startWebRtc(Context context, final Intent data) {
        WebRTCVideoManager.instance().init(context, new WebRTCVideoManager.InitListener() {
            @Override
            public void success() {
                Log.d(TAG, "success...");
                WebRTCVideoManager.instance().setSender(sender);
                WebRTCVideoManager.instance().setResult(result);
                WebRTCVideoManager.instance().start(data);
            }

            @Override
            public void fail() {
                Log.d(TAG, "fail...");
            }
        });
    }

    private void stopWebRtc() {
        WebRTCVideoManager.instance().stop();
        WebRTCVideoManager.instance().destroy();
    }

    private void postData(String content) {
        if (content.contains(Constant.ANSWER)) {
            Log.i(TAG, "Received Answer");

            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.ANSWER);
            //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
            EventBus.getDefault().postSticky(event);

        } else if (content.contains(Constant.CANDIDATE)) {
            Log.i(TAG, "Received candidate");
            SSEEvent event = new SSEEvent();
            Model model = new Model(content);
            event.setModel(model);
            event.setMsgType(Constant.CANDIDATE);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "EventBus post candidate");
                    EventBus.getDefault().post(event);
                }
            }, 1000);

        }

    }

    public void resume() {
        if (hostFragment != null && hostFragment.getContext() != null) {
            if (MirManager.instance().isMirRunning()
                    || WebRTCVideoManager.instance().isStart()) {
                if (mirrorScreenListener != null) {
                    Log.d(TAG, "resume: onMirroringScreen");
                    mirrorScreenListener.onMirroringScreen();
                }
            }
        }
    }

    public void destroy() {
        if (hostFragment != null && hostFragment.getContext() != null) {
            hostFragment.getContext().unregisterReceiver(netWorkStateReceiver);
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        MirManager.instance().setMirServiceListener(null);
        /*if (MirManager.instance().isMirRunning()) {
            stopLocalMirrorScreen();
            stopRemoteMirrorScreen();
        }*/

        if (WebRTCVideoManager.instance().isStart()) {
            stopLocalMirrorScreen();
        }

        netWorkStateReceiver = null;
        mirrorScreenListener = null;
    }

    //屏幕镜像
    public void switchMirrorScreen() {
        Log.d("SmartLab", "switchMirrorScreen, curState=" + mirrorState + ", normal=0, connecting=1, mirroring=2");
        if (mirrorState == STATE_NORMAL) {
            if (hostFragment == null || hostFragment.getContext() == null) return;
            Log.d(TAG, "switchMirrorScreen: 开始镜像...");
            if (dialog == null) {
                dialog = new SDialog(hostFragment.getContext(), "屏幕镜像开始后，本机画面将实时同步到【电视】上", R.string.cancel, R.string.start_at_once,
                        new SDialog.SDialog2Listener() {
                            @Override
                            public void onClick(boolean left, View view) {
                                if (!left) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        startMirrorScreenAuthoring();
                                    } else {
                                        ToastUtils.getInstance().showGlobalShort("版本过低，不支持镜像");
                                    }
                                }
                            }
                        });
            }
            if (hostFragment.getActivity() != null && !dialog.isShowing()) {
                dialog.show();
            }
        } else if (mirrorState == STATE_CONNECTING) {
            Log.d(TAG, "switchMirrorScreen: 已在镜像连接中...");
            ToastUtils.getInstance().showGlobalShort("镜像连接中...");
        } else if (mirrorState == STATE_MIRRORING) {
            Log.d(TAG, "switchMirrorScreen: 镜像中停止镜像");
            stopLocalMirrorScreen();
            //stopRemoteMirrorScreen();

            mirrorState = STATE_NORMAL;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "switchMirrorScreen: onStopMirrorScreen");
                mirrorScreenListener.onStopMirrorScreen();
            }
        } else {
            Log.d(TAG, "switchMirrorScreen: " + mirrorState);
        }
    }

    //1.开始获取镜像授权
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startMirrorScreenAuthoring() {
        Log.d(TAG, "startMirrorScreenAuthoring");
        if (hostFragment == null || hostFragment.getContext() == null) return;
        //这里必须用Fragment启动，不然回调只会在Activity中，不会在Fragment中
        mirrorState = STATE_CONNECTING;
        MediaProjectionManager projectionManager = (MediaProjectionManager) hostFragment.getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        hostFragment.startActivityForResult(projectionManager.createScreenCaptureIntent(), MIRROR_SCREEN_REQUEST_CODE);
        if (mirrorScreenListener != null) {
            Log.d(TAG, "startMirrorScreenAuthoring: onStartMirrorScreen");
            mirrorScreenListener.onStartMirrorScreen();
        }
    }

    //2.获取屏幕镜像授权通知电视开启接受镜像服务
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != MIRROR_SCREEN_REQUEST_CODE) {
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: 镜像权限被拒绝");
            ToastUtils.getInstance().showGlobalShort("镜像权限被拒绝");
            mirrorState = STATE_NORMAL;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "onActivityResult: onStopMirrorScreen");
                mirrorScreenListener.onStopMirrorScreen();
            }
        } else {
            Log.d(TAG, "onActivityResult: 获取到镜像授权镜像准备中...");
            this.intent = data;
            this.resultCode = resultCode;
            //ToastUtils.getInstance().showGlobalShort("镜像准备中...");
            //注册前先去掉之前的
            MirrorScreenTimeoutObserver.getInstance().observeTimeout(
                    mirrorScreenTimeoutObserver, false);
            MirrorScreenTimeoutObserver.getInstance().observeTimeout(
                    mirrorScreenTimeoutObserver, true);
            //startRemoteMirrorScreen();
            startWebRtc(mContext, data);
        }
    }


    //3.收到电视机端准备好接收镜像服务的消息后手机开启发送镜像服务
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MirrorScreenEvent mirrorScreenEvent) {
        String content = mirrorScreenEvent.content;
        if (!TextUtils.isEmpty(content)) {
            postData(content);
        } else {
            Log.d(TAG, "onEvent: 电视机已准备接收镜像数据" + mirrorScreenEvent.result);

            String ip = getIpFromContent(mirrorScreenEvent.result);

            MirrorScreenTimeoutObserver.getInstance().observeTimeout(
                    mirrorScreenTimeoutObserver, false);
            startLocalMirrorScreen(resultCode, intent, ip);
            mirrorState = STATE_MIRRORING;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "onEvent: onMirroringScreen");
                mirrorScreenListener.onMirroringScreen();
            }
        }
    }


    private String getIpFromContent(String content) {
        String ip = null;
        try {
            JSONObject jsonObject = new JSONObject(content);
            String param = jsonObject.optString("param");
            if (!TextUtils.isEmpty(param)) {
                JSONObject jo = new JSONObject(param);
                ip = jo.optString("ip");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ip;
    }


    private void startLocalMirrorScreen(int resultCode, Intent data, String ip) {
        Log.d(TAG, "startLocalMirrorScreen");
        if (hostFragment == null || hostFragment.getContext() == null) return;
        if (TextUtils.isEmpty(ip)) {
            ip = SSConnectManager.getInstance().getTarget().getExtra(SSChannel.STREAM_LOCAL);
        }
        MirManager.instance().startScreenCapture(hostFragment.getContext(), ip, resultCode, data);
    }

    private void stopLocalMirrorScreen() {
        Log.d(TAG, "stopLocalMirrorScreen");
        if (hostFragment == null || hostFragment.getContext() == null) return;
        //MirManager.instance().stopScreenCapture(hostFragment.getContext());
        stopWebRtc();
    }

    private void startRemoteMirrorScreen() {
        Log.d(TAG, "startRemoteMirrorScreen");
        CmdUtil.sendMirrorScreenCmd(MirrorScreenParams.CMD.START_MIRROR.toString());
    }

    private void stopRemoteMirrorScreen() {
        Log.d(TAG, "stopRemoteMirrorScreen");
        CmdUtil.sendMirrorScreenCmd(MirrorScreenParams.CMD.STOP_MIRROR.toString());
    }

    private IPlayerListener iPlayerListener = new IPlayerListener() {
        @Override
        public void onError(int i, String s) {
            Log.d(TAG, "onError: 镜像发生异常:" + i + s);
//            stopLocalMirrorScreen();
            if (i == 9) {
                ToastUtils.getInstance().showGlobalShort("对方已经退出屏幕镜像");
            }
            mirrorState = STATE_NORMAL;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "onError: iPlayerListener onStopMirrorScreen");
                mirrorScreenListener.onStopMirrorScreen();
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NetworkEvent event) {
        Log.d(TAG, "onEvent: 收到网络变化");
        if (hostFragment == null || hostFragment.getContext() == null) return;
        if (!NetworkUtils.isAvailable(hostFragment.getContext())) {
            ToastUtils.getInstance().showGlobalShort("网络不可用");
            Log.d(TAG, "网络不可用 onEvent: onStopMirrorScreen");
            mirrorState = STATE_NORMAL;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "onEvent: onStopMirrorScreen");
                mirrorScreenListener.onStopMirrorScreen();
            }
        }
    }


    private Observer<Integer> mirrorScreenTimeoutObserver = new Observer<Integer>() {
        @Override
        public void onChanged(Integer integer) {
            /*Log.d(TAG, "镜像超时");
            ToastUtils.getInstance().showGlobalShort("镜像启动超时");
            mirrorState = STATE_NORMAL;
            if (mirrorScreenListener != null) {
                Log.d(TAG, "onEvent: onStopMirrorScreen");
                mirrorScreenListener.onStopMirrorScreen();
            }*/
        }

//        @Override
//        public void onEvent(Integer integer) {
//
//        }
    };

    /**
     * 镜像状态回调，开始，镜像中，镜像结束
     */
    public interface MirrorScreenListener {
        void onStartMirrorScreen();

        void onMirroringScreen();

        void onStopMirrorScreen();
    }
}
