package swaiotos.channel.iot.ss.session;

/**
 * The interface Session manager server.
 *
 * @ClassName: SessionManager
 * @Author: lu
 * @CreateDate: 2020 /3/30 3:44 PM
 * @Description:
 */
public interface SessionManagerServer extends SessionManager {

    void open();

    /**
     * Update my session.
     *
     * @param key    the key
     * @param value  the value
     * @param notify the notify
     */
    void updateMySession(String key, String value, boolean notify);

    /**
     * Update my lsid.
     *
     * @param notify the notify
     */
    void updateMyLSID(boolean notify);

    /**
     * 更新被当前设备连接的Session
     *
     * @param session the session
     */
    void setConnectedSession(Session session);

    /**
     * 清除被当前设备连接的Session
     */
    void clearConnectedSession();

    boolean hasServerSession(Session session);

    /**
     * 增加连接当前设备的Session
     *
     * @param session the session
     * @return the boolean
     */
    boolean addServerSession(Session session);

    /**
     * 更新连接上当前设备或被当前设备连接的Session信息
     *
     * @param session the session
     * @return the boolean
     */
    boolean updateSession(Session session);

    /**
     * 通过lsid获取连接上当前设备的Session
     *
     * @param lsid the lsid
     * @return the session
     */
    Session getServerSession(String lsid);

    /**
     * 判断某个Session是否还连接了当前设备上
     *
     * @param session the session
     * @return the boolean
     */
    boolean containServerSession(Session session);

    /**
     * 删除连接当前设备的Session
     *
     * @param session the session
     * @return the boolean
     */
    boolean removeServerSession(Session session);

    /**
     * 请求当前连接room的连接数
     */
    void queryConnectedRoomDevices();

    /**
     * @param type  1:sse 2:local 0: 网络未联网
     * @param state 1:断开  2：连接
     */
    void connectChannelSessionState(int type, int state);

    /**
     * @param type  1:sse 2:local
     * @param state 1:连接中  2：连接中  0：连接后
     */
    void connectingChannelSessionState(int type, int state);

    void saveHandlerConnectSession(String handleConnect);

    void clearHandlerConnectSession(String lsId);

    void setSessionChanged(boolean b);

    boolean getSessionChanged();

    void refreshChannelState();
}
