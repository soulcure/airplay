package swaiotos.channel.iot.ss;

import android.os.RemoteException;

import swaiotos.channel.iot.ss.controller.IControllerService;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: ControllerService
 * @Author: lu
 * @CreateDate: 2020/4/13 3:14 PM
 * @Description:
 */
public class ControllerService extends IControllerService.Stub {
    private SSContext mSSContext;

    public ControllerService(SSContext ssContext) {
        mSSContext = ssContext;
    }

    @Override
    public ParcelableObject connect(String lsid, long timeout) throws RemoteException {
        ParcelableObject object;
        try {
            Session session = mSSContext.getController().connect(lsid, timeout);
            object = new ParcelableObject(0, "", session);
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    @Override
    public void disconnect(Session session) throws RemoteException {
        try {
            mSSContext.getController().disconnect(session);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public ParcelableObject getClientVersion(Session target, String client, long timeout) throws RemoteException {
        ParcelableObject object;
        try {
            int version = mSSContext.getController().getClientVersion(target, client, timeout);
            ParcelableObject.ParcelableInteger obj = new ParcelableObject.ParcelableInteger(version);
            object = new ParcelableObject(0, "", obj);
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    @Override
    public ParcelableObject getDeviceInfo() throws RemoteException {
        ParcelableObject object;
        try {
            DeviceInfo deviceInfo = mSSContext.getController().getDeviceInfo();
            object = new ParcelableObject(0, "", deviceInfo);
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    @Override
    public ParcelableObject join(String roomId, String sid, long timeout) throws RemoteException {
        ParcelableObject object;
        try {
            Session session = mSSContext.getController().join(roomId, sid, timeout);
            object = new ParcelableObject(0, "", session);
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    @Override
    public void leave(String userQuit) throws RemoteException {
        try {
            mSSContext.getController().leave(userQuit);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void connectSSETest(String lsid, IConnectResult result) throws RemoteException {
        try {
            mSSContext.getController().connectSSETest(lsid, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void connectLocalTest(String ip, IConnectResult result) throws RemoteException {
        try {
            mSSContext.getController().connectLocalTest(ip, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }
}
