package swaiotos.channel.iot.ss;

import android.content.Context;

import swaiotos.channel.iot.ss.channel.im.IIMChannel;
import swaiotos.channel.iot.ss.channel.im.IIMChannelClient;
import swaiotos.channel.iot.ss.channel.im.IIMChannelImpl;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.device.DeviceManagerClient;
import swaiotos.channel.iot.ss.device.DeviceManagerImpl;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.ss.session.SessionManagerClient;
import swaiotos.channel.iot.ss.session.SessionManagerImpl;
import swaiotos.channel.iot.utils.ipc.ParcelableBinder;

/**
 * @ClassName: SSServiceManager
 * @Author: lu
 * @CreateDate: 2020/3/21 2:14 PM
 * @Description:
 */
public class SSChannelImpl implements SSChannel {
    private SessionManagerClient mSessionManager = new SessionManagerImpl();
    private IIMChannelClient mIMChannel = new IIMChannelImpl();
    private DeviceManagerClient mDeviceManager = new DeviceManagerImpl();

    public SSChannelImpl() {
    }

    @Override
    public void open(final Context context, final IMainService service) throws Exception {
        try {
            IMainService mMainService = service;
            ParcelableBinder s = mMainService.open(context.getApplicationContext().getPackageName());
            if (s.code == 0) {
                open(ISSChannelService.Stub.asInterface(s.mBinder));
            } else {
                throw new Exception(s.extra);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void open(ISSChannelService service) throws Exception {
        create(service);
    }

    private void create(ISSChannelService service) throws Exception {
        mSessionManager.setService(service.getSessionManager());
        mIMChannel.setService(service.getIMChannel());
        mDeviceManager.setService(service.getDeviceManager());
    }

    @Override
    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    @Override
    public IIMChannel getIMChannel() {
        return mIMChannel;
    }

    @Override
    public DeviceManager getDeviceManager() {
        return mDeviceManager;
    }

    @Override
    public void close() {
        mSessionManager.close();
        mDeviceManager.close();
    }
}
