package swaiotos.channel.iot.ss.client;

import android.content.Intent;

import swaiotos.channel.iot.ss.ISSChannelService;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * The interface Client manager.
 *
 * @ClassName: ClientManager
 * @Author: lu
 * @CreateDate: 2020 /4/8 11:33 AM
 * @Description:
 */
public interface ClientManager {
    /**
     * The interface On client change listener.
     */
    interface OnClientChangeListener {
        int VERSION_REMOVE = -1;

        /**
         * On client change.
         *
         * @param clientID the client id
         * @param version  client的版本号，-1代表系统中无此client了
         * @see OnClientChangeListener#VERSION_REMOVE
         */
        void onClientChange(String clientID, Integer version);
    }

    /**
     * Init.
     *
     * @param binder the binder
     * @param filter the filter
     */
    void init(ISSChannelService.Stub binder, Intent filter);

    /**
     * Add on client change listener.
     *
     * @param listener the listener
     */
    void addOnClientChangeListener(OnClientChangeListener listener);

    /**
     * Remove on client change listener.
     *
     * @param listener the listener
     */
    void removeOnClientChangeListener(OnClientChangeListener listener);

    /**
     * Gets client version.
     *
     * @param clientID the client id
     * @return the client version
     */
    int getClientVersion(String clientID);

    /**
     * Start boolean.
     *
     * @param clientID the client id
     * @param message  the message
     * @return the boolean
     */
    boolean start(String clientID, IMMessage message);
}
