// ISessionManagerService.aidl
package swaiotos.channel.iot.ss.session;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.IOnSessionUpdateListener;
import swaiotos.channel.iot.ss.session.IOnMySessionUpdateListener;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;
import swaiotos.channel.iot.ss.session.RoomDevice;
import swaiotos.channel.iot.ss.session.IOnRoomDevicesUpdateListener;

interface ISessionManagerService {
    /**
     * Add on my session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addOnMySessionUpdateListener(in IOnMySessionUpdateListener listener);

    /**
     * Remove on my session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeOnMySessionUpdateListener(in IOnMySessionUpdateListener listener);

    /**
     * 获取当前设备的Session
     *
     * @return the my session
     * @throws Exception the exception
     */
    ParcelableObject getMySession();

    /**
     * Add on session connect update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addConnectedSessionOnUpdateListener(in IOnSessionUpdateListener listener);

    /**
     * Remove on session connect update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeConnectedSessionOnUpdateListener(in IOnSessionUpdateListener listener);

    /**
     * Gets the device Session for the current device connection
     *
     * @return the connected session
     * @throws Exception the exception
     */
    ParcelableObject getConnectedSession();

    /**
     * Set the connect Session state monitor
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void addServerSessionOnUpdateListener(in IOnSessionUpdateListener listener);

    /**
     * Remove on session update listener.
     *
     * @param listener the listener
     * @throws Exception the exception
     */
    void removeServerSessionOnUpdateListener(in IOnSessionUpdateListener listener);

    /**
     * Gets the Session list of the current device connected
     *
     * @return the server sessions
     * @throws Exception the exception
     */
    List<Session> getServerSessions();


    boolean available(in Session session,String channel);


    boolean isConnectSSE();

    /**
      * Gets the Device of connection  to the current room
      *
      * @return the server room of device
       * @throws Exception the exception
    */
    List<RoomDevice> getRoomDevices();

    /**
    * Listen for changes in the number of room connections
    *
    */
    void addRoomDevicesOnUpdateListener(in IOnRoomDevicesUpdateListener listener);
    void removeRoomDevicesOnUpdateListener(in IOnRoomDevicesUpdateListener listener);

    void clearConnectedSessionByUser();
}
