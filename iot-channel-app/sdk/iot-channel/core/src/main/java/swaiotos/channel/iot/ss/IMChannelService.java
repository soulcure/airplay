package swaiotos.channel.iot.ss;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IIMChannelService;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.utils.ThreadManager;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: IMChannelService
 * @Author: lu
 * @CreateDate: 2020/4/11 3:48 PM
 * @Description:
 */
public class IMChannelService extends IIMChannelService.Stub {
    private static class Callback implements IMMessageCallback {
        private Map<IMMessage, Messenger> callbacks = new LinkedHashMap<>();

        public void add(IMMessage message, Messenger messenger) {
            synchronized (callbacks) {
                if (!callbacks.containsKey(message)) {
                    callbacks.put(message, messenger);
                }
            }
        }

        @Override
        public void onStart(IMMessage message) {
            Messenger messenger;
            synchronized (callbacks) {
                messenger = callbacks.get(message);
            }
            if (messenger != null) {
                Message msg = IMMessageCallback.Builder.createStartMessage(message);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onProgress(IMMessage message, int progress) {
            Messenger messenger;
            synchronized (callbacks) {
                messenger = callbacks.get(message);
            }
            if (messenger != null) {
                Message msg = IMMessageCallback.Builder.createProgressMessage(message, progress);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {
            Messenger messenger;
            synchronized (callbacks) {
                messenger = callbacks.get(message);
                callbacks.remove(message);
            }

            if (messenger != null) {
                Message msg = IMMessageCallback.Builder.createEndMessage(message, code, info);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Callback mCallback;
    private SSContext mSSContext;

    IMChannelService(SSContext ssContext) {
        mSSContext = ssContext;
        mCallback = new Callback();
    }

    @Override
    public void send(final IMMessage message, final Messenger callback) throws RemoteException {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                mCallback.add(message, callback);
                try {
                    mSSContext.getIMChannel().send(message, mCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public ParcelableObject sendSync(IMMessage message, Messenger callback, long timeout) throws RemoteException {
        mCallback.add(message, callback);
        ParcelableObject<IMMessage> object;
        try {
            IMMessage reply = mSSContext.getIMChannel().sendSync(message, mCallback, timeout);
            object = new ParcelableObject(0, "", reply);
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(-1, e.getMessage(), null);
        }
        return object;
    }


    @Override
    public void reset(String sid, String token) throws RemoteException {
        mSSContext.reset(sid, token);
    }

    @Override
    public void resetSidAndUserId(String sid, String token, String userId) throws RemoteException {
        mSSContext.reset(sid, token, userId);
    }

    @Override
    public void sendBroadCast(final IMMessage message, final Messenger callback) throws RemoteException {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                mCallback.add(message, callback);
                try {
                    mSSContext.getIMChannel().sendBroadCast(message, mCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public String fileService(String path) throws RemoteException {
        try {
            return mSSContext.getWebServer().uploadFile(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
