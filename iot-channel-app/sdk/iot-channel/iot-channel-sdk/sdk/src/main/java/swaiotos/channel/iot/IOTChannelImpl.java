package swaiotos.channel.iot;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.ss.IMainService;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelImpl;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: IOTChannelImpl
 * @Author: lu
 * @CreateDate: 2020/4/14 11:23 AM
 * @Description:
 */
public class IOTChannelImpl implements IOTChannel {
    private static final String TAG = "AIDL";


    private enum BIND_STATUS {
        IDLE/*, BINDING*/, BINDED
    }

    static final String SS_ACTION = "swaiotos.intent.action.channel.iot.service.SS";

    private SSChannel mSSChannel = new SSChannelImpl();
    private S<IMainService> mService;

    private IMainService mBindService;

    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.e(TAG, "IOT-Channel IBinder DeathRecipient");

            if (mBindService != null) {
                mBindService.asBinder().unlinkToDeath(mDeathRecipient, 0);
                mBindService = null;
            }

            bind = BIND_STATUS.IDLE;
            mSSChannel.close();
            mService.bind();
        }
    };


    @Override
    public void open(final Context context, final IMainService service) throws Exception {
        try {
            mSSChannel.open(context, service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void open(Context context, OpenCallback callback) {
        open(context, context.getPackageName(), callback);
    }

    @Override
    public void open(final Context context, final String packageName, final OpenCallback callback) {
        if (bind == BIND_STATUS.IDLE) {
            Log.d(TAG, "IOTChannelImpl is binding...");
//            bind = BIND_STATUS.BINDING;
            try {
                performOpen(context, packageName, callback);
            } catch (Exception e) {
                bind = BIND_STATUS.IDLE;
                Log.e(TAG, "IOTChannelImpl open error---" + e.getMessage());
                e.printStackTrace();
            }
        } else if (bind == BIND_STATUS.BINDED) {
            Log.d(TAG, "IOTChannelImpl is binded ,will call back immediately");
            if (callback != null) {
                callback.onConntected(mSSChannel);
            }
        }
    }

    @Override
    public SSChannel getSSChannel() {
        return mSSChannel;
    }


    @Override
    public boolean isOpen() {
        return bind == BIND_STATUS.BINDED;
    }

    @Override
    public boolean isServiceRun(Context context, int type) {
        boolean isRun = false;
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                    .getRunningServices(40);
            int size = serviceList.size();
            String className = "swaiotos.channel.iot.tv.PADChannelService";
            if (type == 1) {
                className = "swaiotos.channel.iot.tv.TVChannelService";
            } else if (type == 2) {
                className = "swaiotos.channel.iot.tv.PADChannelService";
            } else if (type == 3) {

            }
            for (int i = 0; i < size; i++) {
                if (serviceList.get(i).service.getClassName().equals(className)) {
                    isRun = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isRun;
    }

    @Override
    public void close() {
        if (mSSChannel != null) {
            try {
                mSSChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mService != null) {
            try {
                mService.unbind();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        bind = BIND_STATUS.IDLE;
    }

    private void performOpen(final Context context, String packageName, final OpenCallback callback) throws Exception {
        final CountDownLatch latch = callback == null ? new CountDownLatch(1) : null;
        mService = new S<IMainService>(context, packageName, SS_ACTION) {
            @Override
            protected IMainService transform(IBinder service) {
                return IMainService.Stub.asInterface(service);
            }

            @Override
            protected void onConntected(final IMainService service) {
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            open(context, service);
                            if (callback != null) {
                                callback.onConntected(mSSChannel);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mService.unbind();
                            if (callback != null) {
                                callback.onError(e.getMessage());
                            }
                        }
                        if (latch != null) {
                            latch.countDown();
                        }
                    }
                });

            }
        };
        mService.bind();
        if (latch != null) {
            latch.await(10, TimeUnit.SECONDS);
        }
    }


    private abstract class S<T> implements ServiceConnection {
        private Context mContext;
        private Intent mIntent;
        private T mService;

        public S(Context context, String packageName, String action) {
            this.mContext = context;
            mIntent = new Intent(action);
            mIntent.setPackage(packageName);
        }

        public void bind() {
            mContext.bindService(mIntent, this, Context.BIND_AUTO_CREATE);
        }

        public void unbind() {
            mContext.unbindService(this);
        }

        public T getService() {
            return this.mService;
        }

        protected abstract T transform(IBinder service);

        protected abstract void onConntected(T service);

        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            Log.d(TAG, "IOT-Channel onServiceConnected...");

            mService = transform(service);
            onConntected(mService);

            mBindService = IMainService.Stub.asInterface(service);
            try {
                //server端死亡 代理回调
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            bind = BIND_STATUS.BINDED;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("S", "IOT-Channel onServiceDisconnected@" + name);
            mService = null;
            mBindService = null;
            bind = BIND_STATUS.IDLE;
            //bind();
        }
    }
}
