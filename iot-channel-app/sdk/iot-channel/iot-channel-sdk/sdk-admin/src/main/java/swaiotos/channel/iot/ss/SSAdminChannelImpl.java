package swaiotos.channel.iot.ss;

import android.content.Context;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.channel.im.IIMChannel;
import swaiotos.channel.iot.ss.controller.Controller;
import swaiotos.channel.iot.ss.controller.ControllerClient;
import swaiotos.channel.iot.ss.controller.ControllerImpl;
import swaiotos.channel.iot.ss.controller.IControllerService;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.device.DeviceAdminManagerClient;
import swaiotos.channel.iot.ss.device.DeviceAdminManagerImpl;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.device.IDeviceAdminManagerService;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.utils.ipc.ParcelableBinder;

/**
 * @ClassName: SSServiceManager
 * @Author: lu
 * @CreateDate: 2020/3/21 2:14 PM
 * @Description:
 */
public class SSAdminChannelImpl implements SSAdminChannel {

    private ControllerClient mController = new ControllerImpl();
    private DeviceAdminManagerClient mDeviceAdminManager = new DeviceAdminManagerImpl();

    public SSAdminChannelImpl() {
    }

    @Override
    public void open(final Context context, final IMainService mainService) throws Exception {
        try {
            ParcelableBinder service = mainService.open(context.getApplicationContext().getPackageName());
            open(ISSChannelService.Stub.asInterface(service.mBinder));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void open(ISSChannelService service) throws Exception {
        create(service);
    }

    private void create(ISSChannelService service) throws Exception {
        IControllerService controllerService = IControllerService.Stub.asInterface(service.getBinder(IOTAdminChannel.SERVICE_CONTROLLER));
        mController.setService(controllerService);

        IDeviceAdminManagerService s = IDeviceAdminManagerService.Stub.asInterface(service.getBinder(IOTAdminChannel.SERVICE_DEVICEADMIN));
        mDeviceAdminManager.setService(s);
        mDeviceAdminManager.setDeviceManager(getDeviceManager());
    }


    @Override
    public Controller getController() {
        return mController;
    }

    @Override
    public SessionManager getSessionManager() {
        return IOTChannel.mananger.getSSChannel().getSessionManager();
    }

    @Override
    public IIMChannel getIMChannel() {
        return IOTChannel.mananger.getSSChannel().getIMChannel();
    }


    @Override
    public DeviceManager getDeviceManager() {
        return IOTChannel.mananger.getSSChannel().getDeviceManager();
    }

    @Override
    public DeviceAdminManager getDeviceAdminManager() {
        return mDeviceAdminManager;
    }

    @Override
    public void close() {
        mController.close();
        mDeviceAdminManager.close();
        IOTChannel.mananger.getSSChannel().close();
    }
}
