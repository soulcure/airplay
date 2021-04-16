package swaiotos.channel.iot.ss.channel.im;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: IMMessageCallback
 * @Author: lu
 * @CreateDate: 2020/4/1 6:47 PM
 * @Description:
 */
final class IMMessageCallbackHandler extends Handler {
    private Messenger mMessenger;
    private Map<IMMessage, IMMessageCallback> mCallbacks = new LinkedHashMap<>();

    public IMMessageCallbackHandler() {
        this(Looper.getMainLooper());
    }

    public IMMessageCallbackHandler(Looper looper) {
        super(looper);
        mMessenger = new Messenger(this);
    }

    final void add(IMMessage message, IMMessageCallback callback) {
        synchronized (mCallbacks) {
            if (!mCallbacks.containsKey(message)) {
                mCallbacks.put(message, callback);
            }
        }
    }

    @Override
    public final void handleMessage(Message msg) {
        super.handleMessage(msg);
        int method = IMMessageCallback.Builder.parseMethod(msg);
        IMMessage message = IMMessageCallback.Builder.parseIMMessage(msg);
        if (message != null) {
            IMMessageCallback callback;
            synchronized (mCallbacks) {
                callback = mCallbacks.get(message);
            }
            if (callback == null) {
                return;
            }
            switch (method) {
                case 0: {
                    callback.onStart(message);
                    break;
                }
                case 1: {
                    int progress = IMMessageCallback.Builder.parseProgress(msg);
                    callback.onProgress(message, progress);
                    break;
                }
                case 2: {
                    int code = IMMessageCallback.Builder.parseCode(msg);
                    String info = IMMessageCallback.Builder.parseInfo(msg);
                    callback.onEnd(message, code, info);
                    break;
                }
                default:
                    break;
            }
        }
    }

    final Messenger getMessenger() {
        return mMessenger;
    }
}
