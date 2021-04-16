package swaiotos.sensor.server;

import android.text.TextUtils;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.sensor.tm.TM;

/**
 * @Author: yuzhan
 */
public class ClientManager {
    private IServerCallback callback;

    private Map<String, WebSocket> clientMap = new ConcurrentHashMap<>();
    private Map<WebSocket, String> clientIdMap = new ConcurrentHashMap<>();
    private Map<String, Runnable> heartBeatRunnableMap = new ConcurrentHashMap<>();

    private final static long TIMEOUT = 40000;//10s一次心跳，40秒收不到认为client断开

    private final static String TAG = "SSCServer";

    public void addClient(String id, WebSocket socket) {
        if(TextUtils.isEmpty(id)) {
            return ;
        }
        clientMap.put(id, socket);
        clientIdMap.put(socket, id);
        if(callback != null) {
            callback.onClientAdd(id);
        }

        if(heartBeatRunnableMap.get(id) == null) {
            heartBeatRunnableMap.put(id, new HeartBeatRunnable(id));
        }
        TM.Companion.removeIO(heartBeatRunnableMap.get(id));
        TM.Companion.io(heartBeatRunnableMap.get(id), TIMEOUT);
    }

    public void removeClient(String id) {
        if(TextUtils.isEmpty(id)) {
            return ;
        }
        if(heartBeatRunnableMap.get(id) != null) {
            TM.Companion.removeIO(heartBeatRunnableMap.get(id));
        }
        heartBeatRunnableMap.remove(id);

        WebSocket conn = clientMap.get(id);
        if(conn != null) {
            clientIdMap.remove(conn);
            try {
                WebSocketImpl impl = null;
                if(isOpen(conn)) {
                    conn.close(1000, "server-receive-client-stop");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(callback != null) {
                callback.onClientRemove(id);
            }
        }
        clientMap.remove(id);
        if(heartBeatRunnableMap.isEmpty()) {
            if(callback != null)
                callback.onClientEmpty();
        }
    }
    
    public int getClientSize() {
        return clientMap.size();
    }

    private boolean isOpen(WebSocket conn) {
        return conn != null && (conn.getReadyState() == WebSocket.READYSTATE.OPEN);
    }

    public void removeClient(WebSocket conn) {
        if(conn == null) {
            return ;
        }
        String id = clientIdMap.get(conn);
        removeClient(id);
    }

    public boolean isEmpty() {
        return clientMap.isEmpty();
    }

    public Set<String> clientCids() {
        return clientMap.keySet();
    }

    public WebSocket getClient(String id) {
        return clientMap.get(id);
    }

    public String getClientId(WebSocket conn) {
        return clientIdMap.get(conn);
    }

    public void setCallback(IServerCallback callback) {
        this.callback = callback;
    }

    //设备连接前
    public void onClientTryConnect(String id) {
        if(!TextUtils.isEmpty(id)) {
            if(heartBeatRunnableMap.get(id) == null) {
                heartBeatRunnableMap.put(id, new HeartBeatRunnable(id));
            }
            TM.Companion.removeIO(heartBeatRunnableMap.get(id));
            TM.Companion.io(heartBeatRunnableMap.get(id), TIMEOUT);
        }
    }

    public void onClientHeartBeat(WebSocket socket) {
        String id = clientIdMap.get(socket);
        Log.d(TAG, "onClientHeartBeat, id=" + id);
        if(!TextUtils.isEmpty(id)) {
            if(heartBeatRunnableMap.get(id) == null) {
                heartBeatRunnableMap.put(id, new HeartBeatRunnable(id));
            }
            TM.Companion.removeIO(heartBeatRunnableMap.get(id));
            TM.Companion.io(heartBeatRunnableMap.get(id), TIMEOUT);
        }
    }

    public void onClientHeartBeat(String id, WebSocket socket) {
        if(!TextUtils.isEmpty(id)) {
            if(heartBeatRunnableMap.get(id) == null) {
                heartBeatRunnableMap.put(id, new HeartBeatRunnable(id));
            }
            TM.Companion.removeIO(heartBeatRunnableMap.get(id));
            TM.Companion.io(heartBeatRunnableMap.get(id), TIMEOUT);
        }
    }

    private class HeartBeatRunnable implements Runnable {
        public String id;
        public HeartBeatRunnable(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            Log.d(TAG, "heartbeat timeout, remove client id=" + id + ", m.hashCode=" + this.hashCode() + ", r=" + this);
            removeClient(id);
        }
    }
}
