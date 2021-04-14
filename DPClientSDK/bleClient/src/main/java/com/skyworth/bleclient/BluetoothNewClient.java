package com.skyworth.bleclient;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.BleLog;
import cn.com.heaton.blelibrary.ble.BleStates;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.ScanRecord;
import cn.com.heaton.blelibrary.ble.utils.UuidUtils;

/**
 * @author chenaojun
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BluetoothNewClient extends BlePduUtil {

    private static final String TAG = "bleClient";

    //配网UUID前缀
    private static final String NET_UUID_PREFIX = "FFFFFF01";
    //配网模式后缀
    private static final String NET_UUID_SUFFIX = "111111111111";

    //重新连接
    private static final int RECONNECT_BLE_SERVER = 100;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mProcessHandler;  //子线程Handler
    private UIHandler mHandler;

    private BluetoothClientCallback mBluetoothClientCallback;
    private Context mContext;


    //tcp 连接状态
    private enum BLE_STATUS {
        IDLE, CONNECTING, CONNECTED
    }

    private BLE_STATUS bleStatus = BLE_STATUS.IDLE;

    private ByteBuffer mByteBuffer = ByteBuffer.allocate(1024 * 100);

    private UUID serviceUUID;
    private UUID characteristicUUID;

    private BleDevice mBleDevice;

    private String bleMac;
    private List<BleDevice> bleDevices;
    private String mTargetMac;
    private boolean isScan;
    private BleDevice ConnectingBleDevice;
    private boolean isBleSuccess;   //BLE库初始化是否成功
    private boolean isBleRange;  //是否开启蓝牙围栏
    /**
     * MTU(Maximum Transmission Unit)最大传输单元:指在一个协议数据单元中(PDU,Protocol Data Unit)有效的最大传输Byte
     * AndroidMTU一般为23，发送长包需要更改MTU(5.1(API21)开始支持MTU修改)或者分包发送
     * core spec里面定义了ATT的默认MTU为23bytes，除去ATT的opcode一个字节以及ATT的handle2个字节后，剩余20个bytes留给GATT
     * MTU是不可以协商的，只是通知对方，双方在知道对方的极限后会选择一个较小的值作为以后的MTU
     */
    private int mMtu = 512;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BluetoothNewClient(Context context, BluetoothClientCallback callback, Handler processHandler, boolean openBleRange) {
        mContext = context;
        bleDevices = new ArrayList<>();
        mHandler = new UIHandler(this);
        mProcessHandler = processHandler;
        mBluetoothClientCallback = callback;
        isBleRange = openBleRange;

        if (isSupport()) {
            return;
        }

        initBle();
    }


    private BleScanCallback<BleDevice> bleScanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            String bleName = device.getBleName();
            String mac = device.getBleAddress().replace(":", "");

            if (bleMac.equalsIgnoreCase(mac)) {
                BleLog.e(TAG, "BleScan deviceName111---" + bleName + "&mac---" + mac + "bleMac:"+bleMac + "  ---:"+mac.equalsIgnoreCase(bleMac));
            }

            if (!isScan) {
                isScan = true;
            }

            if (isBleRange && !TextUtils.isEmpty(bleName) && bleName.equals("HDD500")) {
                long dec = BluetoothNewClient.hex2decimal(mac);
                dec--;
                String deviceMac = BluetoothNewClient.decimal2Hex(dec).toUpperCase();

                mBluetoothClientCallback.onScanResult(deviceMac);
//                Log.d(TAG, "find CooCaa Ble Device:" + mac.toUpperCase());
            }

            synchronized (Ble.getInstance().getLocker()) {
                for (int i = 0; i < bleDevices.size(); i++) {
                    BleDevice rssiDevice = bleDevices.get(i);
                    if (TextUtils.equals(rssiDevice.getBleAddress(), device.getBleAddress())) {
                        return;
                    }
                }
//                BleLog.e(TAG, "BleScan add deviceName---");
                device.setScanRecord(ScanRecord.parseFromBytes(scanRecord));
                bleDevices.add(device);

                BleLog.e(TAG, "BleScan deviceName---" + bleName + "&mac---" + mac + "bleMac:"+bleMac + "  ---:"+mac.equalsIgnoreCase(bleMac));
                if (!TextUtils.isEmpty(mac) && mac.equalsIgnoreCase(bleMac)) {
                    BleLog.e(TAG, "BleScan connect----");
                    Ble.getInstance().connect(device.getBleAddress(), connectCallback);
                    ConnectingBleDevice = device;
                }
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            updateState(BluetoothClientCallback.DeviceState.SCANING);
            bleStatus = BLE_STATUS.CONNECTING;
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed: " + errorCode);

        }
    };

    private BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            Log.e(TAG, "onConnectionChanged: " + device.getConnectionState());
        }

        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            Log.e(TAG, "onConnectException: 连接异常，异常状态码:" + errorCode);
