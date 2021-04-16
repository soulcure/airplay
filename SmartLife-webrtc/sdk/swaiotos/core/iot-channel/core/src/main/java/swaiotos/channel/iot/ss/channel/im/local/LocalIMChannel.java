package swaiotos.channel.iot.ss.channel.im.local;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.base.BaseChannel;
import swaiotos.channel.iot.ss.channel.base.local.LocalChannel;
import swaiotos.channel.iot.ss.channel.im.IMChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.ss.config.PortConfig;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.IpV4Util;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: LocalIMChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:54 PM
 * @Description:
 */
public class LocalIMChannel implements IMChannel, IStreamChannel.Receiver, BaseChannel.Callback {
    private static final String TAG = "local-im";

    class Client implements IStreamChannel.SenderMonitor {
        public final String lsid;
        public final String ip;
        public final int port;
        public IStreamChannel.Sender sender;

        public Client(LocalChannel channel, String lsid, String ip,
                      int port, TcpClientResult callback, Receiver receiver) {
            this.lsid = lsid;
            this.ip = ip;
            this.port = port;
            this.sender = newSender(channel, ip, port, callback, receiver);
        }

        private IStreamChannel.Sender newSender(LocalChannel channel, String ip, int port,
                                                TcpClientResult callback, Receiver receiver) {
            IStreamChannel.Sender sender = channel.openSender(ip, port, callback, receiver);
            sender.setSenderMonitor(this);
            AndroidLog.androidLog("StreamChannel newSender create");
            return sender;
        }

        boolean available() {
            return sender.available();
        }

        @Override
        public String toString() {
            return "Client[" + lsid + "@" + ip + ":" + port + "]";
        }

        @Override
        public void onAvailableChanged(boolean available) {
            Log.d(TAG, this + " onAvailableChanged " + available);
        }
    }

    private final SSContext mSSContext;
    private final LocalChannel mLocalChannel;
    private String mAddress;
    private int port;
    private Receiver mReceiver;

    private final Map<String, Client> mClients = new ConcurrentHashMap<>();

    public LocalIMChannel(SSContext context, LocalChannel localChannel) {
        mSSContext = context;
        mLocalChannel = localChannel;
        mLocalChannel.addCallback(this);
    }

    @Override
    public String open() throws IOException {
        mLocalChannel.addCallback(this);
        performOpen(false);
        return getAddress();
    }

    private void performOpen(boolean notify) {
        port = mLocalChannel.openReceiver(this);
        update(notify);
    }

    private void update(boolean notify) {
        String ip = mLocalChannel.getAddress();
        if (TextUtils.isEmpty(ip)) {
            mAddress = null;
        } else {
            mAddress = ip + ":" + port;
        }
    }

    @Override
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public boolean available() {
        return mLocalChannel.available();
    }

    @Override
    public void close() throws IOException {
        mLocalChannel.removeCallback(this);
        performClose();
    }

    private void performClose() {
        mLocalChannel.closeReceiver(port);
        synchronized (mClients) {
            Collection<Client> clients = mClients.values();
            for (Client client : clients) {
                mLocalChannel.closeSender(client.sender);
            }
            mClients.clear();
        }
        update(true);
    }

