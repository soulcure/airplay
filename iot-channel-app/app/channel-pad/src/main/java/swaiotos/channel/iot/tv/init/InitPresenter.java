package swaiotos.channel.iot.tv.init;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.usecase.BindCallBackUseCase;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.tv.base.BasePresenter;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class InitPresenter extends BasePresenter<InitContract.View> implements InitContract.Presenter {

    private final String TAG = InitPresenter.class.getSimpleName();

    private BindFlowBroadcastReceiver mBindFlowBroadcastReceiver;
    private Context mContext;
    private ScheduledExecutorService mBindExecutorService;
    private String mBindCode = "", mOldBindCode = "";

    public InitPresenter(@Nullable InitContract.View initView) {
        attachView(initView);
        initView.setPresenter(this);
    }


    @Override
    public void init(Context context) {
        mContext = context;

        initRegisterBroadCast();
        initStartTVSSChannelService();
    }

    /**
     * 启动service channel服务
     */
    private void initStartTVSSChannelService() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "mContext" + mContext);
                    IOTAdminChannel.mananger.open(mContext, mContext.getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG, "---onConntected--TV---");
                            if (getView() != null && getView().isActive()) {
                                onBindCode();
                                try {
                                    getView().refreshTips("channel open success,then Other functions can be manipulated!", true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "---onError--TV---");
                            getView().refreshTips("channel open error:" + s, false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void initRegisterBroadCast() {

        if (getView().isActive() && mBindFlowBroadcastReceiver == null) {
            //fragment 活跃的时候注册二维码
            IntentFilter intentFilter = new IntentFilter(Constants.COOCAA_QRCODE_ACTION);
//            intentFilter.addAction(Constants.COOCAA_PUSH_ACTION);
//            intentFilter.addAction("waiotos.channel.iot.tv.qrcodexxx");
            mBindFlowBroadcastReceiver = new BindFlowBroadcastReceiver();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mBindFlowBroadcastReceiver, intentFilter);
        }

    }

    @Override
    public void detachView() {
        if (mBindFlowBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mBindFlowBroadcastReceiver);
        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
        super.detachView();
    }

    private SessionManager.OnSessionUpdateListener mOnSessionUpdateListener = new SessionManager.OnSessionUpdateListener() {

        @Override
        public void onSessionConnect(Session session) {
            Log.d(TAG, "session:" + session.getId());
        }

        @Override
        public void onSessionUpdate(final Session session) {
            Log.d(TAG, "onSessionUpdate " + session);

        }

        @Override
        public void onSessionDisconnect(final Session session) {
            Log.d(TAG, "onSessionDisconnect " + session);
        }
    };

    class BindFlowBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(InitPresenter.class.getName(), "--------------intent:" + intent.getAction());

            if (intent.getAction().equals(Constants.COOCAA_QRCODE_ACTION)) {
                if (getView() != null && getView().isActive()) {
//                    onBindCode();
                }
            } else if (intent.getAction().equals(Constants.COOCAA_PUSH_ACTION)) {
                //第九步：处理push消息
                String pushMsg = intent.getStringExtra(Constants.COOCAA_PUSH_MSG);
                if (getView() != null && getView().isActive()) {
                    getView().showBindView(pushMsg);
                } else if (intent.getAction().equals("waiotos.channel.iot.tv.qrcodexxx")) {
                    if (getView() != null && getView().isActive()) {
                        getView().hideBindDialog();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //发送消息
                                String mLsid = intent.getStringExtra(Constants.COOCAA_PREF_LSID);
//                                IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().addOnSessionUpdateListener(mOnSessionUpdateListener);
                                IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().addOnMySessionUpdateListener(new SessionManager.OnMySessionUpdateListener() {
                                    @Override
                                    public void onMySessionUpdate(Session mySession) {
                                        Log.d(TAG, "------onMySessionUpdate------");
                                    }
                                });
//                                IOTAdminChannel.mananger.getSSAdminChannel().getController().comfirm(mLsid);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
            }
        }
    }

    private void onBindCode() {
        String accessTokenPref = ShareUtls.getInstance(mContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
        QRCodeUseCase.getInstance(mContext).run(new QRCodeUseCase.RequestValues(accessTokenPref, TYPE.TV), new QRCodeUseCase.QRCodeCallBackListener() {

            @Override
            public void onError(String errType, String msg) {

            }

            @Override
            public void onSuccess(String bindCode,String url, String expiresIn, String typeLoopTime) {
                if (getView() == null || !getView().isActive())
                    return;

                getView().refushOrUpdateQRCode(bindCode, expiresIn);

                mOldBindCode = mBindCode;
                mBindCode = bindCode;
                Log.d(TAG, "-----------QRCodeUseCase----------");

                if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                    mBindExecutorService.shutdownNow();
                    mBindExecutorService = null;
                }
                mBindExecutorService = Executors.newScheduledThreadPool(10);
                mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null || !getView().isActive())
                            return;
                        onBindCode();
                    }
                }, Integer.parseInt(expiresIn), Integer.parseInt(expiresIn), TimeUnit.SECONDS);

                mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "------onBindPoll-----:" + System.currentTimeMillis() / 1000);
                        if (getView() == null || !getView().isActive())
                            return;
                        onBindPoll();
                    }
                }, 0, Integer.parseInt(typeLoopTime), TimeUnit.SECONDS);
            }
        });
    }

    private void onBindPoll() {

        BindCallBackUseCase.getInstance(mContext).run(new BindCallBackUseCase.RequestValues(mBindCode, mOldBindCode), new BindCallBackUseCase.BindCallBackListener() {
            @Override
            public void onError(String msg, String errorType) {
                Log.d(TAG, "onBindPoll onError:" + msg + " mBindCode:" + mBindCode + " mOldBindCode:" + mOldBindCode);
                if (!TextUtils.isEmpty(errorType) && errorType.equals(Constants.COOCAA_TYPE_20003)) {
                    onBindCode();
                }
            }

            @Override
            public void onSuccess(String bindCodeType) {
                Log.d(TAG, "onBindPoll onSuccess:" + bindCodeType);
                if (bindCodeType.equals(Constants.COOCAA_POLL_SUCCESS)) {
                    if (getView() != null && getView().isActive()) {
                        getView().refreshTips("绑定成功", false);
                        mBindExecutorService.shutdownNow();
                        mBindExecutorService = null;
                    }
                }
            }
        });
    }

}
