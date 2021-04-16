package swaiotos.channel.iot.ss.webserver;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import swaiotos.channel.iot.ss.config.PortConfig;
import swaiotos.channel.iot.utils.DeviceUtil;
import swaiotos.channel.iot.utils.F;

/**
 * @ClassName: WebServer
 * @Author: lu
 * @CreateDate: 2020/3/23 10:31 AM
 * @Description:
 */
public class WebServerImpl implements WebServer {
    private static final String TAG = "WebServer";

    private int webPort;
    private Context mContext;
    private FileServer mFileServer;
    private final Map<String, File> files = new LinkedHashMap<>();

    public WebServerImpl(Context context) {
        mContext = context;
        webPort = PortConfig.getWebServerPort(context.getPackageName());
    }

    @Override
    public synchronized String open() throws IOException {
        performOpen();
        return getAddress();
    }

    @Override
    public String uploadFile(File file) throws IOException {
        //String key = F.md5(file);//文件md5名命名
        String key = F.md5(file.getAbsolutePath());//文件全路径字符串md5名命名
        synchronized (files) {
            files.put(key, file);
        }
        String path = getHost() + File.separator + key;

        Log.d(TAG, "WebServer path=" + path);
        return path;
    }


    private void performOpen() throws IOException {
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

    @Override
    public synchronized boolean available() {
        return mFileServer != null && mFileServer.isAlive();
    }


    @Override
    public String getAddress() {
        return getHost();
    }

    @Override
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
