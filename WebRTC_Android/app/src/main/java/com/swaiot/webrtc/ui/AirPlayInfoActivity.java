package com.swaiot.webrtc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.swaiot.webrtc.R;
import com.swaiot.webrtc.StackAct;
import com.swaiot.webrtc.config.Constant;
import com.swaiot.webrtc.entity.Model;
import com.swaiot.webrtc.entity.SSEEvent;
import com.swaiot.webrtc.http.HttpEngine;
import com.swaiot.webrtc.response.LinkCodeResp;
import com.swaiot.webrtc.util.Constants;
import com.swaiot.webrtc.util.ShareUtls;
import com.swaiot.webrtc.util.SignCore;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.internal.CancelAdapt;
import me.jessyan.autosize.internal.CustomAdapt;
import rx.Observer;
import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.IOTChannelImpl;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.session.Session;

public class AirPlayInfoActivity extends Activity implements CancelAdapt {
    private static final String TAG = "webrtc";

    private TextView tv_wifi_name;
    private TextView tv_wifi_pw;
    private TextView tv_host;
    private TextView tv_app;
    private TextView tv_link_code;
    private ScheduledExecutorService mBindExecutorService;
    private boolean isUserExitAppFlag = false;
    private boolean isStartWebRTCFlag = false;

    public static void start(Context context, String wifiAccount, String wifiPW, String host) {
        Intent intent = new Intent(context.getApplicationContext(), AirPlayInfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("wifiAccount", wifiAccount);
        intent.putExtra("wifiPW", wifiPW);
        intent.putExtra("host", host);
        Log.d(TAG,"-----startActivity0------");
        context.getApplicationContext().startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("colin", "AirPlayInfoActivity onCreate");
        setContentView(R.layout.activity_airplay_info);
        //检查是否已经授予权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //若未授权则请求权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }

        tv_wifi_name = findViewById(R.id.tv_wifi_name);
        tv_wifi_pw = findViewById(R.id.tv_wifi_pw);
        tv_host = findViewById(R.id.tv_host);
        tv_app = findViewById(R.id.tv_app);
        tv_link_code = findViewById(R.id.tv_link_code);

        setIntentText(getIntent());
        //setApp();
//        WebRTCActivity.start(getApplicationContext());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntentText(intent);

        startReport();
        Log.d(TAG,"-------------onNewIntent-----");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isUserExitAppFlag = true;
        Log.d(TAG,"----onBackPressed---");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        startReport();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("wang","----------onPause----------------");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        Log.d("wang","----------onStop----------------");
        //exitProtocol();
        if (isUserExitAppFlag || !isStartWebRTCFlag) {
            exitAirPlayReport();
            exitProtocol();
        }
        isStartWebRTCFlag = false;
    }


    @Override
    protected void onDestroy() {

        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }

        super.onDestroy();
    }

    private SSChannel mSSChannel;
    private IMMessage imMessage;

    //黏性事件 发送了该事件之后再订阅者依然能够接收到的事件
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(final SSEEvent event) {
        mSSChannel = event.getSsChannel();
        imMessage = event.getImMessage();

        String type = event.getMsgType();
        switch (type) {
            case Constant.OFFER: {
                Log.d("wang","---OFFER");
                isStartWebRTCFlag = true;
            }
            break;
        }
    }

