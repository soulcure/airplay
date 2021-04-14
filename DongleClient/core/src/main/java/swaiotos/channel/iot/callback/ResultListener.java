package swaiotos.channel.iot.callback;

public interface ResultListener {
    public static final int SUCCESS = 0;
    public static final int SSE_OFFLINE = -1;
    public static final int SSE_UNKNOWN_ERROR = -2;
    public static final int LOCAL_OFFLINE = -3;

    void onResult(int code, String msg);
}