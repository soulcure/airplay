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
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.skyworth.dpclientsdk.MACUtils;
import com.skyworth.dpclientsdk.ProcessHandler;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BluetoothServerImp extends BlePduUtil implements IBluetoothServer{

    private static final String TAG = "bleServer";

    //配网UUID前缀
    private static final String NET_UUID_PREFIX = "FFFFFF01";
    //配网模式后缀
    private static final String NET_UUID_SUFFIX = "111111111111";

    //附带信息标识
    private static final int MANUFACTURERID = 0x11;
    //设备前缀
    private static final String DEVICE_NAME_PREFIX = "酷开共享屏";
//    private ProcessHandler mProcessHandler;

    private Context mContext;

    private BluetoothServerCallBack mBluetoothServerCallBack;
    //蓝牙设备管理类
    private BluetoothManager mBluetoothManager;
    //蓝牙设配器
    private BluetoothAdapter mAdapter;
    private BluetoothLeAdvertiser mAdvertiser;
    //蓝牙Server
    private BluetoothGattServer mBluetoothGattServer;
    //蓝牙服务
    private BluetoothGattService mService;
    //蓝牙特征
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private UUID mServiceUuid;
    private String mCustomData;
    private int mMtu = 20;
    private Map<String, ByteBuffer> mByteReadBufferMap = new LinkedHashMap<>();
    private Object mWritingObj;

    private int mAdvertiseFlag = 0; // 0: not_start   1: starting    2: started     3: stopping   4: stopped
    private Handler mHandler;

    @Override
    public void setCustomData(String hexData) {
        if (hexData != mCustomData) {
            Log.d(TAG, "setOrUpdateServiceData() called with data != mServiceData");
            mCustomData = hexData;
            if (mAdvertiseFlag == 1 || mAdvertiseFlag == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAdvertiser.stopAdvertising(mAdvertiseCallback);
                    initServiceUuid();
                    startAdvertise();
                }
            } else {
                Log.w(TAG, "setOrUpdateServiceData() mAdvertiseFlag wrong: " + mAdvertiseFlag);
            }
        } else {
            Log.d(TAG, "setOrUpdateServiceData() called with data == mServiceData");
        }
    }

    private void initServiceUuid() {
        String mac = MACUtils.getMac(mContext).replace(":", "");
        String prefix = mCustomData == null ? NET_UUID_PREFIX : mCustomData;
        String uuid = prefix + "-" + mac.substring(0, 4) + "-" + mac.substring(4, 8) + "-" + mac.substring(8) + "-" + NET_UUID_SUFFIX;
        mServiceUuid = UUID.fromString(uuid);
        Log.d(TAG, "initServiceUuid: " + mServiceUuid.toString());
    }

    @RequiresApi(api = 21)
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "开启ble广播成功");
            mBluetoothServerCallBack.onStartSuccess(mServiceUuid.toString());
            mAdvertiseFlag = 2;
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "开启ble广播失败:" + errorCode);
            mAdvertiseFlag = 4;
            mBluetoothServerCallBack.onStartSuccess(mServiceUuid.toString());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BluetoothServerImp(Context context, BluetoothServerCallBack callBack) {
        mContext = context;
        mBluetoothServerCallBack = callBack;
//        mProcessHandler = new ProcessHandler("ble-server", true);
        mHandler = new Handler();
        //1.获取管理类
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        //判断设备是否支持蓝牙
        if (mBluetoothManager == null) {
            return;
        }
        //2.获取蓝牙适配器
        mAdapter = mBluetoothManager.getAdapter();
        if (!mAdapter.isEnabled()) {
            mAdapter.enable();
        }
        mAdvertiser = mAdapter.getBluetoothLeAdvertiser();
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
        if (Build.VERSION.SDK_INT >= 21 && mAdapter.isMultipleAdvertisementSupported()) {
            if (initService()) {
                //10.启动ble广播
                initServiceUuid();
                startAdvertise();
            } else {
                Log.d(TAG, "开启ble失败");
            }
        } else {
            Log.d(TAG, "开启ble失败");
            if (!mAdapter.isMultipleAdvertisementSupported()) {
                Log.d(TAG, "您的设备不支持蓝牙从模式");
            }
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void removeService() {
        Log.d(TAG, "removeService() called");
        mAdvertiser.stopAdvertising(mAdvertiseCallback);
        if (mService != null) {
            mBluetoothGattServer.removeService(mService);
            mBluetoothGattServer.close();
        }
    }

    @Override
    public void sendMessage(String msg, byte tempCmd, BluetoothDevice mBluetoothDevice) {
        Log.d(TAG, "sendMessage() called with: msg = [" + msg + "], tempCmd = [" + tempCmd + "], mBluetoothDevice = [" + mBluetoothDevice + "]");
    }

    /**
     * 开启ble广播
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAdvertise() {
        Log.d(TAG, "startAdvertise() called");
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();
        ParcelUuid parcelUuid = new ParcelUuid(mServiceUuid);
        AdvertiseData advertiseData = new AdvertiseData.Builder()
//                .setIncludeDeviceName(true)
//                .setIncludeTxPowerLevel(true)
//                .addServiceData(parcelUuid, mServiceData)
                .addServiceUuid(parcelUuid)
                .build();
        AdvertiseData scanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerData(MANUFACTURERID, (DEVICE_NAME_PREFIX+Build.MODEL).getBytes())
                .build();

        mAdvertiser.startAdvertising(settings, advertiseData, scanResponse, mAdvertiseCallback);
        mAdvertiseFlag = 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean initService() {
        //3.获取服务
        mService = new BluetoothGattService(mServiceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //4.获取一个特征
        // 注意，如果要使特征值可写，PROPERTY需设置PROPERTY_WRITE（服务端需要返回响应）或PROPERTY_WRITE_NO_RESPONSE（服务端无需返回响应），
        mBluetoothGattCharacteristic = new BluetoothGattCharacteristic(mServiceUuid,
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
//        final BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(mServiceUuid,
//                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        //6.将描述加入到特征中
//        mBluetoothGattCharacteristic.addDescriptor(descriptor);
        //7.将特征加入到服务中
        mService.addCharacteristic(mBluetoothGattCharacteristic);
        //8.获取周边
        mBluetoothGattServer = mBluetoothManager.openGattServer(mContext,
                new BluetoothGattServerCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                        // 连接状态改变
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Log.d(TAG, "BLE服务端:BLE Connected!");
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            Log.d(TAG, "BLE服务端:BLE Disconnected!");
                        }
                    }

                    @Override
                    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                        Log.d(TAG, "BLE服务端:接收到特征值读请求!" + " requestId=" + requestId + " offset=" + offset);

                        BluetoothGattCharacteristic gattCharacteristic = mBluetoothGattServer.getService(mServiceUuid).getCharacteristic(mServiceUuid);
                        if (characteristic == gattCharacteristic) {
                            // 证明characteristic与通过gattServer得到的是同一个对象
                            Log.d(TAG, "BLE服务端:same characteristic!");
                        }

                        byte[] value = characteristic.getValue();
                        if (value == null) {
                            value = "init responseData".getBytes();
                            characteristic.setValue(value);
                        }


                        if (offset != 0) {
                            int newLen = value.length - offset;
                            byte[] retVal = new byte[value.length - offset];
                            System.arraycopy(value, offset, retVal, 0, newLen);
                            value = retVal;
                        }

                        // 请求读特征
                        if (mServiceUuid.equals(characteristic.getUuid())) {
                            Log.d(TAG, "onCharacteristicReadRequest: sendResponse");
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
                        Log.d(TAG, "BLE Server端 onCharacteristicWriteRequest"
                                + " requestId:" + requestId + " offset:" + offset + " prepareWrite:" + preparedWrite
                                + " responseNeeded:" + responseNeeded + " value:" + new String(value) + " length:" + value.length);

                        Log.d(TAG, "device address:" + device.getAddress() + " name:" + device.getName());

                        ByteBuffer byteBuffer = mByteReadBufferMap.get(device.getAddress());
                        if (byteBuffer == null) {
                            byteBuffer = ByteBuffer.allocate(1024 * 20);
                            mByteReadBufferMap.put(device.getAddress(), byteBuffer);
                        }

                        // 不是分段写
                        if (!preparedWrite) {
                            characteristic.setValue(value);
                            byteBuffer.put(value);
                        } else {
                            if (offset == 0) {
                                byteBuffer.clear();
                            }
                            byteBuffer.put(value);
                            mWritingObj = characteristic;
                        }

                        byteBuffer.flip();  //for not response
                        int readResult = 0;
                        while ((readResult = parsePdu(byteBuffer, device)) > 0) {
                            //loop parse
                            Log.d(TAG, "ble read length:" + readResult);
                        }


                        if (responseNeeded) {
                            /*final BlePdu blePdu = BlePduUtil.buildPdu(byteBuffer.array());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mBluetoothServerCallBack.onMessageShow(blePdu, device);
                                }
                            });
                            byteBuffer.clear();*/

                            // 注意，如果写特征值需要响应（特征值的属性是PROPERTY_WRITE不是PROPERTY_WRITE_NO_RESPONSE）,必需发送value作为响应数据。如果外层已
                            //bluetoothServerCallBack.onMsgWriteRequest(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                            //如果外层没有回消息
                            //Log.d(TAG, "回复消息:" + new String(value));
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                        }
                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                        super.onExecuteWrite(device, requestId, execute);
                        // 当分段写时，才会回调此方法
                        Log.d(TAG, "onExecuteWrite called! requestId=" + requestId + " execute=" + execute);
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
                        Log.d(TAG, "onServiceAdded");
                    }

                    @Override
                    public void onMtuChanged(BluetoothDevice device, int mtu) {
                        super.onMtuChanged(device, mtu);
                        Log.d(TAG, "BLE SERVER onMTUChanged mtu=" + mtu);
                        mMtu = mtu;
                    }

                    @Override
                    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                        Log.i(TAG, String.format("onDescriptorReadRequest:%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, offset, descriptor.getUuid()));
                        String response = "descriptor"; //模拟数据
                        mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response.getBytes()); // 响应客户端
                        Log.d(TAG, "客户端读取Descriptor[" + descriptor.getUuid() + "]:" + response);
                    }

                    @Override
                    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                        String valueStr = Arrays.toString(value);
                        Log.i(TAG, String.format("onDescriptorWriteRequest:%s,%s,%s,%s,%s,%s,%s,%s", device.getName(), device.getAddress(), requestId, descriptor.getUuid(),
                                preparedWrite, responseNeeded, offset, valueStr));
                        if (responseNeeded) {
                            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);// 响应客户端
                        }

                    }
                });
        //9.将服务加入到周边
        return mBluetoothGattServer.addService(mService);

    }

    @Override
    public void OnRec(final BlePdu blePdu, final BluetoothDevice device) {
        Log.d(TAG, "OnRec() called with: blePdu = [" + blePdu + "], device = [" + device + "]");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothServerCallBack.onMessageShow(blePdu, device);
            }
        });
    }
}
