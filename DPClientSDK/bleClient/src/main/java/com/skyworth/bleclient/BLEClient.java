package com.skyworth.bleclient;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author chenaojun
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BLEClient extends BlePduUtil {

    private static final String TAG = "bleClient";

    //配网UUID前缀
    private static final String NET_UUID_PREFIX = "ffffff01";
    //配网模式后缀
    private static final String NET_UUID_SUFFIX = "111111111111";


    //重新连接
    private static final int RECONNECT_BLE_SERVER = 100;

    private BluetoothAdapter mBluetoothAdapter;

    private Handler mProcessHandler;  //子线程Handler
    private UIHandler mHandler;

    private BluetoothClientCallback mBluetoothClientCallback;
    private BluetoothGatt mBluetoothGatt;
    private Context mContext;
    private String mTargetUuid = null;


    //tcp 连接状态
    private enum BLE_STATUS {
        IDLE, CONNECTING, CONNECTED
    }

    private BLE_STATUS bleStatus = BLE_STATUS.IDLE;


    private boolean isWriteSuccess = false;

    private ByteBuffer mByteBuffer = ByteBuffer.allocate(1024 * 100);

    /**
     * MTU(Maximum Transmission Unit)最大传输单元:指在一个协议数据单元中(PDU,Protocol Data Unit)有效的最大传输Byte
     * AndroidMTU一般为23，发送长包需要更改MTU(5.1(API21)开始支持MTU修改)或者分包发送
     * core spec里面定义了ATT的默认MTU为23bytes，除去ATT的opcode一个字节以及ATT的handle2个字节后，剩余20个bytes留给GATT
     * MTU是不可以协商的，只是通知对方，双方在知道对方的极限后会选择一个较小的值作为以后的MTU
     */
    private int mMtu = 500;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            BluetoothDevice device = gatt.getDevice();
            if (device == null) {
                return;
            }

            Log.d(TAG, "onConnectionStateChange: status---" + status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                //updateState(BluetoothClientCallback.DeviceState.FAILED); //蓝牙连接失败 不通知上层 立即发起重连
                Log.d(TAG, "BLE客户端:BLE连接失败");
                bleStatus = BLE_STATUS.IDLE;
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                reConnect();
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "BLE客户端:BLE连接成功，开始搜索外围服务");
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e(TAG, "BLE客户端:断开BLE连接");
                bleStatus = BLE_STATUS.IDLE;
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                updateState(BluetoothClientCallback.DeviceState.DISCONNECT);
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.e(TAG, "BLE客户端:BLE正在连接...");
                bleStatus = BLE_STATUS.CONNECTING;
                updateState(BluetoothClientCallback.DeviceState.CONNECTING);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "BLE客户端:成功搜索到服务");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    gatt.requestMtu(mMtu);
                }
                BluetoothGattService service = gatt.getService(UUID.fromString(mTargetUuid));
                if (service != null) {
                    Log.d(TAG, "setCharacteristicNotification: ");
                    mBluetoothGatt.setCharacteristicNotification(service.getCharacteristic(UUID.fromString(mTargetUuid)), true);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "BLE客户端:成功读取到特性");

                updateState(BluetoothClientCallback.DeviceState.CONNECTED);
                bleStatus = BLE_STATUS.CONNECTED;

                stopScan();
            } else {
                Log.e(TAG, "BLE客户端:onCharacteristicRead status=" + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isWriteSuccess = true;
                Log.d(TAG, "BLE客户端:写特征值成功");
            } else {
                Log.e(TAG, "BLE客户端:onCharacteristicWrite status=" + status);
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
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
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            Log.d(TAG, "BLE客户端:onDescriptorRead  status=" + status);
            Log.d(TAG, "BLE客户端:成功读取descriptor:" + new String(descriptor.getValue()));
            Log.d(TAG, "BLE length descriptor:" + descriptor.getValue().length);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "BLE客户端:onDescriptorWrite  status=" + status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "BLE客户端: onMTUChanged. mtu=" + mtu + " status=" + status);

            mMtu = mtu;
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "BLE客户端: onMTUChanged status不为0");
            }
            BluetoothGattService service = mBluetoothGatt.getService(UUID.fromString(mTargetUuid));
            if (service != null) {
                boolean b = mBluetoothGatt.readCharacteristic(service.getCharacteristic(UUID.fromString(mTargetUuid)));
                Log.d(TAG, "readCharacteristic: " + b);
            }
        }

    };

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getScanRecord() == null || result.getScanRecord() == null) return;
            BluetoothDevice device = result.getDevice();

            List<ParcelUuid> ids = result.getScanRecord().getServiceUuids();
            if (ids != null) {
                for (ParcelUuid parcelUuid : ids) {
                    Log.d(TAG, "Scan Device:" + parcelUuid.toString() + "  mTargetMac:" + mTargetUuid);
                    if (mTargetUuid.equals(parcelUuid.toString())) {
                        connectDevice(device);
                        break;
                    }
                }
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.d(TAG, "onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed...");
            updateState(BluetoothClientCallback.DeviceState.FAILED);
            bleStatus = BLE_STATUS.IDLE;
        }

    };


    public BLEClient(Context context, BluetoothClientCallback callback,
                     Handler processHandler) {
        mContext = context;
        mHandler = new UIHandler(this);
        mProcessHandler = processHandler;

        mBluetoothClientCallback = callback;
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


    }

    public void startScan(String mac) {
        bleStatus = BLE_STATUS.IDLE;

        if (!isSupport()) {
            return;
        }

        if (mac != null && mac.length() == 12) {
            mac = mac.toLowerCase();
            mTargetUuid = NET_UUID_PREFIX + "-" + mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8) + "-" + NET_UUID_SUFFIX;
            mHandler.removeMessages(RECONNECT_BLE_SERVER);

            mProcessHandler.post(new Runnable() {
                @Override
                public void run() {
                    while (mBluetoothAdapter.getBluetoothLeScanner() == null) {
                        try {
                            Log.d(TAG, "wait Bluetooth open");
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, "startScan(mac) target mac:" + mTargetUuid);
                    //mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);

                    List<ScanFilter> filters = new ArrayList<>();
                    ScanFilter.Builder builder = new ScanFilter.Builder();
                    builder.setServiceUuid(ParcelUuid.fromString(mTargetUuid));
                    ScanFilter filter = builder.build();
                    filters.add(filter);
                    ScanSettings scanSettings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();

                    mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, scanSettings, scanCallback);

                    updateState(BluetoothClientCallback.DeviceState.SCANING);

                }
            });
        } else {
            Log.e(TAG, "startScan deviceName error");
        }


    }

    public void startScan() {
        bleStatus = BLE_STATUS.IDLE;

        if (!isSupport()) {
            return;
        }

        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                while (mBluetoothAdapter.getBluetoothLeScanner() == null) {
                    try {
                        Log.d(TAG, "wait Bluetooth open");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "startScan() target mac:" + mTargetUuid);

                List<ScanFilter> filters = new ArrayList<>();
                ScanFilter.Builder builder = new ScanFilter.Builder();
                builder.setServiceUuid(ParcelUuid.fromString(mTargetUuid));
                ScanFilter filter = builder.build();
                filters.add(filter);

                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();

                mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, scanSettings, scanCallback);

                updateState(BluetoothClientCallback.DeviceState.SCANING);
            }
        });


    }


    public void setTargetMac(String mac) {
        bleStatus = BLE_STATUS.IDLE;
        if (TextUtils.isEmpty(mac)) {
            mTargetUuid = null;
        } else {
            mac = mac.toLowerCase();
            mTargetUuid = NET_UUID_PREFIX + "-" + mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8) + "-" + NET_UUID_SUFFIX;
        }
    }


    public void close() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Log.d(TAG, "Bluetooth Disable");
        }

    }

    /**
     * 使用子线程发送数据
     *
     * @param msg
     */
    public void sendMsg(final String msg, final byte type) {
        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                sendMsgByThread(msg, type);
            }
        });
    }

    private void sendMsgByThread(final String msg, final byte type) {
        if (mBluetoothGatt == null || mTargetUuid == null) {
            Log.d(TAG, "sendMsgByThread error!!!");
            return;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(mTargetUuid));
        if (gattService != null) {

            final BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UUID.fromString(mTargetUuid));
            byte[] data = msg.getBytes();
            Log.d(TAG, "sendMsg data size---" + data.length);
            boolean result = false;

            BlePdu pdu = new BlePdu();
            pdu.pduType = type;
            pdu.length = (short) data.length;
            pdu.body = data;

            byte[] src = pdu.build();
            int limit = mMtu - 10;
            int totalLen = src.length;
            if (totalLen > mMtu) {
                int count = totalLen / limit + 1;
                for (int i = 0; i < count; i++) {
                    int offset = i * limit;
                    int length;
                    if (i < count - 1) {
                        length = limit;
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    } else {
                        length = totalLen - (i * limit);
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                    }
                    byte[] dst = new byte[length];
                    System.arraycopy(src, offset, dst, 0, length);

                    Log.e("yao", "bel client subpackage send data size---" + dst.length + " dst:" + new String(dst));
                    gattCharacteristic.setValue(dst);

                    //为characteristic赋值
                    result = mBluetoothGatt.writeCharacteristic(gattCharacteristic);


                    while (!isWriteSuccess) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    isWriteSuccess = false;
                }
            } else {
                Log.e("yao", "bel client send data size---" + src.length);
                gattCharacteristic.setValue(src);
                //为characteristic赋值
                result = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
            }

            Log.d(TAG, "sendMsg run: writeCharacteristic---" + result);
        }
    }


    public boolean isConnected(String mac) {
        if (TextUtils.isEmpty(mac)) {
            return false;
        }

        mac = mac.toLowerCase();
        String targetMac = NET_UUID_PREFIX + "-" + mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8) + "-" + NET_UUID_SUFFIX;
        if (targetMac.equals(mTargetUuid) && bleStatus == BLE_STATUS.CONNECTED) {
            return true;
        }
        return false;
    }


    public void disConnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        stopScan();
    }

    public void stopScan() {
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
        Log.d(TAG, "ble client stopScan:" + bluetoothLeScanner);

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

    private void reConnect() {
        stopScan();
        if (!mHandler.hasMessages(RECONNECT_BLE_SERVER)) {
            Log.d(TAG, "ble client reConnect---");
            mHandler.sendEmptyMessageDelayed(RECONNECT_BLE_SERVER, 1000);
        }
    }


    public boolean isSupport() {
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "ble service init failed ble service get failed!!!");
            updateState(BluetoothClientCallback.DeviceState.NOTSUPPORT);
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth disable");
            updateState(BluetoothClientCallback.DeviceState.BLE_DISABLE);
            return false;
        }

        if (Build.VERSION.SDK_INT >= 29) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限。
                Log.e(TAG, "Bluetooth no permission location");
                updateState(BluetoothClientCallback.DeviceState.LOCATION_PERMISSION);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限。
                Log.e(TAG, "Bluetooth no permission location");
                updateState(BluetoothClientCallback.DeviceState.LOCATION_PERMISSION);
                return false;
            }
        }
        return true;
    }


    private String initMac(String uuid) {
        return uuid.replace("-", "").substring(8, 20).toUpperCase();
    }

    private void connectDevice(BluetoothDevice device) {
        if (bleStatus == BLE_STATUS.IDLE) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                // 我们想要直接连接到设备，所以我们设置了自动连接
                // parameter to false.
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
            } else {
                // Daniel:使用蓝牙设备。TRANSPORT_LE强制使用BLE传输层，但最小API层为23。
                mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
            bleStatus = BLE_STATUS.CONNECTING;
            updateState(BluetoothClientCallback.DeviceState.CONNECTING);
        }
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

    private static class UIHandler extends Handler {
        WeakReference<BLEClient> weakReference;

        public UIHandler(BLEClient bleClient) {
            super(Looper.getMainLooper());
            weakReference = new WeakReference<>(bleClient);
        }

        @Override
        public void handleMessage(Message msg) {
            BLEClient bleClient = weakReference.get();
            switch (msg.what) {
                case RECONNECT_BLE_SERVER:
                    bleClient.startScan();
                    break;
            }
        }
    }
}