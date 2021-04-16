package swaiotos.channel.iot.ss.session;


import android.os.RemoteException;

import java.util.List;

/**
 * The interface Session manager.
 *
 * @ClassName: ISessionManager
 * @Author: lu
 * @CreateDate: 2020 /4/13 2:42 PM
 * @Description:
 */
public interface SessionManager {
    /**
     * The interface On my session update listener.
     */
    interface OnMySessionUpdateListener {
        /**
         * On my session update.
         *
         * @param mySession the my session
         */
        void onMySessionUpdate(Session mySession);
    }

    /**
     * 有设备连接(被连、主动连)后的各种状态回调
     */
    interface OnSessionUpdateListener {
        /**
         * On session connect.
         *
         * @param session the session
         */
        void onSessionConnect(Session session);

        /**
         * On session update.
         *
         * @param session the session
         */
        void onSessionUpdate(Session session);

        /**
         * On session disconnect.
         *
         * @param session the session
         */
        void onSessionDisconnect(Session session);
    }

    interface OnRoomDevicesUpdateListener {

        void onRoomDevicesUpdate(int count);

    }


    /**
     * Add on my session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addOnMySessionUpdateListener(OnMySessionUpdateListener listener) throws Exception;

    /**
     * Remove on my session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeOnMySessionUpdateListener(OnMySessionUpdateListener listener) throws Exception;

    /**
     * 获取当前设备的Session
     *
     * @return the my session
     * @throws Exception the exception
     */
    Session getMySession() throws Exception;

    /**
     * Add on session connect update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addConnectedSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception;

    /**
     * Remove on session connect update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeConnectedSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception;

    /**
     * 获取当前设备连接的设备Session
     *
     * @return the connected session
     * @throws Exception the exception
     */
    Session getConnectedSession() throws Exception;

    /**
     * 设置已connect的Session状态监听
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addServerSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception;

    /**
     * Remove on session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeServerSessionOnUpdateListener(OnSessionUpdateListener listener) throws Exception;

    /**
     * add room device connected update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addRoomDevicesOnUpdateListener( OnRoomDevicesUpdateListener listener) throws Exception;

    /**
     * remove room device connected update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeRoomDevicesOnUpdateListener(OnRoomDevicesUpdateListener listener) throws Exception;

    /**
     * 获取连接上当前设备的Session列表
     *
     * @return the server sessions
     * @throws Exception the exception
     */
    List<Session> getServerSessions() throws Exception;

    /**
     * 获取连接上当前room下的Devices列表
     *
     * @return the server sessions
     * @throws Exception the exception
     */
    List<RoomDevice> getRoomDevices() throws Exception;

    boolean available(Session session, String channel) throws Exception;


    boolean isConnectSSE() throws Exception;
    /**
     * 清除被当前设备连接的Session
     */
    void clearConnectedSessionByUser() throws Exception;

}
