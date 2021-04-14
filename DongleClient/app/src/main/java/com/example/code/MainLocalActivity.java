package com.example.code;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import swaiotos.channel.iot.SdkManager;
import swaiotos.channel.iot.SkyServer;
import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.callback.ConnectCallback;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.DeviceCallback;
import swaiotos.channel.iot.callback.LoginCallback;
import swaiotos.channel.iot.callback.NotifyListener;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.callback.UnBindResult;
import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.response.DeviceDataResp;
import swaiotos.channel.iot.utils.AppUtils;


public class MainLocalActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "yao";

    private String mConnectSid;

    private RecyclerView recyclerView;
    private MessageAdapter mMessageAdapter;
    private EditText et_bind_code;
    private TextView tv_info;
    private Context mContext;

    private final NotifyListener listener = new NotifyListener() {
        @Override
        public void OnRec(String targetClient, IMMessage msg) {
            Log.d("yao", "OnRec message=" + msg.toString());
            String connect = msg.getContent();
            String type = msg.getType().name();
            Message message = new Message(Message.MSG_TYPE.RECEIVE, connect, type);
            mMessageAdapter.addMsg(message);
        }
    };

    private final ConnectStatusListener mConnectListener = new ConnectStatusListener() {
        @Override
        public void onConnectStatus(int code, String msg) {
            Log.d(TAG, "onConnectStatus code=" + code + " msg=" + msg);
            tv_info.setText("onConnectStatus code=" + code + " msg=" + msg);
        }

        @Override
        public void onTargetSessionUpdate(Session session) {
            Log.d(TAG, "onTargetSessionUpdate session=" + session.toString());
            tv_info.setText("onTargetSessionUpdate session=" + session.toString());
        }

        @Override
        public void onBindDevice(List<Device> list) {
            Log.d(TAG, "onBindDevice list=" + list.size());
            tv_info.setText("onBindDevice list=" + list.size());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_main);
        mContext = this;

        mConnectSid = AppUtils.getStringSharedPreferences(mContext, "connect_sid", "");

        tv_info = findViewById(R.id.tv_info);
        et_bind_code = findViewById(R.id.et_bind_code);
        recyclerView = findViewById(R.id.recyclerView);
        mMessageAdapter = new MessageAdapter(this, recyclerView);
        recyclerView.setAdapter(mMessageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.bind_device).setOnClickListener(this);
        findViewById(R.id.login_sse).setOnClickListener(this);
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.bind_list).setOnClickListener(this);
        findViewById(R.id.get_bind_list).setOnClickListener(this);
        findViewById(R.id.refresh_online).setOnClickListener(this);
        findViewById(R.id.unbind_device).setOnClickListener(this);

        findViewById(R.id.send_msg).setOnClickListener(this);
        findViewById(R.id.send_PPT).setOnClickListener(this);

        SdkManager.instance().addNotifyListener(SdkManager.SOURCE_CLIENT, listener);
        SdkManager.instance().setConnectListener(mConnectListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    private void loginSSE() {
        Log.d(TAG, "loginSSE start----");
        String sid = "547c08a5f84b411fa945ca8f73c426f6";   //18664023439 coocaa账号
        String accessToken = "2.616661a49bd1437eacfe9f054c2623db";

        SdkManager.instance().loginSSE(sid, accessToken, new LoginCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "loginSSE onSuccess");
                tv_info.setText("loginSSE onSuccess");
            }

            @Override
            public void onFail() {
                Log.d(TAG, "loginSSE onFail");
                tv_info.setText("loginSSE onFail");
            }
        });

    }


    private void reqBindDevice() {
        SdkManager.instance().reqBindDevice(new DeviceCallback() {
            @Override
            public void onSuccess(List<Device> list) {
                Log.d(TAG, "reqBindDevice list size=" + list.size());
                tv_info.setText("reqBindDevice list size=" + list.size());
            }

            @Override
            public void onFail(int code, String msg) {
                Log.e(TAG, "onFail code=" + code + " msg+" + msg);
                tv_info.setText("reqBindDevice onFail");
            }
        });
    }


    private void getBindDevice() {
        List<Device> list = SdkManager.instance().getBindDevice();
        Log.d(TAG, "getBindDevice list size=" + list.size());
        tv_info.setText("getBindDevice list size=" + list.size());
    }

    private void connect(final String targetSid) {
        final long startTime = System.currentTimeMillis();
        SdkManager.instance().connect(targetSid, 5000, new ConnectCallback() {
            @Override
            public void onSuccess(Session session) {
                long time = System.currentTimeMillis() - startTime;
                Log.d(TAG, "connect onSuccess session=" + session.toString() + "time=" + time);
                Log.d(TAG, "connect onSuccess time=" + time);

                AppUtils.setStringSharedPreferences(mContext, "connect_sid", targetSid);

                tv_info.setText("connect onSuccess time=" + time + " session=" + session.toString());
            }

            @Override
            public void onConnectFail(int code, String msg) {
                Log.e(TAG, "connect onFail code=" + code + " msg=" + msg);
                tv_info.setText("connect onFail");
            }
        });
    }

    private void bindDevice() {
        String code = et_bind_code.getText().toString();
        SdkManager.instance().bindDevice(code, new BindResult() {
            @Override
            public void onSuccess(Device deviceData) {
                Log.d(TAG, "bindDevice onSuccess deviceData=" + deviceData.toString());
                mConnectSid = deviceData.getZpLsid();
                tv_info.setText("bindDevice onSuccess mConnectSid=" + mConnectSid);
            }

            @Override
            public void onFail(int code, String message) {
                Log.d(TAG, "bindDevice onFail code=" + code + " message=" + message);
                tv_info.setText("bindDevice onFail");
            }
        });
    }


    private void refreshOnlineStatus() {
        SdkManager.instance().refreshOnlineStatus(new DeviceCallback() {
            @Override
            public void onSuccess(List<Device> list) {
                Log.d(TAG, "refreshOnlineStatus list=" + list.size());
                tv_info.setText("refreshOnlineStatus list=" + list.size());
            }

            @Override
            public void onFail(int code, String msg) {
                Log.e(TAG, "onFail code=" + code + " msg+" + msg);
                tv_info.setText("refreshOnlineStatus onFail");
            }
        });
    }

    public void unbindDevice() {
        String targetSid = mConnectSid;
        int deleteType = 1;
        UnBindResult callback = new UnBindResult() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "unbindDevice onSuccess message=" + message);
                tv_info.setText("unbindDevice onSuccess message=" + message);
            }

            @Override
            public void onFail(int code, String message) {
                Log.e(TAG, "unbindDevice onFail code=" + code + " msg+" + message);
                tv_info.setText("unbindDevice onFail code=" + code + " msg+" + message);
            }
        };

        SdkManager.instance().unbindDevice(targetSid, deleteType, callback);
    }


    private int count;

    private void sendMessage() {
        String targetClient = "ss-iotclientID-9527";
        String text = "test" + count++;

        Message message = new Message(Message.MSG_TYPE.SEND, text, "");
        mMessageAdapter.addMsg(message);

        SdkManager.instance().sendMessage(text, targetClient, new ResultListener() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("yao", "send message=" + msg);
                tv_info.setText("send message=" + msg);
            }
        });
    }


    private void sendPPT() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir.getAbsolutePath(), "1.ppt");

        Session source = SdkManager.instance().getMySession();
        Session target = SdkManager.instance().getTargetSession();
        String sourceClient = SdkManager.SOURCE_CLIENT;
        String targetClient = "ss-clientID-UniversalMediaPlayer";
        IMMessage imMessage = IMMessage.Builder.createDocMessage(source, target,
                sourceClient, targetClient, file);


        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("avatar", "https://passport.coocaa.com/Avatar/getNewAvatar/MzU0MzgxMjg=/0/200.jpg");
            jsonObject.put("mobile", "18664923439");
            jsonObject.put("nickName", "陈琼瑶01");
            jsonObject.put("token", "2.616661a49bd1437eacfe9f054c2623db");
            jsonObject.put("userID", "5ed178a321ba4c838934e0a6c007af43");
            String ownerStr = jsonObject.toString();

            imMessage.putExtra("owner", ownerStr);

            JSONObject cmdObject = new JSONObject();
            cmdObject.put("cmd", "PLAY");
            cmdObject.put("param", new JSONObject().put("name", "1.ppt").toString());
            cmdObject.put("type", "LOCAL_MEDIA");

            imMessage.putExtra("response", cmdObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        imMessage.putExtra("open_id", "5ed178a321ba4c838934e0a6c007af43");
        imMessage.putExtra("mobile", "18664923439");
        imMessage.putExtra("showtips", "true");

        SdkManager.instance().sendMessage(imMessage, new ResultListener() {
            @Override
            public void onResult(int code, String msg) {
                Log.d("yao", "send ppt=" + msg);
                tv_info.setText("send ppt=" + msg);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind_device:
                bindDevice();
                break;

            case R.id.login_sse:
                loginSSE();
                break;

            case R.id.connect:
                connect(mConnectSid);
                break;
            case R.id.bind_list:
                reqBindDevice();
                break;
            case R.id.get_bind_list:
                getBindDevice();
                break;
            case R.id.refresh_online:
                refreshOnlineStatus();
                break;
            case R.id.unbind_device:
                unbindDevice();
                break;
            case R.id.send_msg:
                sendMessage();
                break;
            case R.id.send_PPT:
                sendPPT();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SdkManager.instance().removeNotifyListener(SdkManager.SOURCE_CLIENT);
    }
}