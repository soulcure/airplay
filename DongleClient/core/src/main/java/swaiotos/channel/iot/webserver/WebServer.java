package swaiotos.channel.iot.webserver;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import swaiotos.channel.iot.config.PortConfig;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.DeviceUtil;

public class WebServer {
    private static final String TAG = "WebServer";

    private int webPort;
    private Context mContext;
    private FileServer mFileServer;
    private final Map<String, File> files = new LinkedHashMap<>();

    public WebServer(Context context) {
        mContext = context;
        webPort = PortConfig.FILE_SERVER_PORT;
    }

    public void open() {
        mFileServer = new FileServer(webPort) {
            @Override
            protected File link(String uri) {
                if (uri.length() >= 1) {
                    String key = uri.substring(1);
                    Log.d(TAG, "link key:" + key);
                    synchronized (files) {
                        if (files.containsKey(key)) {
                            return files.get(key);
                        }
                    }
                }
                return null;
            }
        };
        mFileServer.startServer();
        Log.d(TAG, "WebServer open...");
    }

    public String uploadFile(File file) {
        String key = AppUtils.md5(file);  //文件md5命名
        //String key = file.getName();

        synchronized (files) {
            if (!files.containsKey(key)) {
                files.put(key, file);
            }
        }
        String path = getHost() + File.separator + key;

        Log.d(TAG, "WebServer path=" + path);
        return path;
    }


    public synchronized boolean available() {
        return mFileServer != null && mFileServer.isAlive();
    }


    public String getAddress() {
        return getHost();
    }

    public synchronized void close() throws IOException {
        performClose();
    }

    private void performClose() {
        if (mFileServer != null) {
            mFileServer.stop();
            mFileServer = null;
        }
    }


    private String getHost() {
        String ip = DeviceUtil.getLocalIPAddress(mContext);
        return "http://" + ip + ":" + webPort;
    }


}
