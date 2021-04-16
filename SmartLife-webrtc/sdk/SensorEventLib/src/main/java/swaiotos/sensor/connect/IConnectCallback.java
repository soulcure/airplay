package swaiotos.sensor.connect;

/**
 * @Author: yuzhan
 */
public interface IConnectCallback {
    void onSuccess();
    void onFail(String reason);
    void onFailOnce(String reason);
    void onClose();
    void onMessage(String msg);
}