    private void setIntentText(Intent intent) {
        if (intent != null) {
            String wifiAccount = intent.getStringExtra("wifiAccount");
            String wifiPW = intent.getStringExtra("wifiPW");

            if (wifiAccount == null) {
                wifiAccount = "";
            }
            if (wifiPW == null) {
                wifiPW = "";
            }

            setWifiInfo(wifiAccount, wifiPW);

            String host = intent.getStringExtra("host");
            if (host == null) {
                host = "";
            }
            setWebHost(host);
        }
        try {
            Context swaiotosContext = getApplicationContext().createPackageContext("swaiotos.channel.iot",
                    Context.CONTEXT_IGNORE_SECURITY);

            String mAccessToken = ShareUtls.getInstance(swaiotosContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
            Log.d(TAG,"---mAccessToken:"+mAccessToken);
            if (TextUtils.isEmpty(mAccessToken)) {
                IOTChannelImpl.mananger.open(getApplicationContext(), "swaiotos.channel.iot", new IOTChannel.OpenCallback() {
                    @Override
                    public void onConntected(SSChannel ssChannel) {
                        Log.d(TAG,"---onConntected:"+ssChannel);
                        if (ssChannel != null) {
                            try {
                                String  accessToken = ssChannel.getDeviceManager().getAccessToken();
                                if (!TextUtils.isEmpty(accessToken))
                                    queryLinkCode(accessToken);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(String s) {
                        Log.d(TAG,"---onError:"+s);
                    }
                });
            } else {
                queryLinkCode(mAccessToken);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void queryLinkCode(final String accessToken) {
        try {
            Map<String,String> map = new HashMap<>();
            map.put(Constants.COOCAA_ACCESSTOKEN,accessToken);
            map.put(Constants.COOCAA_TIME,""+System.currentTimeMillis());
            map.put(Constants.COOCAA_SIGN, SignCore.buildRequestMysign(map, Constants.getAppKey(getApplicationContext())));

            HttpEngine.getLinkCode( Constants.getIOTServer(getApplicationContext()) + Constants.COOCAA_LINK_CODE,
                    map, new Observer<LinkCodeResp>() {
                @Override
                public void onCompleted() {
                    Log.d(TAG,"---onCompleted-");
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG,"---onError-:"+e.getMessage());
                }

                @Override
                public void onNext(LinkCodeResp linkCodeResp) {
                    Log.d(TAG,"---onNext-");
                    try {
                        if (linkCodeResp != null && linkCodeResp.getData() != null) {
                            tv_link_code.setText(String.format(getResources().getString(R.string.link_code),linkCodeResp.getData().getLinkCode()));
                            tv_link_code.setVisibility(View.VISIBLE);
                            if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                                mBindExecutorService.shutdownNow();
                                mBindExecutorService = null;
                            }

                            mBindExecutorService = Executors.newScheduledThreadPool(1);
                            mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                                @Override
                                public void run() {
                                    queryLinkCode(accessToken);
                                }
                            }, Integer.parseInt(linkCodeResp.getData().getExpiresIn()), Integer.parseInt(linkCodeResp.getData().getExpiresIn()), TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setWifiInfo(String wifiAccount, String wifiPW) {
        tv_wifi_name.setText(wifiAccount);
        tv_wifi_pw.setText(wifiPW);
        if (getNetworkType(getApplicationContext()).equals("ETHERNET")) {
            ((ViewGroup)tv_wifi_name.getParent()).setVisibility(View.GONE);
            ((ViewGroup)tv_wifi_pw.getParent()).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.wifi_account_textView)).setText(getResources().getString(R.string.wifi_account2));

        } else {
            ((ViewGroup)tv_wifi_name.getParent()).setVisibility(View.VISIBLE);
            ((ViewGroup)tv_wifi_pw.getParent()).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.wifi_account_textView)).setText(getResources().getString(R.string.wifi_account));
        }

    }

    public String getNetworkType(Context context) {
        String type = "UNKNOWN";
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {     // wifi
                type = "WIFI";
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {    //有线
                type = "ETHERNET";
            }
        }
        Log.d("yao", "getNetworkType = " + type);
        return type;
    }


    private void setWebHost(String host) {
        tv_host.setText(host);
    }


    private void setApp() {
        String app = "【酷开共享屏】";
        String text = getResources().getString(R.string.scan_qr_code);

        int start = text.indexOf(app);
        int end = app.length() + 1;

        SpannableStringBuilder style = new SpannableStringBuilder(text);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FFA700"));
        style.setSpan(colorSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_app.setText(style);
    }


    private void sendData(String content) {
        if (imMessage == null || mSSChannel == null) {
            Log.e(TAG, "sendData error...");
            return;
        }

        IMMessage.Builder builder = new IMMessage.Builder();

        String msgId = imMessage.getId();  //使用原消息体id
        builder.setId(msgId);

        Session source = imMessage.getTarget();  //发送方设置为接收方
        Session target = imMessage.getSource();  //接收方设置为发送方
        builder.setTarget(target);
        builder.setSource(source);

        String sourceClient = imMessage.getClientTarget(); //发送方设置为接收方
        //String targetClient = message.getClientSource(); //接收方设置为发送方
        String targetClient = "ss-clientID-runtime-h5-channel"; //接收方

        builder.setClientSource(sourceClient);
        builder.setClientTarget(targetClient);

        builder.setType(IMMessage.TYPE.TEXT);
        builder.putExtra(SSChannel.FORCE_SSE, "true");//强制云端

        builder.setContent(content);
        IMMessage msg = builder.build();

        IMMessageCallback callback = new IMMessageCallback() {

            @Override
            public void onStart(IMMessage imMessage) {
                Log.d(TAG, "send onStart = " + content);
            }

            @Override
            public void onProgress(IMMessage imMessage, int i) {
                Log.d(TAG, "send onProgress = " + content);
            }

            @Override
            public void onEnd(IMMessage imMessage, int code, String info) {
                Log.d(TAG, "send onEnd = " + content);
            }
        };

        try {
            mSSChannel.getIMChannel().send(msg, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startReport() {
        String id = getPackageName() + "$" + "airplay_wait";
        businessStateReport(id, "{}");
    }


    private void businessStateReport(String id, String values) {
        Intent intent = new Intent();
        intent.setAction("coocaa.intent.action.BusinessStateReportService");
        intent.setPackage("swaiotos.channel.iot");
        intent.putExtra("id", id);
        intent.putExtra("values", values);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }


    private void exitProtocol() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cmd", "exit");
        businessStateReport("", new Gson().toJson(hashMap));

        try {
            JSONObject json = new JSONObject();
            json.put("code", isUserExitAppFlag ? 1 : 2);
            json.put("type", "SIGNALING_NOTIFY");
            json.put("message", "用户退出屏幕镜像");
            String text = json.toString();
            sendData(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void exitAirPlayReport() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("stopAirPlay", true);
            jsonObject.put("code", -2);
            jsonObject.put("message", "屏幕镜像详情页面已经关闭");
            String content = jsonObject.toString();
            sendData(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
