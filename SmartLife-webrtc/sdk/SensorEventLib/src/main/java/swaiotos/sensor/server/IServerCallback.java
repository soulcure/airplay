package swaiotos.sensor.server;

/**
 * @Author: yuzhan
 */
public interface IServerCallback {
    void onClientAdd(String id);
    void onClientRemove(String id);
    void onClientEmpty();
    void onClose();
}
