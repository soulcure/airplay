package swaiotos.channel.iot.tv.init.devices;


import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.IOTAdminChannelImpl;
import swaiotos.channel.iot.common.usecase.BindCallBackUseCase;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.tv.base.BasePresenter;
import swaiotos.channel.iot.tv.init.InitContract;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class DevicesPresenter extends BasePresenter<DevicesContract.View> implements DevicesContract.Presenter {

    private final String TAG = DevicesPresenter.class.getSimpleName();

    private Context mContext;
    private DeviceBindStatus mDeviceBindStatus;

    public DevicesPresenter(@Nullable DevicesContract.View initView) {
        attachView(initView);
        initView.setPresenter(this);
    }


    @Override
    public void init(Context context) {
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Device> devices = IOTAdminChannelImpl.mananger.getSSAdminChannel().getDeviceAdminManager().getDevices();
                    if (getView() != null && getView().isActive()) {
                        getView().refreshDevices(devices);
                    }

                    mDeviceBindStatus = new DeviceBindStatus();
                    IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().addDeviceBindListener(mDeviceBindStatus);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void detachView() {
        if (mDeviceBindStatus != null) {
            try {
                IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().removeDeviceBindListener(mDeviceBindStatus);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        super.detachView();
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
                        getView().refreshDevices(list);
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
                    List<Device> list = IOTAdminChannel.mananger.getSSAdminChannel().getDeviceManager().updateDeviceList();
                    if (getView() != null && getView().isActive()) {
                        getView().refreshDevices(list);
                    }

                }
            });
        }
    }

}
