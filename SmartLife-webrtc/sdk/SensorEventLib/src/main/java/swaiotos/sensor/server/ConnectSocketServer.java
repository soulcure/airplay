package swaiotos.sensor.server;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import swaiotos.sensor.data.ClientCmdInfo;

/**
 * @Author: yuzhan
 */
public class ConnectSocketServer extends WebSocketServer implements IConnectServer {

    private static final String TAG = "SSCServer";
    private ClientManager clientManager = new ClientManager();
    private IServerCallback callback;

    public ConnectSocketServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }

    @Override
    public void start() {
        try {
            Log.d(TAG, "inner socker server call start.");
            super.start();
        } catch (Exception e) {
            Log.d(TAG, "already started ConnectSocketServer.");
//            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
//        try {
//            super.stop();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void setCallback(IServerCallback callback) {
        clientManager.setCallback(callback);
        this.callback = callback;
    }

    @Override
    public void broadcast(String text) {
        Log.d(TAG, "broadcast : " + text);
        super.broadcast(text);
    }

    @Override
    public void broadcastTo(String text, String cid) {
        Log.d(TAG, "broadcast to single : " + cid);
        broadcastTo(text, Arrays.asList(new String[]{cid}));
    }

    @Override
    /**
     * 发送给指定手机端，排除单个端
     * @param text
     */
    public void broadcastExclude(String text, String cid) {
        Log.d(TAG, "broadcastExclude : " + cid);
        Set<String> cids = new HashSet<>(clientManager.clientCids());
        Iterator<String> iter  = cids.iterator();
        while(iter.hasNext()) {
            if(iter.next().equals(cid)) {
                iter.remove();
            }
        }
        broadcastTo(text, cids);
    }

    @Override
    /**
     * 发送给指定手机端
     * @param text
     * @param clients
     */
    public void broadcastTo(String text, Collection<String> clients) {
        Log.d(TAG, "broadcast to : " + clients + ", text=" + text);
        List<WebSocket> clientList = new ArrayList<>(clients.size());
        for(String cid : clients) {
            WebSocket client = clientManager.getClient(cid);
            if(client != null) {
                clientList.add(client);
            }
        }
        broadcast(text, clientList);
    }

    @Override
    public int getClientSize() {
        return clientManager.getClientSize();
    }

    @Override
    public void onClientTryConnect(String cid) {
        clientManager.onClientTryConnect(cid);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d(TAG, "server onOpen...conn=" + conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "server onClose... code=" + code + ", reason=" + reason);
        clientManager.removeClient(conn);
        if(callback != null) {
            callback.onClose();
        }
//        if(clientManager.isEmpty()) {
//            if(callback != null) {
//                callback.onClientEmpty();
//            }
//        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ClientCmdInfo info = JSON.parseObject(message, ClientCmdInfo.class);
        Log.d(TAG, "server onMessage cmd=" + info.cmd);
        if(ClientCmdInfo.CMD_CLIENT_CONNECT.equals(info.cmd)) {
            Log.d(TAG, "add client, cid=" + info.cid + ", conn=" + conn);
            clientManager.addClient(info.cid, conn);
            EventBus.getDefault().post(info);
        } else if(ClientCmdInfo.CMD_CLIENT_MOTION_EVENT.equals(info.cmd)) {
            Log.d(TAG, "receive client motion event, cid=" + info.cid + ", conn=" + conn);
            EventBus.getDefault().post(info);
        } else if(ClientCmdInfo.CMD_CLIENT_STOP.equals(info.cmd)) {
            Log.d(TAG, "receive client stop, cid=" + info.cid + ", conn=" + conn);
            EventBus.getDefault().post(info);
        } else {
            Log.d(TAG, "receive client cmd, cid=" + info.cid + ", conn=" + conn);
            EventBus.getDefault().post(info);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        Log.e(TAG, "server onError : " + ex);
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "server onStart... address=" + getAddress().getHostName() + ", port=" + getAddress().getPort());
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        Log.e(TAG, "server onWebsocketPing : " + f);
        clientManager.onClientHeartBeat(conn);
        super.onWebsocketPing(conn, f);
    }
}