//            updateState(BluetoothClientCallback.DeviceState.FAILED);
//            bleStatus = BLE_STATUS.IDLE;
            Ble.getInstance().connect(device.getBleAddress(), connectCallback);
        }

        @Override
        public void onConnectTimeOut(BleDevice device) {
            super.onConnectTimeOut(device);
            Log.e(TAG, "onConnectTimeOut: " + device.getBleAddress());
            updateState(BluetoothClientCallback.DeviceState.FAILED);
            bleStatus = BLE_STATUS.IDLE;
        }

        @Override
        public void onConnectCancel(BleDevice device) {
            super.onConnectCancel(device);
            Log.e(TAG, "onConnectCancel: " + device.getBleName());
            updateState(BluetoothClientCallback.DeviceState.DISCONNECT);
            bleStatus = BLE_STATUS.IDLE;
        }

        @Override
        public void onServicesDiscovered(BleDevice device, BluetoothGatt gatt) {
            super.onServicesDiscovered(device, gatt);
            Log.e(TAG, "onServicesDiscovered---" + device.getBleName());

//            if (device.getConnectionState() == BleDevice.CONNECTED) {
//
//                Log.d(TAG, "BLE客户端:成功搜索到服务");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    gatt.requestMtu(mMtu);
//                }
//
//                BluetoothGattService service = gatt.getService(serviceUUID);
//                Log.d(TAG, "BLE客户端:成功搜索到服务---service:"+service + "  serviceUUID:"+serviceUUID.toString());
//
//                if (service != null) {
//                    Log.d(TAG, "setCharacteristicNotification: ");
//                    gatt.setCharacteristicNotification(service.getCharacteristic(characteristicUUID), true);
//                }
//            }
        }

        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            Log.d(TAG, "BLE客户端:成功读取到特性---------onReady----");
            if (device.getConnectionState() == BleDevice.CONNECTED) {
                Log.d(TAG, "BLE客户端:onReady");

                mBleDevice = device;
                updateState(BluetoothClientCallback.DeviceState.CONNECTED);
                bleStatus = BLE_STATUS.CONNECTED;
            }

            //连接成功后，设置通知
            Ble.getInstance().enableNotify(device, true, new BleNotifyCallback<BleDevice>() {
                @Override
                public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                    UUID uuid = characteristic.getUuid();
                    BleLog.e(TAG, "onChanged==uuid:" + uuid.toString());
                    byte[] newValue = characteristic.getValue();

                    Log.d(TAG, "BLE客户端:onCharacteristicChanged value size=" + newValue.length);
                    mByteBuffer.put(newValue);
                    mByteBuffer.flip();

                    int readResult = 0;
                    while ((readResult = parsePdu(mByteBuffer)) > 0) {
                        //loop parse
                        Log.d(TAG, "ble read length:" + readResult);
                    }
                }

                @Override
                public void onNotifySuccess(BleDevice device) {
                    super.onNotifySuccess(device);
                    BleLog.e(TAG, "onNotifySuccess: " + device.getBleName());
                }
            });
        }
    };


    public void startScan(final String wifiMac) {
        bleDevices.clear();
        setTargetMac(wifiMac);
        if (isSupport()) {
            mProcessHandler.post(new Runnable() {
                @Override
                public void run() {
                    while (!Ble.getInstance().isSupportBle(mContext) || !Ble.getInstance().isBleEnable()) {
                        try {
                            Log.d(TAG, "wait Bluetooth open");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!isBleSuccess) {
                        initBle();
                    }
                    BleLog.e(TAG, "startScan---" + wifiMac);
                    Ble.getInstance().startScan(bleScanCallback);
                }
            });
        } else {
            if (!isBleSuccess) {
                initBle();
            }
            BleLog.e(TAG, "startScan---" + wifiMac);
            Ble.getInstance().startScan(bleScanCallback);
        }
    }


    public void startScan() {
        bleDevices.clear();
        if (isSupport()) {
            mProcessHandler.post(new Runnable() {
                @Override
                public void run() {
                    while (!Ble.getInstance().isSupportBle(mContext) || !Ble.getInstance().isBleEnable()) {
                        try {
                            Log.d(TAG, "wait Bluetooth open");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!isBleSuccess) {
                        initBle();
                    }

                    if (isBleSuccess) {
                        BleLog.e(TAG, "startScan---");
                        Ble.getInstance().startScan(bleScanCallback);
                    }
                }
            });
        } else {

            if (!isBleSuccess) {
                initBle();
            }

            if (isBleSuccess) {
                BleLog.e(TAG, "startScan---");
                Ble.getInstance().startScan(bleScanCallback);
            }
        }
    }

    public void setTargetMac(String wifiMac) {
        if (TextUtils.isEmpty(wifiMac)) {
            bleMac = null;
        } else {
            wifiMac = wifiMac.toLowerCase();

            BleLog.e(TAG, "wifi mac: " + wifiMac);
            long dec = hex2decimal(wifiMac);
            dec++;

            bleMac = decimal2Hex(dec);
            BleLog.e(TAG, "ble mac: " + bleMac);

            mTargetMac = "-" + wifiMac.substring(0, 4) + "-" + wifiMac.substring(4, 8) + "-" + wifiMac.substring(8) + "-";

            serviceUUID = UUID.fromString(NET_UUID_PREFIX + mTargetMac + NET_UUID_SUFFIX);
            characteristicUUID = UUID.fromString(NET_UUID_PREFIX + mTargetMac + NET_UUID_SUFFIX);
        }

    }

    public void disConnect() {
        if (isSupport())
            return;

        if (!isBleSuccess) {
            initBle();
        }

        if (isBleSuccess) {
            bleDevices.clear();
            if (ConnectingBleDevice != null) {
                Ble.getInstance().cancelCallback(connectCallback);
                ConnectingBleDevice = null;
            }

            if (mBleDevice != null) {
                if (mBleDevice.isConnecting()) {
                    Ble.getInstance().cancelConnecting(mBleDevice);
                } else if (mBleDevice.isConnected()) {
                    Ble.getInstance().disconnect(mBleDevice);
                }
                mBleDevice =  null;
            }
        }
    }

    public void stopScan() {
        if (isSupport())
            return;

        if (!isBleSuccess) {
            initBle();
        }
        if (isBleSuccess) {
            isScan = false;
            Ble.getInstance().stopScan();
            Log.d(TAG, "ble client stopScan");
        }

    }

    public boolean isScan() {
        return isScan;
    }

    public boolean isConnected(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return false;
        }

        String deviceName = mac.toLowerCase();
        String targetMac = "-" + deviceName.substring(0, 4) + "-" + deviceName.substring(4, 8) + "-" + deviceName.substring(8) + "-";
        if (targetMac.equals(mTargetMac) && bleStatus == BluetoothNewClient.BLE_STATUS.CONNECTED) {
            return true;
        }
        return false;
    }


    /**
     * 使用子线程发送数据
     *
     * @param msg
     */
    public void sendMsg(final String msg, final byte type) {
        if (isSupport()) return;

        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                sendMsgByThread(msg, type);
            }
        });
    }

    private void sendMsgByThread(final String msg, final byte type) {

        byte[] data = msg.getBytes();
        Log.d(TAG, "sendMsg data size---" + data.length);

        BlePdu pdu = new BlePdu();
        pdu.pduType = type;
        pdu.length = (short) data.length;
        pdu.body = data;

        byte[] src = pdu.build();

        if (mBleDevice != null && mBleDevice.isConnected()) {

            Ble.getInstance().writeByUuid(mBleDevice, src, serviceUUID, characteristicUUID,
                    new BleWriteCallback<BleDevice>() {
                        @Override
                        public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {
                            BleLog.e(TAG, "写入特征成功: " + device.getBleName());
                        }

                        @Override
                        public void onWriteFailed(BleDevice device, int failedCode) {
                            super.onWriteFailed(device, failedCode);
                            BleLog.e(TAG, "写入特征失败: " + failedCode);
                        }
                    });
        }
    }


    private boolean isSupport() {
        if (!Ble.getInstance().isSupportBle(mContext) || !Ble.getInstance().isBleEnable()) {
            mBluetoothClientCallback.onStateChange(BluetoothClientCallback.DeviceState.NOTSUPPORT);
            return true;
        }
        return false;
    }


    @Override
    public void OnRec(final BlePdu blePdu) {
        Log.d(TAG, "ble onRec msg---" + new String(blePdu.body));
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //主线程回调数据
                mBluetoothClientCallback.onMessageShow(blePdu);
            }
        });
    }


    private void reConnect() {
        stopScan();
        if (!mHandler.hasMessages(RECONNECT_BLE_SERVER)) {
            Log.d(TAG, "ble client reConnect---");
            mHandler.sendEmptyMessageDelayed(RECONNECT_BLE_SERVER, 0);
        }
    }


    private void updateState(final BluetoothClientCallback.DeviceState state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothClientCallback != null) {
                    mBluetoothClientCallback.onStateChange(state);
                }
            }
        });
    }

    //初始化蓝牙
    private void initBle() {
        Ble.options()
                .setLogBleEnable(true)//设置是否输出打印蓝牙日志
                .setThrowBleException(true)//设置是否抛出蓝牙异常
                .setLogTAG("AndroidBLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(false)//设置是否自动连接
                .setIgnoreRepeat(false)//设置是否过滤扫描到的设备(已扫描到的不会再次扫描)
                .setConnectFailedRetryCount(3)//连接异常时（如蓝牙协议栈错误）,重新连接次数
                .setConnectTimeout(10 * 1000)//设置连接超时时长
                .setScanPeriod(24* 60 * 60 * 1000)//设置扫描时长
                .setMaxConnectNum(7)//最大连接数量
//                .setScanFilter(scanFilter)
                .setUuidService(UUID.fromString(UuidUtils.uuid16To128("fd00")))//设置主服务的uuid
                .setUuidWriteCha(UUID.fromString(UuidUtils.uuid16To128("fd01")))//设置可写特征的uuid
//                .setUuidReadCha(UUID.fromString(UuidUtils.uuid16To128("fd02")))//设置可读特征的uuid （选填）
                .setUuidNotifyCha(UUID.fromString("ffffff01-b002-47b9-9f49-111111111111"))//设置可通知特征的uuid （选填，库中默认已匹配可通知特征的uuid）
//                .setFactory(new BleFactory() {//实现自定义BleDevice时必须设置
//                    @Override
//                    public BleRssiDevice create(String address, String name) {
//                        return new BleRssiDevice(address, name);//自定义BleDevice的子类
//                    }
//                })
                .setBleWrapperCallback(new MyBleWrapperCallback())
                .create(mContext, new Ble.InitCallback() {
                    @Override
                    public void success() {
                        BleLog.e("bleClient", "初始化成功");
                        isBleSuccess = true;
                    }

                    @Override
                    public void failed(int failedCode) {
                        BleLog.e("bleClient", "初始化失败：" + failedCode);
                        if (BleStates.NotAvailable == failedCode || BleStates.NotSupportBLE == failedCode) {
                            mBluetoothClientCallback.onStateChange(BluetoothClientCallback.DeviceState.NOTSUPPORT);
                            isBleSuccess = false;
                        } else {
                            isBleSuccess = true;
                        }

                    }
                });
    }

    private static class UIHandler extends Handler {
        WeakReference<BluetoothNewClient> weakReference;

        public UIHandler(BluetoothNewClient bleClient) {
            super(Looper.getMainLooper());
            weakReference = new WeakReference<>(bleClient);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothNewClient bleClient = weakReference.get();
            switch (msg.what) {
                case RECONNECT_BLE_SERVER:
                    bleClient.startScan();
                    break;
            }
        }
    }


    /**
     * 16进制转10进制
     *
     * @param hex
     * @return
     */
    public static long hex2decimal(String hex) {
        return Long.parseLong(hex, 16);
    }


    /**
     * 10进制转16进制
     *
     * @param i
     * @return
     */
    public static String decimal2Hex(long i) {
        String s = Long.toHexString(i);
        return s;
    }
}