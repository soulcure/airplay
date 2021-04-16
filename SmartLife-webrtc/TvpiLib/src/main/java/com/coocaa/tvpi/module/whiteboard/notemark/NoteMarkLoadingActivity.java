package com.coocaa.tvpi.module.whiteboard.notemark;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.coocaa.publib.base.BaseActionBarAppletActivity;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.channel.events.ScreenshotEvent;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.coocaa.whiteboard.client.WhiteBoardClientSSEvent;
import com.coocaa.whiteboard.notemark.NoteMarkClientSocket;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;
import com.coocaa.whiteboard.ui.common.notemark.NoteClientIOTChannelHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.AccountInfo;

public class NoteMarkLoadingActivity extends BaseActionBarAppletActivity implements UnVirtualInputable {

    private LinearLayout openNoteMarkLayout;
    private ProgressBar progressBar;
    private TextView tvOpenNoteMark;

    private AccountInfo accountInfo;
    private String message;
    private ScreenshotEvent screenshotEvent;
    private volatile boolean isShotSuccess;
    private volatile boolean isConnectSuccess;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "NMClient";
        setContentView(R.layout.acitvity_note_mark);
        initTitle();
        initView();
        accountInfo = getAccountInfo();
        EventBus.getDefault().register(this);
        NoteClientIOTChannelHelper.init(this);
        NoteMarkClientSocket.INSTANCE.init(this, accountInfo);
    }


    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    private void initTitle() {
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle("");
            mHeaderHandler.setDarkMode(false);
        }
    }

    private void initView() {
        openNoteMarkLayout = findViewById(R.id.layout_open_note_mark);
        progressBar = findViewById(R.id.progressbar_open_note_mark);
        tvOpenNoteMark = findViewById(R.id.tv_open_note_mark);
        openNoteMarkLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SmartApi.isDeviceConnect()) {
                    SmartApi.startConnectDevice();
                } else {
                    if (!SmartApi.isSameWifi()) {
                        SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                    } else {
                        startNoteMark();
                    }
                }
            }
        });
    }

    //打开实时批注
    private void showUnOpenNoteMark() {
        openNoteMarkLayout.setEnabled(true);
        tvOpenNoteMark.setText("打开实时批注");
        progressBar.setVisibility(View.GONE);
    }

    //正在启动实时批注
    private void showOpeningNoteMark() {
        openNoteMarkLayout.setEnabled(false);
        tvOpenNoteMark.setText("正在启动实时批注");
        progressBar.setVisibility(View.VISIBLE);
    }

    private void startNoteMark() {
        resetFlag();
        register();
        showOpeningNoteMark();
        CmdUtil.sendScreenshot();
        NoteClientIOTChannelHelper.sendStartNoteMsg(accountInfo);
        startTimeout(15000, new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: timeoutRunnable");
                unregister();
                showUnOpenNoteMark();
                ToastUtils.getInstance().showGlobalLong("启动批注超时");
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WhiteBoardClientSSEvent event) {
        Log.d(TAG, "receive onEvent : " + event);
        if (event.info == null)
            return;
        if (WhiteBoardServerSSCmd.CMD_SERVER_REPLY_START.equals(event.info.cmd)) {
            try {
                WhiteBoardServerInfo serverInfo = JSON.parseObject(event.info.content, WhiteBoardServerInfo.class);
                startConnectServer(serverInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScreenshotEvent screenshotEvent) {
        Log.d(TAG, "ScreenshotEvent: " + screenshotEvent.url + "\n"
                + screenshotEvent.msg + "\n");
        isShotSuccess = true;
        this.screenshotEvent = screenshotEvent;
        checkFinalResult();
    }

    @Override
    protected void onDestroy() {
        unregister();
        resetFlag();
        stopTimeout();
        super.onDestroy();
    }

    private void resetFlag() {
        isShotSuccess = false;
        isConnectSuccess = false;
        screenshotEvent = null;
        message = null;
    }

    private void startConnectServer(WhiteBoardServerInfo info) {
        Log.d(TAG, "startConnectServer url : " + info.url);
//        ToastUtils.getInstance().showGlobalLong("正在同步批注数据...");
        Log.d(TAG, "client start connect : " + info.url);
        NoteMarkClientSocket.INSTANCE.setCallback(callback);
        NoteMarkClientSocket.INSTANCE.start();
        NoteMarkClientSocket.INSTANCE.connect(info.url);
    }

    private final IConnectCallback callback = new IConnectCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess22");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    ToastUtils.getInstance().showGlobalLong("批注启动成功，正在连接批注...");
                }
            });
        }

        @Override
        public void onFail(String reason) {
            Log.d(TAG, "onFail22 : " + reason);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    ToastUtils.getInstance().showGlobalLong("批注连接失败，正在重新尝试...");
                }
            });
        }

        @Override
        public void onFailOnce(String reason) {
            Log.d(TAG, "onFailOnce : " + reason);
        }

        @Override
        public void onClose() {
            Log.d(TAG, "onClose22 : ");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    ToastUtils.getInstance().showGlobalLong("批注连接已关闭！");
                }
            });
        }

        @Override
        public void onMessage(String msg) {
            Log.d(TAG, "收到Server消息22 : " + msg);
            message = msg;
            isConnectSuccess = true;
            checkFinalResult();
        }
    };

    private void checkFinalResult() {
        if (isConnectSuccess && isShotSuccess) {
            try {
                WhiteBoardServerCmdInfo info = JSON.parseObject(message, WhiteBoardServerCmdInfo.class);
                NoteMarkClientSocket.INSTANCE.setInitSyncData(info);
                NoteMarkClientSocket.INSTANCE.setCallback(null);//clear
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopTimeout();
                        startUIActivity();
                        finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startUIActivity() {
        Intent intent = new Intent();
        intent.setClass(this, NoteMarkDrawActivity.class);
        if (screenshotEvent != null) {
            intent.putExtra("url", screenshotEvent.url);
        }
        startActivity(intent);
        finish();
    }

    public static AccountInfo getAccountInfo() {
        AccountInfo info = new AccountInfo();
        IUserInfo userInfo = SmartApi.getUserInfo();
        if (userInfo != null) {
            info.accessToken = userInfo.accessToken;
            info.avatar = userInfo.avatar;
            info.mobile = userInfo.mobile;
            info.open_id = userInfo.open_id;
            info.nickName = userInfo.nickName;
        }
        Log.d(TAG, "getAccountInfo()============" + info.toString());
        return info;
    }

    private void register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void unregister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
