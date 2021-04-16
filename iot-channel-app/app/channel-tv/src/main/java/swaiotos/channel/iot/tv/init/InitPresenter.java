package swaiotos.channel.iot.tv.init;


import android.content.Context;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.FileAccessTokenUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.tv.base.BasePresenter;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class InitPresenter extends BasePresenter<InitContract.View> implements InitContract.Presenter {

    private final String TAG = InitPresenter.class.getSimpleName();

    private Context mContext;
    private ScheduledExecutorService mBindExecutorService;
    private DeviceBindStatus mDeviceBindStatus;
    private DeviceInfoUpdate mOnDeviceInfoUpdateListener;
    private String mAccessToken;

    public InitPresenter(@Nullable InitContract.View initView) {
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "mContext" + mContext);
                    IOTAdminChannel.mananger.open(mContext, mContext.getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG, "---onConntected--TV---:"+getView());
                            try {
                                setIotChannelListener();
                                if (getView() != null && getView().isActive()) {
                                    onBindCode(channel.getDeviceManager().getAccessToken());
                                    List<Device> list = IOTAdminChannel.mananger.getSSAdminChannel()
                                            .getDeviceAdminManager().updateDeviceList();
                                    getView().triggerQueryDevices(list,1);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "---onError--TV---");
                            getView().refreshTips(3);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setIotChannelListener() throws RemoteException {
        if (mDeviceBindStatus == null) {
            mDeviceBindStatus = new DeviceBindStatus();
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .addDeviceBindListener(mDeviceBindStatus);
        } else {
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .removeDeviceBindListener(mDeviceBindStatus);
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .addDeviceBindListener(mDeviceBindStatus);
        }

        if (mOnDeviceInfoUpdateListener == null) {
            mOnDeviceInfoUpdateListener = new DeviceInfoUpdate();
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .addDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
        } else {
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .removeDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                    .addDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
        }
    }

    @Override
    public void detachView() {

        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
        if (mDeviceBindStatus != null) {
            try {
                IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                        .removeDeviceBindListener(mDeviceBindStatus);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (mOnDeviceInfoUpdateListener != null) {
            try {
                IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager()
                        .removeDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        super.detachView();
    }

    private void onBindCode(String accessToken) {
        AndroidLog.androidLog("accessToken:"+accessToken);
        if (!TextUtils.isEmpty(accessToken)) {
            mAccessToken = accessToken;
        }
        AndroidLog.androidLog("---accessToken:"+ShareUtls.getInstance(mContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,""));
        if (TextUtils.isEmpty(mAccessToken)) {
            try {
                if (new File(mContext.getFilesDir(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME).exists()) {
                    //首先读取文件中的accessToken
                    mAccessToken = FileAccessTokenUtils.getDataFromFile(mContext,Constants.COOCAA_FILE_ACCESSTOKEN_NAME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //为空情况读取SharedPreferences
            if (TextUtils.isEmpty(mAccessToken)) {
                mAccessToken = ShareUtls.getInstance(mContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
            }
        }

        QRCodeUseCase.getInstance(mContext).run(new QRCodeUseCase.RequestValues(mAccessToken, TYPE.TV), new QRCodeUseCase.QRCodeCallBackListener() {
            @Override
            public void onError(String errType,String msg) {
                Log.d(TAG, "QRCodeUseCase:" + msg);
                if (getView() == null || !getView().isActive())
                    return;
                getView().refreshErrorUI();

                if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                    mBindExecutorService.shutdownNow();
                    mBindExecutorService = null;
                }
            }

            @Override
            public void onSuccess(String bindCode,String url, String expiresIn, String typeLoopTime) {

                if (getView() == null || !getView().isActive())
                    return;

                getView().reflushOrUpdateQRCode(bindCode, url,expiresIn);

                if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                    mBindExecutorService.shutdownNow();
                    mBindExecutorService = null;
                }
                mBindExecutorService = Executors.newScheduledThreadPool(1);
                mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null || !getView().isActive())
                            return;
                        onBindCode(mAccessToken);
                    }
                }, Integer.parseInt(expiresIn), Integer.parseInt(expiresIn), TimeUnit.SECONDS);
            }
        });
    }

    class DeviceBindStatus implements DeviceAdminManager.OnDeviceBindListener {
        @Override
        public void onDeviceBind(String lsid) {
            Log.d(TAG,"-----onDeviceBind---");
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    List<Device> list = IOTAdminChannel.mananger.getSSAdminChannel().getDeviceManager().updateDeviceList();
                    if (getView() != null && getView().isActive()) {
                        getView().triggerQueryDevices(list,2);
                    }

                }
            });

        }

        @Override
        public void onDeviceUnBind(String lsid) {
            Log.d(TAG,"-----onDeviceUnBind---");
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"-----onDeviceUnBind2---");
                    List<Device> list = IOTAdminChannel.mananger.getSSAdminChannel().getDeviceManager().updateDeviceList();
                    if (getView() != null && getView().isActive()) {
                        getView().triggerQueryDevices(list,2);
                    }

                }
            });
        }
    }

    class DeviceInfoUpdate implements DeviceAdminManager.OnDeviceInfoUpdateListener {

        @Override
        public void onDeviceInfoUpdate(List<Device> devices) {
            if (getView() != null && getView().isActive()) {
                getView().triggerQueryDevices(devices,2);
            }
        }

        @Override
        public void sseLoginSuccess() {

        }

        @Override
        public void loginState(int code, String info) {

        }

        @Override
        public void loginConnectingState(int code, String info) {

        }

    }

}
