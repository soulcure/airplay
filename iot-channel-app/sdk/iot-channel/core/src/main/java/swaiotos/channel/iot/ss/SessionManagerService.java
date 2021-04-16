package swaiotos.channel.iot.ss;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.session.IOnMySessionUpdateListener;
import swaiotos.channel.iot.ss.session.IOnRoomDevicesUpdateListener;
import swaiotos.channel.iot.ss.session.IOnSessionUpdateListener;
import swaiotos.channel.iot.ss.session.ISessionManagerService;
import swaiotos.channel.iot.ss.session.RoomDevice;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;

/**
 * @ClassName: SessionManagerService
 * @Author: lu
 * @CreateDate: 2020/4/13 3:14 PM
 * @Description:
 */
public class SessionManagerService extends ISessionManagerService.Stub {
    private SSContext mSSContext;
    private final RemoteCallbackList<IOnSessionUpdateListener> mRemoteServerSessionOnUpdateListener = new RemoteCallbackList<>();
    private final RemoteCallbackList<IOnSessionUpdateListener> mRemoteConnectedSessionOnUpdateListener = new RemoteCallbackList<>();
    private final RemoteCallbackList<IOnMySessionUpdateListener> mRemoteOnMySessionUpdateListener = new RemoteCallbackList<>();
    private final RemoteCallbackList<IOnRoomDevicesUpdateListener> mRemoteOnRoomDevicesUpdateListeners = new RemoteCallbackList<>();

    public SessionManagerService(SSContext ssContext) {
        mSSContext = ssContext;

        try {
            mSSContext.getSessionManager().addConnectedSessionOnUpdateListener(mConnectedSessionOnUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSSContext.getSessionManager().addOnMySessionUpdateListener(mOnMySessionUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSSContext.getSessionManager().addServerSessionOnUpdateListener(mServerSessionOnUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mSSContext.getSessionManager().addRoomDevicesOnUpdateListener(mOnRoomDevicesUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public ParcelableObject getMySession() throws RemoteException {
        ParcelableObject object = null;
        try {
            Session my = mSSContext.getSessionManager().getMySession();
            if (my != null) {
                object = new ParcelableObject(0, "", my);
            } else {
                object = new ParcelableObject(2, "my session is empty!", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    @Override
    public ParcelableObject getConnectedSession() throws RemoteException {
        ParcelableObject object = null;
        try {
            Session target = mSSContext.getSessionManager().getConnectedSession();
            if (target != null) {
                object = new ParcelableObject(0, "", target);
            } else {
                object = new ParcelableObject(0, "no session connected!", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            object = new ParcelableObject(1, e.getMessage(), null);
        }
        return object;
    }

    private SessionManager.OnSessionUpdateListener mConnectedSessionOnUpdateListener = new SessionManager.OnSessionUpdateListener() {
        @Override
        public synchronized void onSessionConnect(Session session) {
            int n = mRemoteConnectedSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteConnectedSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionConnect(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteConnectedSessionOnUpdateListener.finishBroadcast();
        }

        @Override
        public synchronized void onSessionUpdate(Session session) {
            int n = mRemoteConnectedSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteConnectedSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionUpdate(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteConnectedSessionOnUpdateListener.finishBroadcast();
        }

        @Override
        public synchronized void onSessionDisconnect(Session session) {
            int n = mRemoteConnectedSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteConnectedSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionDisconnect(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteConnectedSessionOnUpdateListener.finishBroadcast();
        }
    };

    @Override
    public void addConnectedSessionOnUpdateListener(IOnSessionUpdateListener listener) throws RemoteException {
        mRemoteConnectedSessionOnUpdateListener.register(listener);
    }

    @Override
    public void removeConnectedSessionOnUpdateListener(IOnSessionUpdateListener listener) throws RemoteException {
        mRemoteConnectedSessionOnUpdateListener.unregister(listener);
    }

    private SessionManager.OnMySessionUpdateListener mOnMySessionUpdateListener = new SessionManager.OnMySessionUpdateListener() {
        @Override
        public void onMySessionUpdate(Session session) {
            int n = mRemoteOnMySessionUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnMySessionUpdateListener listener = mRemoteOnMySessionUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onMySessionUpdate(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteOnMySessionUpdateListener.finishBroadcast();
        }
    };

    @Override
    public void addOnMySessionUpdateListener(final IOnMySessionUpdateListener listener) throws RemoteException {
        mRemoteOnMySessionUpdateListener.register(listener);
    }

    @Override
    public void removeOnMySessionUpdateListener(IOnMySessionUpdateListener listener) throws RemoteException {
        mRemoteOnMySessionUpdateListener.unregister(listener);
    }

    private SessionManager.OnSessionUpdateListener mServerSessionOnUpdateListener = new SessionManager.OnSessionUpdateListener() {
        @Override
        public void onSessionConnect(Session session) {
            int n = mRemoteServerSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteServerSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionConnect(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteServerSessionOnUpdateListener.finishBroadcast();
        }

        @Override
        public void onSessionUpdate(Session session) {
            int n = mRemoteServerSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteServerSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionUpdate(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteServerSessionOnUpdateListener.finishBroadcast();
        }

        @Override
        public void onSessionDisconnect(Session session) {
            int n = mRemoteServerSessionOnUpdateListener.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnSessionUpdateListener listener = mRemoteServerSessionOnUpdateListener.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onSessionDisconnect(session);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteServerSessionOnUpdateListener.finishBroadcast();
        }
    };

    @Override
    public void addServerSessionOnUpdateListener(IOnSessionUpdateListener listener) throws RemoteException {
        mRemoteServerSessionOnUpdateListener.register(listener);
    }

    @Override
    public void removeServerSessionOnUpdateListener(IOnSessionUpdateListener listener) throws RemoteException {
        mRemoteServerSessionOnUpdateListener.unregister(listener);
    }

    @Override
    public List<Session> getServerSessions() throws RemoteException {
        try {
            return mSSContext.getSessionManager().getServerSessions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    @Override
    public boolean available(Session session, String channel) throws RemoteException {
        try {
            return mSSContext.getSessionManager().available(session, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isConnectSSE() throws RemoteException {
        try {
            return mSSContext.getIMChannel().isConnectSSE();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<RoomDevice> getRoomDevices() throws RemoteException {
        try {
            return mSSContext.getSessionManager().getRoomDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void addRoomDevicesOnUpdateListener(IOnRoomDevicesUpdateListener listener) throws RemoteException {
        mRemoteOnRoomDevicesUpdateListeners.register(listener);
    }

    @Override
    public void removeRoomDevicesOnUpdateListener(IOnRoomDevicesUpdateListener listener) throws RemoteException {
        mRemoteOnRoomDevicesUpdateListeners.unregister(listener);
    }

    @Override
    public void clearConnectedSessionByUser() throws RemoteException {
        try {
            mSSContext.getSessionManager().clearConnectedSessionByUser();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SessionManager.OnRoomDevicesUpdateListener mOnRoomDevicesUpdateListener = new SessionManager.OnRoomDevicesUpdateListener() {

        @Override
        public void onRoomDevicesUpdate(int count) {
            int n = mRemoteOnRoomDevicesUpdateListeners.beginBroadcast();
            try {
                for (int i = 0; i < n; i++) {
                    IOnRoomDevicesUpdateListener listener = mRemoteOnRoomDevicesUpdateListeners.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onRoomDevicesUpdate(count);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mRemoteOnRoomDevicesUpdateListeners.finishBroadcast();
        }

    };
}
