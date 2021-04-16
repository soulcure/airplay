package swaiotos.channel.iot.ss.channel.base.local;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.skyworth.dpclientsdk.ble.BlePdu;
import com.skyworth.dpclientsdk.ble.BluetoothServer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.client.event.BindCodeEvent;

public class WifiResult {
    private static final String TAG = "WifiResult";

    private static final String ACTION_BLE_RECEIVE_WIFI_PW = "channel.iot.action.ble.receive.wifi.pw.success"; //蓝牙接收wifi账号密码成功
    private static final String ACTION_SET_WIFI_START = "channel.iot.action.set.wifi.start";//连接指定wifi开始
    private static final String ACTION_SET_WIFI_SUCCESS = "channel.iot.action.set.wifi.success";//连接指定wifi成功
    private static final String ACTION_SET_WIFI_FAIL = "channel.iot.action.set.wifi.fail";//连接指定wifi失败（包括超时）


    public interface WifiConfigCallBack {
        void onFinish();
    }

    private final LinkedBlockingQueue<ConnectResult> mSendQueue;
    private Context mContext;
    private SSContext mSsContext;
    private BluetoothServer mBleServer;
    private BluetoothDevice mBleDevice;
    private String mSsid;   //wifi name
    private String mSid;   //tv 智屏id

    private int errCode;
    private WifiSendThread mWifiSendThread;
    private boolean isSendBindCode = false;
    private WifiConfigCallBack wifiConfigCallBack;


    private static class ConnectResult {
        int status;
    }


