package swaiotos.channel.iot.tv.pad;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.lsid.ILSIDManagerService;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.tv.base.BasePresenter;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class PadInitPresenter extends BasePresenter<PadInitContract.View> implements PadInitContract.Presenter {

    private final String TAG = PadInitPresenter.class.getSimpleName();
    private Context mContext;
    private ILSIDManagerService mLSIDManagerService;

    public PadInitPresenter(@Nullable PadInitContract.View initView) {
        attachView(initView);
        initView.setPresenter(this);
    }


    @Override
    public void init(Context context) {
        mContext = context;

        initRegisterBroadCast();
        initStartPadSSChannelService();
    }

    @Override
    public void bind(String bindCode, String accessToken) {
    }

    @Override
    public void unBind(final String accessToken,final String sid, final int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().unBindDevice(accessToken, sid, type, new DeviceAdminManager.unBindResultListener() {
                        @Override
                        public void onSuccess(String lsid) {
                            Log.d(TAG,"unbind onsuccess!");
                            Toast.makeText(mContext,"unbind onsuccess",Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFail(String lsid, String errorType, String msg) {
                            Log.d(TAG,"unbind onfail! reason errorType"+ errorType + " msg:"+msg);
                            Toast.makeText(mContext,"unbind onfail!",Toast.LENGTH_LONG).show();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG,"unbind onfail! reason e:"+ e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void queryDevices() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Device> devices =  IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().getDevices();

                    if (getView() != null && getView().isActive()) {
                        getView().showDevices(devices);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     *
     * 启动channel自检
     * */
    private void initStartPadSSChannelService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG,"mContext"+mContext);
                    IOTAdminChannel.mananger.open(mContext, mContext.getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG,"---onConntected--TV---");
                            if (getView() != null &&  getView().isActive()) {
                                getView().refrushTips("channel open success!",true);
                            }
                            final String accessToken = ShareUtls.getInstance(mContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                            if (TextUtils.isEmpty(accessToken)) {
                                AtomicBoolean mRegisterAtomicBoolean = new AtomicBoolean(true);
                                bindLsIDManagerSerivce(mRegisterAtomicBoolean);
                                try {
                                    mLSIDManagerService.reset();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG,"---onError--TV---");
                            if (getView() != null && getView().isActive()) {
                                getView().refrushTips("channel open error:"+s,true);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void bindLsIDManagerSerivce(final AtomicBoolean mRegisterAtomicBoolean) {
        Intent intent = new Intent(Constants.COOCAA_LSID_ACTION);
        intent.setPackage(mContext.getPackageName());

        mContext.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mLSIDManagerService = ILSIDManagerService.Stub.asInterface(service);
                mRegisterAtomicBoolean.compareAndSet(true,false);
                Log.d(TAG,"--bindService LSIDManagerService success");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        }, Context.BIND_AUTO_CREATE);

        while (mRegisterAtomicBoolean.get()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initRegisterBroadCast() {
    }

    @Override
    public void detachView() {
        super.detachView();
    }

    private SessionManager.OnSessionUpdateListener mOnSessionUpdateListener = new SessionManager.OnSessionUpdateListener() {

        @Override
        public void onSessionConnect(final Session session) {
            Log.d("TV", "onSessionConnect " + session);
            //15步：
            Toast.makeText(mContext,"step 15 success",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSessionUpdate(final Session session) {
            Log.d("TV", "onSessionUpdate " + session);

        }

        @Override
        public void onSessionDisconnect(final Session session) {
            Log.d("TV", "onSessionDisconnect " + session);
        }
    };

}
