package swaiotos.sensor.server;

import android.os.SystemClock;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: yuzhan
 */
public class ServerConfig {
    private static Map<String, Integer> portMap = new ConcurrentHashMap<>();
    private static int PORT_FROM = 30010;
    private static int PORT_LIMIT = 55500;
    private static int PORT_END = 55500;

    private static int curPort = 0;

    /**
     * range in [30010, 65000]
     * @param start
     */
    public static void setPortStart(int start) {
        if(start > 30010 && start < 65000) {
            PORT_FROM = start;
            PORT_LIMIT = start + 1000;
            PORT_END = PORT_LIMIT;
        }
    }

    public static int newPort() {
        if(curPort > 0 && curPort < 65500) {
            return curPort + new Random().nextInt(30) + 1;
        }
        return (PORT_FROM + new Random().nextInt((int) SystemClock.uptimeMillis() % (PORT_END-PORT_FROM))) % PORT_LIMIT;//随机端口
    }

    public static void savePort(String id, int port) {
        portMap.put(id, port);
        curPort = port;
    }

    public static int curPort() {
        return curPort;
    }

    public static int getPort(String clientId) {
        //putIfAbsent
        return portMap.get(clientId);
    }

    public static void reset(String clientId) {
        portMap.remove(clientId);
    }
}
