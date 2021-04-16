package swaiotos.channel.iot.ss;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedHashMap;
import java.util.Map;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.channel.im.IIMChannelService;
import swaiotos.channel.iot.ss.channel.im.IMChannel;
import swaiotos.channel.iot.ss.channel.im.IMChannelServer;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannelService;
import swaiotos.channel.iot.ss.device.IDeviceManagerService;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.ISessionManagerService;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.channel.iot.utils.ipc.ParcelableBinder;
import swaiotos.channel.iot.webrtc.entity.FileProgress;

/**
 * @ClassName: SSChannelServiceImpl
 * @Author: lu
 * @CreateDate: 2020/4/2 10:42 AM
 * @Description:
 */
public class SSChannelServiceImpl implements IMChannelServer.Receiver {
    private static final String TAG = "SSSrv";

    private static class SSChannelServiceStub extends ISSChannelService.Stub {
        private SSContext mSSContext;
        private ControllerService mController;
        private SessionManagerService mSessionManager;
        private IMChannelService mIMChannel;
        private DeviceManagerService mDeviceManager;
        private DeviceAdminManagerService mDeviceAdminManager;
        private Map<String, IBinder> mBinders = new LinkedHashMap<>();

        public SSChannelServiceStub(Context context, SSContext ssContext) {
            super();
            mSSContext = ssContext;
            mSessionManager = new SessionManagerService(mSSContext);
            mIMChannel = new IMChannelService(mSSContext);
            mDeviceManager = new DeviceManagerService(mSSContext);


            mController = new ControllerService(mSSContext);
            mBinders.put(IOTAdminChannel.SERVICE_CONTROLLER, mController);

            mDeviceAdminManager = new DeviceAdminManagerService(mSSContext);
            mBinders.put(IOTAdminChannel.SERVICE_DEVICEADMIN, mDeviceAdminManager);
        }

        @Override
        public ISessionManagerService getSessionManager() throws RemoteException {
            return mSessionManager;
        }

        @Override
        public IIMChannelService getIMChannel() throws RemoteException {
            return mIMChannel;
        }

        @Override
        public IStreamChannelService getStreamChannel() throws RemoteException {
            return null;
        }

        @Override
        public IDeviceManagerService getDeviceManager() throws RemoteException {
            return mDeviceManager;
        }

        @Override
        public IBinder getBinder(String name) throws RemoteException {
            return mBinders.get(name);
        }
    }


    private static class MainStub extends IMainService.Stub {
        private ISSChannelService.Stub mChannelService;
        private Context mContext;
        private String mError;
        private SSChannelService.SSChannelServiceManager mManager;

        public MainStub(Context context, SSChannelService.SSChannelServiceManager manager) {
            this.mContext = context;
            this.mManager = manager;
            Log.d(TAG, "MainStub created");
        }

        synchronized void setSSChannelService(ISSChannelService.Stub channelService) {
            this.mChannelService = channelService;
            notifyAll();
        }

        synchronized void setError(String error) {
            mError = error;
            notifyAll();
        }

        @Override
        public synchronized ParcelableBinder open(String packageName) throws RemoteException {
            Log.d(TAG, "------open----packageName:" + packageName);

            if (mManager != null && mManager.getLSIDManager() != null) {
                AndroidLog.androidLog("-----mManager.getLSIDManager()-refreshLSIDInfo---");
                mManager.getLSIDManager().refreshLSIDInfo();
            }

            if (this.mChannelService == null) {

                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ParcelableBinder binder;
            try {
                if (mChannelService != null) {
//                String clientID = mService.verify(packageName);
                    binder = new ParcelableBinder(0, "open successfully!", mChannelService);
                } else {
                    binder = new ParcelableBinder(2, TextUtils.isEmpty(mError) ? "open error" : mError);
                }
            } catch (VerifyError verifyError) {
                verifyError.printStackTrace();
                binder = new ParcelableBinder(1, verifyError.getMessage());
            }
            return binder;
        }
    }

    private SmartScreen mSmartScreen;
    private MainStub mMainStub;
    private Context mContext;
    private SSChannelService.SSChannelServiceManager mManager;

    SSChannelServiceImpl(Context context) {
        this.mContext = context;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    void start(final SSChannelService.SSChannelServiceManager manager) {
        if (mMainStub != null) {
            return;
        }

        String start = "Service Binder Start";
        Log.d("logfile", start);
//        LogFile.inStance().toFile(start);

        synchronized (SSChannelServiceImpl.this) {
            mMainStub = new MainStub(mContext, manager);
            SSChannelServiceImpl.this.notifyAll();
        }
        mManager = manager;
        mSmartScreen = new SmartScreenImpl(mContext, mManager);
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                while (!NetUtils.isConnected(mContext)) {
                    Log.d(TAG, "waiting for network connected!!");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "SSChannelServiceManager created:" + Constants.getVersionCode(mContext));
                Log.d(TAG, "SmartScreen created");
                mSmartScreen.open(mContext, new SmartScreen.OpenHandler() {
                    @Override
                    public void onOpened(SmartScreen ss) {
                        Log.d(TAG, "SmartScreen onOpened");
                        SSChannelServiceStub ssChannelServiceStub = new SSChannelServiceStub(mContext, ss);

                        mSmartScreen.getIMChannel().setReceiver(SSChannelServiceImpl.this);
                        mSmartScreen.getClientManager().init(ssChannelServiceStub, mManager.getClientServiceIntent(mContext));

                        mMainStub.setSSChannelService(ssChannelServiceStub);
                        mManager.onSSChannelServiceStarted(mContext);

                        String end = "Service Binder End";
                        Log.d("logfile", end);
//                        LogFile.inStance().toFile(end);
                    }

                    @Override
                    public void onFailed(String error) {
                        mMainStub.setError(error);
                    }
                });
            }
        });
    }


    void close() {
        mSmartScreen.close();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public IBinder getMainStub() {
        synchronized (SSChannelServiceImpl.this) {
            if (mMainStub == null) {
                try {
                    SSChannelServiceImpl.this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return mMainStub;
        }
    }

    @Override
    public void onReceive(IMChannel channel, IMMessage message) {
        String c = message.getClientTarget();
        if (TextUtils.isEmpty(c)) {
            return;
        }
        mSmartScreen.getClientManager().start(c, message);
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void sendFileProgress(FileProgress progress) {
        IMMessage message = progress.imMessage;
        String c = message.getClientTarget();
        if (TextUtils.isEmpty(c)) {
            return;
        }
        mSmartScreen.getClientManager().start(c, message);
    }
}
