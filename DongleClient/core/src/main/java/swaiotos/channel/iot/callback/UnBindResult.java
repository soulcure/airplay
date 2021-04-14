package swaiotos.channel.iot.callback;


public interface UnBindResult {
    void onSuccess(String message);

    void onFail(int code, String message);
}