    public WifiResult(Context context, SSContext ssContext, BluetoothServer bleServer,
                      BluetoothDevice bleDevice, String ssid, String sid,
                      WifiConfigCallBack callback) {
        EventBus.getDefault().register(this);
        mSendQueue = new LinkedBlockingQueue<>();
        mContext = context;
        mSsContext = ssContext;
        mBleServer = bleServer;
        mBleDevice = bleDevice;
        mSsid = ssid;
        mSid = sid;
        wifiConfigCallBack = callback;
        Log.d(TAG, "WifiResult tv sid----" + sid);
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEvent(final BindCodeEvent event) {
        try {
            String bindCode = event.bindCode;
            Log.e(TAG, "get ---bindCode:" + bindCode);
            if (TextUtils.isEmpty(bindCode))
                return;
            Log.e(TAG, "get ---bindCode111:" + bindCode);

            if (!isSendBindCode) {
                isSendBindCode = true;
                sendBindCode(bindCode);
            }

            EventBus.getDefault().unregister(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addWifiResult(int status) {
        Log.e(TAG, "addWifiResult status=" + status);
        synchronized (this) {
            ConnectResult result = new ConnectResult();
            result.status = status;
            if (mWifiSendThread != null) {
                mWifiSendThread.send(result);
            }
        }
    }


    public void setErrCode(int code) {
        errCode = code;
    }

    public void sendWifiResult() {
        Log.e(TAG, "sendWifiResult start...");
        mWifiSendThread = new WifiSendThread();
        mWifiSendThread.clear();
        mWifiSendThread.start();

        Intent in = new Intent();
        in.setAction(ACTION_BLE_RECEIVE_WIFI_PW);
        in.putExtra("ssid", mSsid);
        in.putExtra("errCode", errCode);
        mContext.sendBroadcast(in);
    }

    /**
     * socket 发送线程类
     */
    private class WifiSendThread implements Runnable {
        private boolean isExit = false;  //是否退出
        private ConnectResult result;
        private ConnectResult mLast = null;
        private boolean isProgress = false;
        private boolean isSuccess = false;

        /**
         * 发送线程开启
         */
        public void start() {
            Thread thread = new Thread(this);
            thread.setName("wifiSend-thread");
            thread.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(8000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "sendWifiResult time out run...");

                    if (!isProgress && !isSuccess && mLast != null) {
                        sendProto(mLast);
                        Log.e(TAG, "sendWifiResult time out send mLast" + mLast.status);

                        if (wifiConfigCallBack != null) {
                            wifiConfigCallBack.onFinish();
                            wifiConfigCallBack = null;
                        }
                    }
                }
            }).start();

        }

        public void send(ConnectResult connectResult) {
            synchronized (this) {
                if (connectResult != null) {
                    mSendQueue.offer(connectResult);
                    notify();
                }
            }

        }

        /**
         * 发送线程关闭
         */
        public void close() {
            synchronized (this) { // 激活线程
                isExit = true;
                notify();
            }
        }


        public void clear() {
            mSendQueue.clear();
            synchronized (this) { // 激活线程
                isExit = false;
                notify();
            }
            mLast = null;
            isProgress = false;
            isSuccess = false;
            isSendBindCode = false;
            Log.e(TAG, "sendWifiResult clear...");
        }


        @Override
        public void run() {
            while (!isExit) {
                Log.v(TAG, "tcpSend-thread is running");

                synchronized (mSendQueue) {
                    result = mSendQueue.poll();
                    if (result == null) {
                        synchronized (this) {
                            try {
                                wait();// 发送完消息后，线程进入等待状态
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.e(TAG, "tcp mSendQueue error---" + e.getMessage());
                            }
                        }
                        continue;
                    }

                    mLast = result;
                    Log.e(TAG, "sendWifiResult result=" + result.status);

                    if (result.status == 1) {
                        isProgress = true;
                    }

                    if (result.status == 2) {
                        isSuccess = true;
                    }

                    if (isProgress && !isSuccess) {
                        boolean isExit = sendProto(result);

                        if (isExit) {
                            break;
                        }
                    } else if (isProgress && isSuccess) {
                        sendProto(result);  //发送成功消息
                        clear();
                        break;
                    }

                }

            }


            if (wifiConfigCallBack != null) {
                wifiConfigCallBack.onFinish();
                wifiConfigCallBack = null;
            }
            Log.e(TAG, "tcpSend-thread is exit");

        }//#run

    }//#

    private boolean sendProto(ConnectResult result) {
        boolean res = false;
        JSONObject jo = new JSONObject();
        try {
            jo.put("code", 0);

            if (result.status == -1) {
                jo.put("msg", "set wifi connect fail");
                res = true;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jo.put("errCode", errCode);

            } else if (result.status == 1) {
                jo.put("msg", "set wifi connecting");
                res = false;
            } else if (result.status == 2) {
                jo.put("msg", "set wifi success");
                jo.put("sid", mSid);
                res = true;
            }

            jo.put("proto", "ConfigureWiFi");
            jo.put("ssid", mSsid);
            jo.put("status", result.status);

            String json = jo.toString();

            Log.e(TAG, "BleServer config wifi proto---" + json);
            byte[] data = json.getBytes();

            ByteBuffer byteBuffer = ByteBuffer.allocate(BlePdu.PDU_HEADER_LENGTH + data.length);
            byteBuffer.put(BlePdu.pduStartFlag);
            byteBuffer.put(BlePdu.TEMP_PROTO);
            byteBuffer.putShort((short) data.length);
            byteBuffer.put(data);

            mBleServer.sendMessage(byteBuffer, mBleDevice);


            Intent in = new Intent();
            if (result.status == -1) {
                in.setAction(ACTION_SET_WIFI_FAIL);
            } else if (result.status == 1) {
                in.setAction(ACTION_SET_WIFI_START);
            } else if (result.status == 2) {
                in.setAction(ACTION_SET_WIFI_SUCCESS);
            }
            in.putExtra("ssid", mSsid);
            //WifiManager.ERROR_AUTH_FAILURE_NONE WifiManager.ERROR_AUTHENTICATING  WifiManager.ERROR_AUTH_FAILURE_TIMEOUT
            in.putExtra("errCode", errCode);
            mContext.sendBroadcast(in);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;


    }


    public void sendBindCode(String bindCode) {

        JSONObject jo = new JSONObject();
        try {
            jo.put("code", 0);
            jo.put("msg", "set bindCode success");
            jo.put("status", 2);
            jo.put("bindCode", bindCode);
            jo.put("proto", "ConfigureWiFi");
            String json = jo.toString();

            Log.e(TAG, "BleServer config wifi proto---" + json);
            byte[] data = json.getBytes();

            ByteBuffer byteBuffer = ByteBuffer.allocate(BlePdu.PDU_HEADER_LENGTH + data.length);
            byteBuffer.put(BlePdu.pduStartFlag);
            byteBuffer.put(BlePdu.TEMP_PROTO);
            byteBuffer.putShort((short) data.length);
            byteBuffer.put(data);

            mBleServer.sendMessage(byteBuffer, mBleDevice);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

