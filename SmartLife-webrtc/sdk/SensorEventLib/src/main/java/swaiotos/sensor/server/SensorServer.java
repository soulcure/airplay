package swaiotos.sensor.server;

import android.content.Context;
import android.net.Uri;
import android.os.DeadObjectException;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.Collection;
import java.util.UUID;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.sensor.channel.ChannelMsgSender;
import swaiotos.sensor.channel.IMsgSender;
import swaiotos.sensor.data.ClientCmdInfo;
import swaiotos.sensor.data.ServerCmdInfo;
import swaiotos.sensor.mgr.InfoManager;
import swaiotos.sensor.server.data.ServerBusinessInfo;
import swaiotos.sensor.server.data.ServerInfo;
import swaiotos.sensor.utils.NetUtils;

/**
 * @Author: yuzhan
 */
public class SensorServer {

    private Context context;
    private ServerBusinessInfo businessInfo;
    private IMsgSender msgSender;
    private IConnectServer server;
    private volatile boolean bStart;
    InfoManager infoManager;
    int port = 65001;
    int testPort = 0;

    private static String globalId = null;

    private static final String TAG = "SSCServer";

    public SensorServer(Context context, ServerBusinessInfo businessInfo) {
        Log.d(TAG, "create SSCServer....");
        this.context = context;
        this.businessInfo = businessInfo;
        msgSender = new ServerChannelMsgSender(context, businessInfo.clientSSId);
        ChannelMsgSender.TAG = TAG;

        int port = newServer();

        Log.d(TAG, "create server : channel-id=" + businessInfo.clientSSId + ", port=" + port);
        Log.d(TAG, "server ip=" + getServerIp(context));
        infoManager = new InfoManager();

        InfoManager.setAppContext(context);
        if(globalId != null) {
            infoManager.setId(globalId);
        }
        infoManager.setId(UUID.randomUUID().toString());
    }

    public static void setId(String id) {
        globalId = id;
    }

    public String getServerInfo() {
        return getServerIp(context) + ":" + port;
    }

    public String getServerAddress() {
        return getUrl();
    }

    public InfoManager getInfoManager() {
        return infoManager;
    }

    private int newServer() {
        int retryCount = 0;
        while(retryCount < 10) {
            try {
                port = newPort();
                server = new ConnectSocketServer(port);
                ServerConfig.savePort(businessInfo.clientSSId, port);
                Log.d(TAG, "server init in ...");
                break;
            } catch (Exception e) {
                retryCount++;
                Log.d(TAG, "new connect server error : " + e.toString() + ", retry count=" + (retryCount));
                ServerConfig.reset(businessInfo.clientSSId);
            }
        }
        return port;
    }

    public void setTestPort(int port) {
        this.testPort = port;
    }

    protected int newPort() {
        if(testPort != 0)
            return testPort;
        return ServerConfig.newPort();
    }

    public int getClientSize() {
        return server == null ? 0 : server.getClientSize();
    }

    public void start() {
        if(bStart) {
            Log.d(TAG, "already start.");
            return ;
        }
        bStart = true;
        server.start();
    }

    public void stop() {
        bStart = false;
//        server.stop();
    }

    public void setCallback(IServerCallback callback) {
        server.setCallback(callback);
    }

    public void onClientStart(IMMessage message) {
        Log.d(TAG, "onClientStart, message=" + message);
        String content = message.getContent();
        ClientCmdInfo clientCmdInfo = JSON.parseObject(content, ClientCmdInfo.class);
        onClientStart(clientCmdInfo, message.getClientSource());
    }

    public void onClientStart(ClientCmdInfo clientCmdInfo, String clientSource) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.url = getUrl();
        Log.d(TAG, "receive client start, reply server info url=" + serverInfo.url);
        ServerCmdInfo sInfo = new ServerCmdInfo();
        sInfo.cmd = ServerCmdInfo.CMD_SERVER_RECEIVE_CONNECT;
        sInfo.cId = clientCmdInfo.cid;
        sInfo.sId = infoManager.getId();
        sInfo.content = JSON.toJSONString(serverInfo);
        try {
            msgSender.sendMsgSticky(JSON.toJSONString(sInfo), clientSource);
        } catch (DeadObjectException e) {
            //channel service died.
            if(msgSender instanceof ChannelMsgSender) {
                ((ChannelMsgSender) msgSender).open();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //增加心跳检测
        server.onClientTryConnect(clientCmdInfo.cid);
    }

    public void broadcast(String text) {
        server.broadcast(text);
    }

    public void broadcastTo(String text, String cid) {
        server.broadcastTo(text, cid);
    }

    /**
     * 发送给指定手机端，排除单个端
     * @param text
     */
    public void broadcastExclude(String text, String cid) {
        server.broadcastExclude(text, cid);
    }

    /**
     * 发送给指定手机端
     * @param text
     * @param clients
     */
    public void broadcastTo(String text, Collection<String> clients) {
        server.broadcastTo(text, clients);
    }

    public String getSid() {
        return infoManager.getId();
    }

    private String getUrl() {
        return Uri.decode(new Uri.Builder().scheme("http").authority(getServerIp(context) + ":" + ServerConfig.getPort(businessInfo.clientSSId)).build().toString());
    }

    private String getServerIp(Context context) {
        return NetUtils.getIP(context);
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        int ipAddress = wifiInfo.getIpAddress();
//        //  Log.d(Tag, "int ip "+ipAddress);
//        if (ipAddress == 0) return null;
//        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
//                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

}