    @Override
    public void openClient(final Session session, final TcpClientResult callback) {
        final String lsid = session.getId();
        synchronized (mClients) {
            Client client = mClients.get(lsid);
            if (client == null) {
                AndroidLog.androidLog("openClient lsid:" + lsid);
                final String ip = session.getExtra(SSChannel.STREAM_LOCAL);
                if (TextUtils.isEmpty(ip)) {
                    mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                            Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);
                    return;
                }
                mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                        Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTING);

                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        int port = PortConfig.getLocalServerPort(mSSContext.getContext().getPackageName());
                        Client client = new Client(mLocalChannel, lsid, ip, port, callback, mReceiver);
                        mClients.put(lsid, client);
                    }
                });

            } else {
                if (callback != null && client.available()) {
                    mSSContext.postDelay(new Runnable() {
                        @Override
                        public void run() {
                            mSSContext.getSessionManager().connectChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                                    Constants.COOCAA_IOT_CHANNEL_STATE_CONNECT);
                            if (callback != null) {
                                callback.onResult(0, "local is connected");
                            }
                        }
                    }, 10);
                }
            }
        }
    }

    @Override
    public void reOpenLocalClient(final Session session) {
        AndroidLog.androidLog("reOpenLocalClient start");

        final String lsid = session.getId();
        synchronized (mClients) {
            final Client client = mClients.get(lsid);
            if (client != null && !client.available()) {
                mLocalChannel.closeSender(client.sender);
                mClients.remove(lsid);
                AndroidLog.androidLog("reOpenLocalClient remove");
            }

            final String ip = session.getExtra(SSChannel.STREAM_LOCAL);
            if (TextUtils.isEmpty(ip) || ip.equals("null")) {
                AndroidLog.androidLog("reOpenLocalClient error and ip is empty");
                return;
            }
            mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                    Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTING);
            int port = PortConfig.getLocalServerPort(mSSContext.getContext().getPackageName());
            Client newClient = new Client(mLocalChannel, lsid, ip, port, new TcpClientResult() {
                @Override
                public void onResult(int code, String message) {
                    if (code == 0) {
                        mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                                Constants.COOCAA_IOT_CHANNEL_TYPE_CONNECTED);
                    } else {
                        mSSContext.getSessionManager().connectingChannelSessionState(Constants.COOCAA_IOT_CHANNEL_TYPE_LOCAL,
                                Constants.COOCAA_IOT_CHANNEL_STATE_DISCONNECT);
                    }
                }
            }, mReceiver);

            mClients.put(lsid, newClient);
            AndroidLog.androidLog("reOpenLocalClient end");
        }
    }

    @Override
    public void reOpenSSE() {
        //do nothing
    }

    @Override
    public boolean available(Session session) {
        String lsid = session.getId();
        synchronized (mClients) {
            Client client = mClients.get(lsid);
            if (client != null) {
                return client.available();//mLocalChannel.available(client.sender);
            }
        }
        Log.d("yao", "LocalIMChannel available session client is null");
        return false;
    }

    @Override
    public void closeClient(Session session, boolean forceClose) {
        String lsid = session.getId();
        synchronized (mClients) {
            Client client = mClients.get(lsid);
            if (client != null) {
                if (forceClose) {
                    mLocalChannel.closeSender(client.sender);
                    mClients.remove(lsid);
                    client = null;
                    System.gc();
                } else {
                    if (!client.available()) {
                        mLocalChannel.closeSender(client.sender);
                        mClients.remove(lsid);
                        client = null;
                        System.gc();
                    }
                }

            }

        }
    }

    @Override
    public void send(final IMMessage message, IMMessageCallback callback) throws Exception {
        Session target = message.getTarget();
        send(target, message, callback);
    }

    @Override
    public void send(IMMessage message) throws Exception {
        try {
            send(message, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(Session target, IMMessage message) throws Exception {
        send(target, message, null);
    }

    @Override
    public void send(Session target, IMMessage message, IMMessageCallback callback) throws Exception {
        Client client = getClient(target);
        if (client != null) {
            if (callback != null) {
                callback.onStart(message);
            }
            client.sender.send(message, callback);
        } else {
            if (callback != null) {
                callback.onEnd(message, -1, "tcp local send error");
            }
        }


    }

    @Override
    public boolean serverSend(IMMessage message, IMMessageCallback callback) throws Exception {
        if (mLocalChannel != null) {
            return mLocalChannel.sendServerMessage(message);
        }
        return false;
    }

    @Override
    public List<String> serverSendList() throws Exception {
        if (mLocalChannel != null) {
            return mLocalChannel.serverConnectList();
        }
        return null;
    }

    @Override
    public void removeServerConnect(String sid) {
        if (mLocalChannel != null) {
            mLocalChannel.removeServerConnect(sid);
        }
    }

    @Override
    public void onReceive(byte[] data) {
        String content = new String(data);
        try {
            IMMessage message = IMMessage.Builder.decode(content);
            if (mReceiver != null) {
                mReceiver.onReceive(this, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String type() {
        return SSChannel.STREAM_LOCAL;
    }

    private Client getClient(Session session) {
        String lsid = session.getId();
        synchronized (mClients) {
            return mClients.get(lsid);
        }
    }

    private void resetConnections() {
        try {
            Session session = mSSContext.getSessionManager().getConnectedSession();
            if (session != null) {
                reOpenLocalClient(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(BaseChannel channel) {
        mSSContext.post(new Runnable() {
            @Override
            public void run() {
                //performOpen(true);  //add by colin ,切换网络，socket server不变
                update(true);
                resetConnections();
            }
        });
    }

    @Override
    public void onDisconnected(BaseChannel channel) {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                //performClose(); //add by colin ,切换网络，socket server不变
                Collection<Client> clients = mClients.values();
                for (Client client : clients) {
                    mLocalChannel.closeSender(client.sender);
                }
                mClients.clear();
                //update(true);
            }
        });
    }

    private boolean checkCommonNet(Session session) {
        try {
            Session mySession = mSSContext.getSessionManager().getMySession();
            if (session == null || session.getExtras().size() <= 0
                    || mySession == null || mySession.getExtras().size() <= 0)
                return false;
            String sessionIP = session.getExtras().get(SSChannel.STREAM_LOCAL);
            Map<String, String> extras = mySession.getExtras();
            String mySessionIP = extras.get(SSChannel.STREAM_LOCAL);


            Log.e("yao", "LocalIMChannel checkCommonNet=mySessionIP:" + mySessionIP + "  target ip=" + sessionIP);
            if (TextUtils.isEmpty(sessionIP) || TextUtils.isEmpty(mySessionIP)) {
                return false;
            }

            if (IpV4Util.checkSameSegmentByDefault(sessionIP, mySessionIP)) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
