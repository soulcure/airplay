package swaiotos.channel.iot.tv;


import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;
import swaiotos.channel.iot.utils.ThreadManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "IOT_CHANNEL";

    private DemoApplication app;
    //声明接收消息 Service ID ,跟 AndroidManifest 定义一致
    public static final String AUTH = "ss-clientID-XXXXXXX";

    private DeviceAdminManager.OnDeviceBindListener deviceBindListener;
    private DeviceAdminManager.OnDeviceChangedListener onDeviceChangedListener;
    private DeviceAdminManager.OnDeviceInfoUpdateListener mOnDeviceInfoUpdateListener;

    private SessionManager.OnMySessionUpdateListener mOnMySessionUpdateListener;
    private SessionManager.OnSessionUpdateListener mConnectedSessionOnUpdateListener;
    private SessionManager.OnSessionUpdateListener mOnServerSessionOnUpdateListener;

    private Button btn_send, bind_btn, unbind_btn, devices_btn, update_devices_status;
    private Button set_devices_listener_btn, current_device_btn, connect_device_btn;
    private Button my_session_btn, current_connect_session_btn;
    private Button dis_connect_session_btn, listener_session_btn;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {
        app = (DemoApplication) getApplication();

        mEditText = findViewById(R.id.check_bt_editText);
        mEditText.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        btn_send = findViewById(R.id.btn_send);
        bind_btn = findViewById(R.id.bind_btn);
        unbind_btn = findViewById(R.id.unbind_btn);
        devices_btn = findViewById(R.id.devices_btn);
        update_devices_status = findViewById(R.id.update_devices_status_btn);
        set_devices_listener_btn = findViewById(R.id.set_devices_listener_btn);
        current_device_btn = findViewById(R.id.current_device_btn);
        connect_device_btn = findViewById(R.id.connect_device_btn);
        my_session_btn = findViewById(R.id.my_session_btn);
        current_connect_session_btn = findViewById(R.id.current_connect_session_btn);
        dis_connect_session_btn = findViewById(R.id.dis_connect_session_btn);
        listener_session_btn = findViewById(R.id.listener_session_btn);

        btn_send.setOnClickListener(this);
        bind_btn.setOnClickListener(this);
        unbind_btn.setOnClickListener(this);
        devices_btn.setOnClickListener(this);
        update_devices_status.setOnClickListener(this);
        set_devices_listener_btn.setOnClickListener(this);
        current_device_btn.setOnClickListener(this);
        connect_device_btn.setOnClickListener(this);
        my_session_btn.setOnClickListener(this);
        current_connect_session_btn.setOnClickListener(this);
        dis_connect_session_btn.setOnClickListener(this);
        listener_session_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!app.getInitIOTChannel()) {
            Log.e(TAG, "初始化失败,无法使用跨屏互动相关功能！！！");
            return;
        }

        if (id == R.id.bind_btn) {
            /**绑定案例**/
            if (TextUtils.isEmpty(mEditText.getText().toString()) || mEditText.getText().toString().length() < 6) {
                Toast.makeText(getApplicationContext(), "请输入正确的验证码", Toast.LENGTH_LONG).show();
                return;
            }
            submitDevice(mEditText.getText().toString());

        } else if (id == R.id.unbind_btn) {
            /**解绑案例**/
            List<Device> devices = getDevices();
            if (devices == null || devices.size() <= 0) {
                Toast.makeText(getApplicationContext(), "设备列表为空", Toast.LENGTH_LONG).show();
                return;
            }
            unbindDevice(devices.get(0));

        } else if (id == R.id.set_devices_listener_btn) {
            /** 设置设备监听 */
            setDeviceStatusListener();
        } else if (id == R.id.update_devices_status_btn) {
            /**设置状态更新   绑定的设备列表有设备从离线到在线或者在线到离线，会通过回调通知 **/
            setDeviceStatusListener();
            getDeviceOnlineStatus();
        } else if (id == R.id.connect_device_btn) {
            /** 连接设备*/
            List<Device> devices = getDevices();
            if (devices == null || devices.size() <= 0) {
                Toast.makeText(getApplicationContext(), "设备列表为空", Toast.LENGTH_LONG).show();
                return;
            }
            setSessionListener();
            connectDevice(devices.get(0).getLsid(), 5000L);
        } else if (id == R.id.dis_connect_session_btn) {
            /** 断开设备*/
            List<Device> devices = getDevices();
            if (devices == null || devices.size() <= 0) {
                Toast.makeText(getApplicationContext(), "设备列表为空", Toast.LENGTH_LONG).show();
                return;
            }
            setSessionListener();
            disconnectDevice();
        } else if (id == R.id.devices_btn) {
            /**获取设备列表**/
            List<Device> devices = getDevices();
            if (devices == null || devices.size() <= 0) {
                Toast.makeText(getApplicationContext(), "设备列表为空", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "列表中第一个好友的唯一标识:" + devices.get(0).getLsid(), Toast.LENGTH_LONG).show();
        } else if (id == R.id.current_device_btn) {
            /**  获取当前正在连接着的设置 **/
            getCurrentConnectDevice();
        } else if (id == R.id.my_session_btn) {
            /**  获取本地设备信息 **/
            getLocalSession();
        } else if (id == R.id.current_connect_session_btn) {
            /**  获取本地设备信息  **/
            getCurrentConnectSession();
        } else if (id == R.id.listener_session_btn) {
            /**  设置连接（session）信息变化监听**/
            setSessionListener();
        } else if (id == R.id.btn_send) {
            /** 发送IM消息*/
            String cmd = "{\"cmd\":\"24\",\"param\":\"\",\"type\":\"KEY_EVENT\"}";  //声音减
            String targetClient = "ss-clientID-appstore_12345";  //TV端 应用市场
            sendTextMessage(cmd, targetClient);
        }

    }

    /**
     * 绑定接口
     *
     * @param bindCode 绑定码
     */

    public void submitDevice(String bindCode) {

        if (TextUtils.isEmpty(bindCode)) {
            Log.e(TAG, "bindcode is null or \"\"");
            return;
        }
        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().startBind("", bindCode, new DeviceAdminManager.OnBindResultListener() {
                @Override
                public void onSuccess(String bindCode, Device device) {
                    Log.d(TAG, "submitDevice onsuccess!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "bind onsuccess", Toast.LENGTH_LONG).show();
                        }
                    });

                }

                @Override
                public void onFail(String bindCode, final String errorType, final String msg) {
                    Log.d(TAG, "submitDevice onFail!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "bind failure", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }, 50000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解绑接口
     *
     * @param device 解绑设备
     */
    public void unbindDevice(Device device) {

        if (device == null || TextUtils.isEmpty(device.getLsid())) {
            Log.e(TAG, "解绑设备设备信息为空！");
            return;
        }

        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().unBindDevice("", device.getLsid(), 1, new DeviceAdminManager.unBindResultListener() {
                @Override
                public void onSuccess(String lsid) {
                    Log.d(TAG, "unbind onsuccess!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "unbind onsuccess", Toast.LENGTH_LONG).show();
                        }
                    });

                }

                @Override
                public void onFail(String lsid, String errorType, String msg) {
                    Log.d(TAG, "unbind onfail! reason errorType" + errorType + " msg:" + msg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "unbind onfail!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备列表接口
     */
    public List<Device> getDevices() {

        try {
            return IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().getDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 手动拉取设备在线离线状态列表，设备状态（在线、离线）有变化会通过onDeviceChangedListener回调通知到使用方
     */
    public void getDeviceOnlineStatus() {

        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().getDeviceOnlineStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设备监听
     */
    public void setDeviceStatusListener() {

        if (deviceBindListener == null) {
            deviceBindListener = new DeviceAdminManager.OnDeviceBindListener() {
                @Override
                public void onDeviceBind(String s) {
                    Log.d(TAG, "onDeviceBind!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "onDeviceBind", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onDeviceUnBind(String s) {
                    Log.d(TAG, "onDeviceUnBind!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "onDeviceUnBind", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().addDeviceBindListener(deviceBindListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (onDeviceChangedListener == null) {
            onDeviceChangedListener = new DeviceAdminManager.OnDeviceChangedListener() {
                @Override
                public void onDeviceOffLine(Device device) {
                    if (device != null)
                        Log.e(TAG, "------onDeviceOffLine:" + device.encode());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "设备从在线到离线通知", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onDeviceOnLine(Device device) {
                    if (device != null)
                        Log.e(TAG, "------onDeviceOnLine:" + device.encode());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "设备从离线到在线通知", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onDeviceUpdate(Device device) {
                    Log.d(TAG, "onDeviceUpdate!");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "设备从离线到在线通知", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };

            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().addOnDeviceChangedListener(onDeviceChangedListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mOnDeviceInfoUpdateListener == null) {
            mOnDeviceInfoUpdateListener = new DeviceAdminManager.OnDeviceInfoUpdateListener() {
                @Override
                public void onDeviceInfoUpdate(List<Device> list) {
                    Log.d(TAG, "onDeviceInfoUpdate");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "设备属性变化（DeviceInfo）通知", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            };
            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().addDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeDeviceListener() {
        if (deviceBindListener != null) {
            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().removeDeviceBindListener(deviceBindListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (onDeviceChangedListener != null) {
            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().removeOnDeviceChangedListener(onDeviceChangedListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mOnDeviceInfoUpdateListener != null) {
            try {
                IOTChannel.mananger.getSSChannel().getDeviceManager().removeDeviceInfoUpdateListener(mOnDeviceInfoUpdateListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接设备
     *
     * @param sid                设备唯一标识
     * @param connectionOverTime 连接超时时间
     */
    public void connectDevice(final String sid, final long connectionOverTime) {
        try {
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "" + Thread.currentThread().getId());
                    try {
                        final Session connectionSession = IOTAdminChannel.mananger.getSSAdminChannel().getController().connect(sid, connectionOverTime);
                        ThreadManager.getInstance().uiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "成功连接设备的标识:" + connectionSession.getId(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     *
     * 断连设备
     *
     * */
    public void disconnectDevice() {

        Session currentConnectSession = getCurrentConnectSession();
        Log.d(TAG, "" + Thread.currentThread().getId());
        try {
            if (currentConnectSession != null) {
                IOTAdminChannel.mananger.getSSAdminChannel().getController().disconnect(currentConnectSession);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前连接着的设备
     */
    public void getCurrentConnectDevice() {
        try {
            Device currentConnectionDevice = IOTChannel.mananger.getSSChannel().getDeviceManager().getCurrentDevice();
            if (currentConnectionDevice == null || TextUtils.isEmpty(currentConnectionDevice.getLsid())) {
                Toast.makeText(getApplicationContext(), "当前未有连接着的设置", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "当前连接的设置的唯一标识:" + currentConnectionDevice.getLsid(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 本地连接信息
     */
    public void getLocalSession() {
        try {
            Session mySession = IOTChannel.mananger.getSSChannel().getSessionManager().getMySession();

            Log.e(TAG, mySession.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接着设备的session信息
     */
    public Session getCurrentConnectSession() {
        try {
            Session connectedSession = IOTChannel.mananger.getSSChannel().getSessionManager().getConnectedSession();
            if (connectedSession == null || TextUtils.isEmpty(connectedSession.getId())) {
                Toast.makeText(getApplicationContext(), "请先连接！", Toast.LENGTH_LONG).show();
                return null;
            }
            Log.e(TAG, connectedSession.toString());
            return connectedSession;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置监听session（连接信息）
     * <p>
     * session介绍：每台设备都有它独有的连接信息  有设备的唯一标识、有本地IP等信息
     */
    public void setSessionListener() {
        try {
            if (mOnMySessionUpdateListener == null) {
                //设置本地设备连接信息变化的监听
                mOnMySessionUpdateListener = new SessionManager.OnMySessionUpdateListener() {
                    @Override
                    public void onMySessionUpdate(Session session) {
                        if (session == null)
                            return;
                        Log.e(TAG, session.toString());
                    }
                };
                IOTChannel.mananger.getSSChannel().getSessionManager().addOnMySessionUpdateListener(mOnMySessionUpdateListener);
            }

            if (mConnectedSessionOnUpdateListener == null) {
                //连接着设备的session变化
                mConnectedSessionOnUpdateListener = new SessionManager.OnSessionUpdateListener() {
                    @Override
                    public void onSessionConnect(Session session) {
                        Log.e(TAG, "---mConnectedSessionOnUpdateListener----------onSessionConnect------:" + session.toString());
                    }

                    @Override
                    public void onSessionUpdate(Session session) {
                        Log.e(TAG, "---mConnectedSessionOnUpdateListener----------onSessionUpdate------:" + session.toString());
                    }

                    @Override
                    public void onSessionDisconnect(Session session) {
                        Log.e(TAG, "------mConnectedSessionOnUpdateListener-------onSessionDisconnect------:" + session.toString());
                    }
                };
                IOTChannel.mananger.getSSChannel().getSessionManager().addConnectedSessionOnUpdateListener(mConnectedSessionOnUpdateListener);
            }

            if (mOnServerSessionOnUpdateListener == null) {
                //连接着设备的session变化
                mOnServerSessionOnUpdateListener = new SessionManager.OnSessionUpdateListener() {
                    @Override
                    public void onSessionConnect(Session session) {
                        Log.e(TAG, "-----mOnServerSessionOnUpdateListener--------onSessionConnect------:" + session.toString());
                    }

                    @Override
                    public void onSessionUpdate(Session session) {
                        Log.e(TAG, "-----mOnServerSessionOnUpdateListener--------onSessionUpdate------:" + session.toString());
                    }

                    @Override
                    public void onSessionDisconnect(Session session) {
                        Log.e(TAG, "-----mOnServerSessionOnUpdateListener--------onSessionDisconnect------:" + session.toString());
                    }
                };
                IOTChannel.mananger.getSSChannel().getSessionManager().addServerSessionOnUpdateListener(mOnServerSessionOnUpdateListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     *
     * 移除连接监听
     *
     * */
    public void removeSessionListener() {
        try {
            if (mOnMySessionUpdateListener != null) {
                IOTChannel.mananger.getSSChannel().getSessionManager().removeOnMySessionUpdateListener(mOnMySessionUpdateListener);
            }

            if (mConnectedSessionOnUpdateListener != null) {
                IOTChannel.mananger.getSSChannel().getSessionManager().removeConnectedSessionOnUpdateListener(mConnectedSessionOnUpdateListener);
            }

            if (mOnServerSessionOnUpdateListener != null) {
                IOTChannel.mananger.getSSChannel().getSessionManager().removeServerSessionOnUpdateListener(mOnServerSessionOnUpdateListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cmd          发送的消息内容，json格式
     * @param targetClient 消息接收者ID，声明接收消息 Service ID ,跟 AndroidManifest 定义一致
     */
    public void sendTextMessage(String cmd, String targetClient) {
        try {
            Session my = IOTChannel.mananger.getSSChannel().getSessionManager().getMySession();
            Session target = IOTChannel.mananger.getSSChannel().getSessionManager().getConnectedSession();

            IMMessage message = IMMessage.Builder.createTextMessage(my, target, AUTH, targetClient, cmd);
            sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("no session connected!")) {
                Toast.makeText(getApplicationContext(), "请先设备之间的建立连接", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendMessage(IMMessage message) throws Exception {

        IOTChannel.mananger.getSSChannel().getIMChannel().send(message, new IMMessageCallback() {
            @Override
            public void onStart(IMMessage message) {
                Log.d(TAG, "onStart: " + message);
            }

            @Override
            public void onProgress(IMMessage message, int progress) {
                Log.d(TAG, "onProgress: " + progress);
            }

            @Override
            public void onEnd(IMMessage message, int code, String info) {
                Log.d(TAG, "onEnd: code=" + code + "\n info:" + info);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeDeviceListener();
        removeSessionListener();
        deviceBindListener = null;
        onDeviceChangedListener = null;
        mOnDeviceInfoUpdateListener = null;
        mOnMySessionUpdateListener = null;
        mConnectedSessionOnUpdateListener = null;
        mOnServerSessionOnUpdateListener = null;
    }
}