package com.skyworth.dpclientsdk.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.skyworth.dpclientsdk.MACUtils;
import com.skyworth.dpclientsdk.ProcessHandler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class BluetoothServer extends BlePduUtil {

    private static final String TAG = "bleServer";
    //配网UUID前缀
    private static final String NET_UUID_PREFIX = "ffffff01";
    //配网模式后缀
    private static final String NET_UUID_SUFFIX = "111111111111";

    private final Context mContext;

    private final BluetoothServerCallBack mBluetoothServerCallBack;
    //蓝牙设备管理类
    private final BluetoothManager mBluetoothManager;
    //蓝牙设配器
    private BluetoothAdapter mAdapter;
    private BluetoothLeAdvertiser mAdvertiser;
    //蓝牙Server
    private BluetoothGattServer mBluetoothGattServer;
    //蓝牙服务
    private BluetoothGattService mService;
    //蓝牙特征
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;

    private final Map<String, ByteBuffer> mByteReadBufferMap = new HashMap<>();
    private Object mWritingObj;
    private UUID mUuid;
    private int mMtu = 0;
    private final ProcessHandler mProcessHandler;  //子线程Handler
    private final Handler mHandler;  //主线程Handler


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "开启ble广播成功");
            mBluetoothServerCallBack.onStartSuccess(mUuid.toString());
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "开启ble广播失败:" + errorCode);
            mBluetoothServerCallBack.onStartFail("开启ble广播失败:" + errorCode);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BluetoothServer(Context context, BluetoothServerCallBack callBack) {
        mContext = context;
        mBluetoothServerCallBack = callBack;
        mProcessHandler = new ProcessHandler("ble-server", true);
        mHandler = new Handler(Looper.getMainLooper());
        mUuid = getUUID();

        //1.获取管理类
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        //判断设备是否支持蓝牙
        if (mBluetoothManager == null) {
            return;
        }
        //2.获取蓝牙适配器
        mAdapter = mBluetoothManager.getAdapter();

        @SuppressLint("HardwareIds")
        String bleMac = mAdapter.getAddress();
        Log.d(TAG, "蓝牙mac地址：" + bleMac);

        if (!mAdapter.isEnabled()) {
            mAdapter.enable();
        }
    }

    /**
     * 开启蓝牙并开启ble广播
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public synchronized void openBle() {
        //如果是调用close Ble来关闭蓝牙的，会将bluetoothAdapter,bluetoothReceiver置为null，需要重新赋值
        if (mAdapter == null) {
            mAdapter = mBluetoothManager.getAdapter();
        }
        if (!mAdapter.isEnabled()) {
            mAdapter.enable();
        }
        initNetUuid();
        if (Build.VERSION.SDK_INT >= 21 && mAdapter.isMultipleAdvertisementSupported()) {
            if (initService()) {
                //10.启动ble广播
                startAdvertise();
            } else {
                Log.e(TAG, "开启ble失败1");
            }
        } else {
            Log.e(TAG, "开启ble失败2");
            if (!mAdapter.isMultipleAdvertisementSupported()) {
                Log.e(TAG, "您的设备不支持蓝牙从模式");
            }
        }
    }

    /**
     * 开启ble广播
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAdvertise() {
        ParcelUuid parcelUuid = new ParcelUuid(mUuid);
        mAdvertiser = mAdapter.getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        //初始化广播包
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                //设置广播设备名称
                .setIncludeDeviceName(false)
                //设置发射功率级别
                .setIncludeTxPowerLevel(false)
                //设置广播的服务`UUID`
                .addServiceUuid(parcelUuid)
                .build();

        mAdvertiser.startAdvertising(settings, advertiseData, mAdvertiseCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean initService() {
        //3.获取服务
        mService = new BluetoothGattService(mUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //4.获取一个特征
        // 注意，如果要使特征值可写，PROPERTY需设置PROPERTY_WRITE（服务端需要返回响应）或PROPERTY_WRITE_NO_RESPONSE（服务端无需返回响应），
        mBluetoothGattCharacteristic = new BluetoothGattCharacteristic(mUuid,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(mUuid,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        //6.将描述加入到特征中
        mBluetoothGattCharacteristic.addDescriptor(descriptor);
        //7.将特征加入到服务中
        mService.addCharacteristic(mBluetoothGattCharacteristic);
        //8.获取周边
        mBluetoothGattServer = mBluetoothManager.openGattServer(mContext,
                new BluetoothGattServerCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                        // 连接状态改变
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.d(TAG, "onConnectionStateChange device = " + device.getAddress() + " connected");
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.e(TAG, "onConnectionStateChange device = " + device.getAddress() + " disconnected");
                        }
                    }

                    @Override
                    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                        Log.d(TAG, String.format("onCharacteristicReadRequest 接收到特征值读请求：device = %s ,requestId = %s ,offset = %s",
                                device.getAddress(), requestId, offset));

                        byte[] value = characteristic.getValue();
                        if (value == null) {
                            value = "response ok".getBytes();
                            characteristic.setValue(value);
                        }

                        if (offset != 0) {
                            int newLen = value.length - offset;
                            byte[] retVal = new byte[value.length - offset];
                            System.arraycopy(value, offset, retVal, 0, newLen);
                            value = retVal;
                        }

                        // 请求读特征
                        if (mUuid.equals(characteristic.getUuid())) {
                            Log.d(TAG, "onCharacteristicReadRequest: sendResponse=" + new String(value));
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                        }
                    }

                    @Override
                    public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId,
                                                             BluetoothGattCharacteristic characteristic,
                                                             boolean preparedWrite, boolean responseNeeded,
                                                             int offset, byte[] value) {
                        // super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                        //当远程设备请求写入本地特征时回调
                        //通常我们讲的BLE通信，其实就说对characteristic的读写或者订阅
                        //必须调用BluetoothGattServer.sendResponse
                        Log.d(TAG, String.format("onCharacteristicWriteRequest：device = %s ,requestId = %s ,preparedWrite = %s ,responseNeeded = %s ,offset = %s",
                                device.getAddress(), requestId, preparedWrite, responseNeeded, offset));
                        if (responseNeeded) {
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                        }

                        //分段写
                        if (preparedWrite) {
                            mWritingObj = characteristic;
                        } else {//不是分段写
                            characteristic.setValue(value);
                        }

                        ByteBuffer byteBuffer = getBuffer(device.getAddress());
                        byteBuffer.put(value);
                        byteBuffer.flip();  //for not response
                        int readResult = 0;
                        while ((readResult = parsePdu(byteBuffer, device)) > 0) {
                            //loop parse
                            Log.d(TAG, "ble read length:" + readResult);
                        }
                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                        super.onExecuteWrite(device, requestId, execute);
                        // 当分段写时，才会回调此方法
                        Log.d(TAG, String.format("onExecuteWrite：device = %s ,requestId = %s ,execute = %s ",
                                device.getAddress(), requestId, execute));

                        if (execute) {
                            ByteBuffer byteBuffer = mByteReadBufferMap.get(device.getAddress());
                            if (byteBuffer != null) {
                                byteBuffer.flip();
                                byte[] data = byteBuffer.array();
                                if (mWritingObj != null) {
                                    if (mWritingObj instanceof BluetoothGattCharacteristic) {
                                        ((BluetoothGattCharacteristic) mWritingObj).setValue(data);
                                    } else if (mWritingObj instanceof BluetoothGattDescriptor) {
                                        ((BluetoothGattDescriptor) mWritingObj).setValue(data);
                                    } else {
                                        throw new RuntimeException("writingObj类型不明");
                                    }
                                } else {
                                    throw new RuntimeException("writingObj为空");
                                }
                            }
                            // 注意，当写数据过长时，会自动分片，多次调用完onCharacteristicWriteRequest后，便会调用此方法，要在此方法中发送响应，execute参数指示是否执行成功，可按照此参数发送响应的状态
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                        } else {
                            Log.d(TAG, "BLE SERVER: onExecuteWrite发送失败响应");
                            // 发送一个失败响应
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                        }
                    }

                    @Override
                    public void onServiceAdded(int status, BluetoothGattService service) {
                        super.onServiceAdded(status, service);
                        Log.d(TAG, String.format("onServiceAdded：status = %s, service = %s",
                                status, service.getUuid()));
                    }

                    @Override
                    public void onMtuChanged(BluetoothDevice device, int mtu) {
                        super.onMtuChanged(device, mtu);
                        Log.d(TAG, String.format("onMtuChanged：device = %s, mtu = %s",
                                device.getAddress(), mtu));
                        mMtu = mtu;
                    }

                    @Override
                    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                        Log.d(TAG, String.format("onDescriptorReadRequest: device = %s ,requestId = %s ,offset = %s ,descriptor = %s ", device.getAddress(),
                                requestId, offset, descriptor.getUuid()));
                        String response = "descriptor"; //模拟数据
                        mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                                offset, response.getBytes()); // 响应客户端
                        Log.d(TAG, "客户端读取Descriptor[" + descriptor.getUuid() + "]:" + response);
                    }

                    @Override
                    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                        Log.d(TAG, String.format("onDescriptorWriteRequest:device = %s ,requestId = %s ,descriptor = %s ,preparedWrite = %s ,responseNeeded = %s ,offset = %s ,value = %s ",
                                device.getAddress(), requestId, descriptor.getUuid(),
                                preparedWrite, responseNeeded, offset, value.length));
                        if (responseNeeded) {
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
                        }

                    }
                });
        //9.将服务加入到周边
        return mBluetoothGattServer.addService(mService);

    }


    private ByteBuffer getBuffer(String device) {
        synchronized (mByteReadBufferMap) {
            ByteBuffer byteBuffer = mByteReadBufferMap.get(device);
            if (byteBuffer == null) {
                byteBuffer = ByteBuffer.allocate(1024 * 100);
                mByteReadBufferMap.put(device, byteBuffer);
            }
            return byteBuffer;
        }
    }

    private UUID getUUID() {
        String mac = MACUtils.getMac(mContext).replace(":", "");
        String uuid = mac.substring(0, 8) + "-" + mac.substring(8) + "-1111-1111-111111111111";
        Log.d(TAG, "getUUID: " + uuid + " mac:" + mac);
        return UUID.fromString(uuid);
    }


    private void initNetUuid() {
        String mac = MACUtils.getMac(mContext).replace(":", "");
        String uuid = NET_UUID_PREFIX + "-" + mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8) + "-" + NET_UUID_SUFFIX;
        mUuid = UUID.fromString(uuid);
        Log.d(TAG, "initNetUuid: " + mUuid.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void removeService() {
        mAdvertiser.stopAdvertising(mAdvertiseCallback);
        if (mService != null) {
            mBluetoothGattServer.removeService(mService);
            mBluetoothGattServer.close();
        }
    }


    public void sendMessage(final ByteBuffer byteBuffer, final BluetoothDevice device) {
        mProcessHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                sendMessageByThread(byteBuffer, device);
            }
        });
    }


    public void sendMessage(final String message, final byte type, final BluetoothDevice device) {
        byte[] data = message.getBytes();
        BlePdu pdu = new BlePdu();
        pdu.pduType = type;
        pdu.length = (short) data.length;
        pdu.body = data;

        final ByteBuffer byteBuffer = ByteBuffer.allocate(BlePdu.PDU_HEADER_LENGTH + pdu.body.length);
        byteBuffer.put(BlePdu.pduStartFlag);
        byteBuffer.put(pdu.pduType);
        byteBuffer.putShort(pdu.length);
        byteBuffer.put(pdu.body);
        byteBuffer.flip();

        mProcessHandler.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                sendMessageByThread(byteBuffer, device);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void sendMessageByThread(ByteBuffer byteBuffer, BluetoothDevice device) {
        byte[] src = byteBuffer.array();
        int limit = mMtu - 3;
        int totalLen = src.length;

        if (totalLen > limit) {
            int count = totalLen / limit + 1;
            for (int i = 0; i < count; i++) {
                int offset = i * limit;
                int length;
                if (i < count - 1) {
                    length = limit;
                } else {
                    length = totalLen - (i * limit);
                }
                byte[] dst = new byte[length];
                System.arraycopy(src, offset, dst, 0, length);

                mBluetoothGattCharacteristic.setValue(dst);
                if (device != null) {
                    mBluetoothGattServer.notifyCharacteristicChanged(device, mBluetoothGattCharacteristic, false);
                }
            }

        } else {
            mBluetoothGattCharacteristic.setValue(src);
            if (device != null) {
                mBluetoothGattServer.notifyCharacteristicChanged(device, mBluetoothGattCharacteristic, false);
            }
        }
    }

    @Override
    public void OnRec(final BlePdu blePdu, final BluetoothDevice device) {
        Log.d(TAG, "OnRec: " + new String(blePdu.body));
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothServerCallBack.onMessageShow(blePdu, device);
            }
        });

    }
}
