package swaiotos.channel.iot.tv.init;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.lsid.ILSIDManagerService;
import swaiotos.channel.iot.common.usecase.BindCallBackUseCase;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.device.DeviceAdminManagerImpl;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.tv.base.BasePresenter;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class SmallPresenter extends BasePresenter<SmallContract.View> implements SmallContract.Presenter {

    private final String TAG = SmallPresenter.class.getSimpleName();

    private Context mContext;
    private ScheduledExecutorService mBindExecutorService;
    private String mBindCode = "", mOldBindCode = "";

    public SmallPresenter(@Nullable SmallContract.View initView) {
        attachView(initView);
        initView.setPresenter(this);
    }


    @Override
    public void init(Context context) {
        mContext = context;
        initStartTVSSChannelService();
    }

    /**
     * 启动service channel服务
     */
    private void initStartTVSSChannelService() {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "mContext" + mContext);
                    IOTAdminChannel.mananger.open(mContext, mContext.getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG, "---onConntected--TV---");
                            if (getView() != null && getView().isActive()) {
                                try {
                                    onBindCode();
                                    getView().triggerQuerDevices();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "---onError--TV---");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void detachView() {
        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
        super.detachView();
    }

    private void onBindCode() {
        String accessTokenPref = ShareUtls.getInstance(mContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN, "");
        QRCodeUseCase.getInstance(mContext).run(new QRCodeUseCase.RequestValues(accessTokenPref, TYPE.TV), new QRCodeUseCase.QRCodeCallBackListener() {
            @Override
            public void onError(String errType,String msg) {
                Log.d(TAG, "QRCodeUseCase:" + msg);
            }

            @Override
            public void onSuccess(String bindCode,String url, String expiresIn, String typeLoopTime) {
                if (getView() == null || !getView().isActive())
                    return;

                getView().refushOrUpdateQRCode(bindCode, url,expiresIn);

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
                        getView().refreshTips();
                        onBindCode();
                    }
                }
            }
        });
    }
}
