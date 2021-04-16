package swaiotos.channel.iot.ss.channel.im;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

/**
 * @ClassName: IMMessageCallback
 * @Author: lu
 * @CreateDate: 2020/4/1 6:52 PM
 * @Description:
 */
public interface IMMessageCallback {
    class IMMessageCallbackProxy implements IMMessageCallback {
        private IMMessageCallback callback;

        public IMMessageCallbackProxy(IMMessageCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onStart(IMMessage message) {
            if (callback != null) {
                callback.onStart(message);
            }
        }

        @Override
        public void onProgress(IMMessage message, int progress) {
            if (callback != null) {
                callback.onProgress(message, progress);
            }
        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {
            if (callback != null) {
                callback.onEnd(message, code, info);
            }
        }
    }

    IMMessageCallback defaultIMMessageCallback = new IMMessageCallback() {
        @Override
        public void onStart(IMMessage message) {

        }

        @Override
        public void onProgress(IMMessage message, int progress) {

        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {

        }
    };

    class Builder {
        public static final int METHOD_ON_START = 0;
        public static final int METHOD_ON_PROGRESS = 1;
        public static final int METHOD_ON_END = 2;

        private static final String KEY_MESSAGE = "message";
        private static final String KEY_INFO = "info";

        private static Bundle putIMMessage(IMMessage message, Bundle bundle) {
            bundle.putParcelable(KEY_MESSAGE, message);
            return bundle;
        }

        public static Message createStartMessage(IMMessage message) {
            Bundle bundle = new Bundle();
            bundle = putIMMessage(message, bundle);
            Message msg = Message.obtain();
            msg.arg1 = METHOD_ON_START;
            msg.setData(bundle);
            return msg;
        }

        public static Message createProgressMessage(IMMessage message, int progress) {
            Bundle bundle = new Bundle();
            bundle = putIMMessage(message, bundle);
            Message msg = Message.obtain();
            msg.setData(bundle);
            msg.arg1 = METHOD_ON_PROGRESS;
            msg.arg2 = progress;
            return msg;
        }

        public static Message createEndMessage(IMMessage message, int code, String info) {
            Bundle bundle = new Bundle();
            bundle = putIMMessage(message, bundle);
            bundle.putString(KEY_INFO, TextUtils.isEmpty(info) ? "" : info);
            Message msg = Message.obtain();
            msg.arg1 = METHOD_ON_END;
            msg.arg2 = code;
            msg.setData(bundle);
            return msg;
        }

        static int parseMethod(Message message) {
            return message.arg1;
        }

        static IMMessage parseIMMessage(Message message) {
            Bundle bundle = message.getData();
            if (bundle != null) {
                bundle.setClassLoader(IMMessage.class.getClassLoader());
                return bundle.getParcelable(KEY_MESSAGE);
            }
            return null;
        }

        static int parseProgress(Message message) {
            if (parseMethod(message) == METHOD_ON_PROGRESS) {
                return message.arg2;
            }
            return -1;
        }

        static String parseInfo(Message message) {
            if (parseMethod(message) == METHOD_ON_END) {
                Bundle bundle = message.getData();
                if (bundle != null) {
                    bundle.setClassLoader(IMMessage.class.getClassLoader());
                    return bundle.getString(KEY_INFO);
                }
            }
            return null;
        }

        static int parseCode(Message message) {
            if (parseMethod(message) == METHOD_ON_END) {
                return message.arg2;
            }
            return 0;
        }
    }

    void onStart(IMMessage message);

    void onProgress(IMMessage message, int progress);

    void onEnd(IMMessage message, int code, String info);
}
