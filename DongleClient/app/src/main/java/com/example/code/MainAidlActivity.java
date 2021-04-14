package com.example.code;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.sdk.IBindListener;
import com.coocaa.sdk.IReceiveMessage;
import com.coocaa.sdk.IResultListener;
import com.coocaa.sdk.SdkAidlManager;
import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import swaiotos.channel.iot.SkyServer;
import swaiotos.channel.iot.response.DeviceDataResp;
import swaiotos.channel.iot.utils.AppUtils;


public class MainAidlActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "yao";

    private static final String TARGET_CLIENT = "test_aidl_target_client";

    private RecyclerView recyclerView;
    private MessageAdapter mMessageAdapter;
    private EditText et_bind_code;
    private TextView tv_info;
    private Context mContext;
    private String connectSid;

    private final IReceiveMessage listener = new IReceiveMessage.Stub() {
        @Override
        public void OnRec(String targetClient, IMMessage msg) throws RemoteException {
            Log.d("yao", "OnRec message=" + msg.toString());
            String connect = msg.getContent();
            String type = msg.getType().name();
            Message message = new Message(Message.MSG_TYPE.RECEIVE, connect, type);
            mMessageAdapter.addMsg(message);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl_main);
        mContext = this;
        SdkAidlManager.instance().init(this, new SdkAidlManager.InitListener() {
            @Override
            public void success() {
                Log.d("yao", "init success");
            }

            @Override
            public void fail() {
                Log.d("yao", "init fail");
            }
        });

        tv_info = findViewById(R.id.tv_info);
        et_bind_code = findViewById(R.id.et_bind_code);
        recyclerView = findViewById(R.id.recyclerView);
        mMessageAdapter = new MessageAdapter(this, recyclerView);
        recyclerView.setAdapter(mMessageAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.bind_device).setOnClickListener(this);
        findViewById(R.id.connect).setOnClickListener(this);
        findViewById(R.id.send_msg).setOnClickListener(this);
        findViewById(R.id.send_PPT).setOnClickListener(this);

        SdkAidlManager.instance().addNotifyListener(TARGET_CLIENT, listener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }


    private void connect(final String targetSid) {
        final long startTime = System.currentTimeMillis();
        SdkAidlManager.instance().post(new Runnable() {
            @Override
            public void run() {
                final Session session = SdkAidlManager.instance().connect(targetSid, 5000);

                long time = System.currentTimeMillis() - startTime;
                Log.d(TAG, "connect onSuccess session=" + session.toString() + "time=" + time);
                Log.d(TAG, "connect onSuccess time=" + time);

                AppUtils.setStringSharedPreferences(mContext, "connect_sid", targetSid);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_info.setText("connect onSuccess time=" + time + " session=" + session.toString());
                    }
                });
            }
        });
    }

    private void bindDevice() {
        String code = et_bind_code.getText().toString();
        IBindListener callback = new IBindListener.Stub() {
            @Override
            public void onSuccess(String device) throws RemoteException {
                Log.d(TAG, "bindDevice onSuccess deviceData=" + device);
                DeviceDataResp.DataBean deviceData = new Gson().fromJson(device, DeviceDataResp.DataBean.class);
                connectSid = deviceData.getSid();
                tv_info.setText("bindDevice onSuccess connectSid=" + connectSid);
            }

            @Override
            public void onFail(int code, String message) throws RemoteException {
                Log.d(TAG, "bindDevice onFail code=" + code + " message=" + message);
                tv_info.setText("bindDevice onFail");
            }
        };
        SdkAidlManager.instance().bindDevice(code, callback);
    }

    private int count;

    private void sendMessage() {
        String targetClient = "ss-iotclientID-9527";
        String text = "test" + count++;

        SdkAidlManager.instance().sendMessage(targetClient, text);
    }


    private void sendPPT() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(dir.getAbsolutePath(), "1.ppt");

        Session source = SdkAidlManager.instance().getMySession();
        Session target = SdkAidlManager.instance().getTargetSession();
        String sourceClient = SdkAidlManager.SOURCE_CLIENT;
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

        IResultListener callback = new IResultListener.Stub() {
            @Override
            public void onResult(int code, String msg) throws RemoteException {
                Log.d("yao", "send ppt=" + msg);
                tv_info.setText("send ppt=" + msg);
            }
        };
        SdkAidlManager.instance().sendMessage(imMessage, callback);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bind_device:
                bindDevice();
                break;
            case R.id.connect:
                connect(connectSid);
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
        SdkAidlManager.instance().removeNotifyListener(TARGET_CLIENT, listener);
    }
}