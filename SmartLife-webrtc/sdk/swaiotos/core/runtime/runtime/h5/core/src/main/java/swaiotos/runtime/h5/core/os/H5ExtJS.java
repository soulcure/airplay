package swaiotos.runtime.h5.core.os;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import swaiotos.runtime.base.AppletThread;

/**
 * @Author: yuzhan
 */
public class H5ExtJS {
    private String url;
    private volatile String extJs;
    private CountDownLatch latch = new CountDownLatch(1);
    private Object lock = new Object();
    private boolean isLoading = false;
    private static final String TAG = "CCApplet";

    private H5ExtJS(String url) {
        this.url = url;
    }

    public static H5ExtJS tryGetInstance(String url) {
        if(isValidUrl(url)) {
            Log.d(TAG, "valid extJs url= " + url);
            return new H5ExtJS(url);
        }
        return null;
    }

    public String getExtJsUrl() {
        return url;
    }

    public String getExtJsContent() {
        if(TextUtils.isEmpty(extJs)) {
            AppletThread.execute(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if(TextUtils.isEmpty(extJs) && !isLoading) {
                            String jsHttpContent = loadExtJsSync();
                            StringBuilder sb = new StringBuilder("javascript:(function() {");
                            sb.append(jsHttpContent);
                            sb.append("})()");
                            extJs = sb.toString();
                            Log.d(TAG, "extJs=" + extJs);
                            latch.countDown();
                        }
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return extJs;
        }
        return extJs;
    }

    public static boolean isValidUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            return uri != null && !TextUtils.isEmpty(uri.getScheme()) && !TextUtils.isEmpty(uri.getHost()) && !TextUtils.isEmpty(uri.getPath());
        } catch (Exception e) {

        }
        return false;
    }

    private String loadExtJsSync() {
        String ret = null;
        isLoading = true;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(this.url);
            connection = (HttpURLConnection) url.openConnection();
            //设置请求方法
            connection.setRequestMethod("GET");
            //设置连接超时时间（毫秒）
            connection.setConnectTimeout(5000);
            //设置读取超时时间（毫秒）
            connection.setReadTimeout(5000);
            //返回输入流
            InputStream in = connection.getInputStream();

            //读取输入流
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            ret = result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {

                }
            }
            if (connection != null) {//关闭连接
                try {
                    connection.disconnect();
                } catch (Exception e) {

                }
            }
        }
        isLoading = false;
        return ret;
    }
}
