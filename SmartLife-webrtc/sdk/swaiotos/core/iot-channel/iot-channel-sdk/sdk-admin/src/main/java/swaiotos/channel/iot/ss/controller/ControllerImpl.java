package swaiotos.channel.iot.ss.controller;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: ControllerImpl
 * @Author: lu
 * @CreateDate: 2020/4/13 2:49 PM
 * @Description:
 */
public class ControllerImpl implements ControllerClient {
    private IControllerService mService;

    public ControllerImpl() {
    }

    @Override
    public void setService(IControllerService service) {
        mService = service;
    }

    @Override
    public Session connect(String lsid, long timeout) throws Exception {
        ParcelableObject<Session> session = mService.connect(lsid, timeout);
        if (session.code == 0 && session.object != null) {
            return session.object;
        }
        throw new Exception(session.extra);
    }

    @Override
    public Session connect(Device device, long timeout) throws Exception {
        return null;
    }

    @Override
    public void disconnect(Session session) throws Exception {
        mService.disconnect(session);
    }

    @Override
    public void reConnectSession(Session target, boolean forceClose) throws Exception {
        //do nothing
    }

    @Override
    public Session join(String roomId, final String sid, long timeout) throws Exception {
        ParcelableObject<Session> session = mService.join(roomId, sid, timeout);
        if (session.code == 0 && session.object != null) {
            return session.object;
        }
        throw new Exception(session.extra);
    }

    @Override
    public void join(final String roomId, final String sid, final long timeout, final JoinHandlerCallBack callBack) throws Exception {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    callBack.handleJoin(join(roomId, sid, timeout));
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.handleJoinError(e);
                }
            }
        });
    }

    @Override
    public void leave(String userQuit) throws Exception {
        mService.leave(userQuit);
    }

    @Override
    public int getClientVersion(Session target, String client, long timeout) throws Exception {
        ParcelableObject<ParcelableObject.ParcelableInteger> value = mService.getClientVersion(target, client, timeout);
        if (value.code == 0 && value.object != null) {
            return value.object.value;
        }
        throw new Exception(value.extra);
    }

    @Override
    public DeviceInfo getDeviceInfo() throws Exception {
        ParcelableObject<DeviceInfo> value = mService.getDeviceInfo();
        if (value.code == 0 && value.object != null) {
            return value.object;
        }
        throw new Exception(value.extra);
    }

    @Override
    public void connectSSETest(String lsid, IConnectResult result) throws Exception {
        mService.connectSSETest(lsid, result);
    }

    @Override
    public void connectLocalTest(String ip, IConnectResult result) throws Exception {
        mService.connectLocalTest(ip, result);
    }

    @Override
    public void close() {

    }
}
