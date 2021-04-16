package swaiotos.channel.iot.ss.controller;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.session.Session;

/**
 * The interface Controller.
 *
 * @ClassName: IController
 * @Author: lu
 * @CreateDate: 2020 /4/13 2:42 PM
 * @Description:
 */
public interface Controller {

    /**
     * 打开指定sid的Session
     *
     * @param lsid    the target screenid
     * @param timeout the timeout
     * @return the session
     * @throws Exception the exception
     */
    Session connect(String lsid, long timeout) throws Exception;

    Session connect(Device device, long timeout) throws Exception;

    /**
     * Close session.
     *
     * @param target the session
     * @throws Exception the exception
     */
    void disconnect(Session target) throws Exception;


    void reConnectSession(Session target, boolean forceClose) throws Exception;

    /**
     *
     */
    interface JoinHandlerCallBack {
        void handleJoin(Session session);

        void handleJoinError(Exception e);
    }

    /**
     * 打开指定roomId的Session(异步调用)
     *
     * @param roomId  the target roomId
     * @param timeout the timeout
     * @return the session
     * @throws Exception the exception
     */
    Session join(String roomId, String sid, long timeout) throws Exception;

    /**
     * 打开指定roomId的Session(同步调用)
     *
     * @param roomId  the target roomId
     * @param timeout the timeout
     * @return the session
     * @throws Exception the exception
     */
    void join(String roomId, String sid, long timeout, JoinHandlerCallBack callBack) throws Exception;

    /*
     * 离开虚拟房间
     *
     */
    void leave(String userQuit) throws Exception;

    /**
     * 获取指定session设备中指定client的版本号
     *
     * @param target  the target
     * @param client  对应client的clientID
     * @param timeout
     * @return the client version
     * @throws TimeoutException the timeout exception
     */
    int getClientVersion(Session target, String client, long timeout) throws Exception;

    DeviceInfo getDeviceInfo() throws Exception;


    void connectSSETest(String lsid, IConnectResult result) throws Exception;


    void connectLocalTest(String ip, IConnectResult result) throws Exception;


}
