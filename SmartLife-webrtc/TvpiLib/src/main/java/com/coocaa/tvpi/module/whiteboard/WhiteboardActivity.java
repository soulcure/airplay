package com.coocaa.tvpi.module.whiteboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.publib.base.BaseActionBarAppletActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.connection.wifi.WifiConnectErrorCode;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.whiteboard.ui.WhiteBoardDrawActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.coocaa.whiteboard.client.WhieBoardClientSSCmd;
import com.coocaa.whiteboard.client.WhiteBoardClientSSEvent;
import com.coocaa.whiteboard.client.WhiteBoardClientSocket;
import com.coocaa.whiteboard.server.WhiteBoardServerCmdInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerInfo;
import com.coocaa.whiteboard.server.WhiteBoardServerSSCmd;
import com.coocaa.whiteboard.ui.common.WBClientIOTChannelHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.data.ClientCmdInfo;

/**
 * @ClassName WhiteboardActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 2021/3/4
 */
public class WhiteboardActivity extends BaseActionBarAppletActivity implements UnVirtualInputable {
    private LinearLayout btnOpenWhiteboard;
    private TextView tvOpenWhiteboard;
    private ProgressBar progressbarOpenWhiteboard;

    private LinearLayout joinWhiteboardLayout;
    private ImageView ivJoinWhiteboardAvatar;
    private TextView tvJoinWhiteboardName;
    private LinearLayout btnJoinWhiteboard;
    private TextView tvJoinWhiteboard;
    private ProgressBar progressbarJoinWhiteboard;

    private LinearLayout continueOrNewCanvasWhiteboardLayout;
    private LinearLayout btnContinueWhiteboard;
    private LinearLayout btnNewCanvasWhiteboard;
    private TextView tvContinueWhiteboard;
    private TextView tvNewCanvasWhiteboard;
    private ProgressBar progressbarContinueWhiteboard;
    private ProgressBar progressbarNewCanvasWhiteboard;

    private AccountInfo accountInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "WBClient";
        setContentView(R.layout.whiteboard_acitvity);
        initTitle();
        initView();
        initListener();
        register();
        WBClientIOTChannelHelper.init(this);
        accountInfo = getAccountInfo();
        WhiteBoardClientSocket.INSTANCE.init(this, accountInfo);
        showOpenWhiteboard();
        requestOwner();
    }



    @Override
    protected void onStop() {
        super.onStop();
        unRegister();
    }

    private void register() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void unRegister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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
        } else if (WhiteBoardServerSSCmd.CMD_SERVER_REPLY_OWNER.equals(event.info.cmd)) {
            if (event.info.content != null) {
                try {
                    JSONObject jsonObject = JSON.parseObject(event.info.content);
                    String owner = jsonObject.getString("owner");
                    AccountInfo ownerInfo = JSON.parseObject(owner, AccountInfo.class);
                    showJoinWhiteboard(ownerInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                boolean hasSavedCache = event.info.extra != null && "true".equals(event.info.extra.get("hasCache"));
                Log.d(TAG, "onEvent: hasSavedCache " + hasSavedCache);
                if (hasSavedCache) {
                    showContinueOrRecreateWhiteboard();
                }
            }
        }
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
        btnOpenWhiteboard = findViewById(R.id.layout_open_whiteboard);
        tvOpenWhiteboard = findViewById(R.id.tv_open_whiteboard);
        progressbarOpenWhiteboard = findViewById(R.id.progressbar_open_whiteboard);
        joinWhiteboardLayout = findViewById(R.id.layout_join_whiteboard);
        ivJoinWhiteboardAvatar = findViewById(R.id.image_join_whiteboard_avatar);
        tvJoinWhiteboardName = findViewById(R.id.text_join_whiteboard_name);
        btnJoinWhiteboard = findViewById(R.id.layout_btn_join_whiteboard);
        tvJoinWhiteboard = findViewById(R.id.tv_join_whiteboard);
        progressbarJoinWhiteboard = findViewById(R.id.progressbar_join_whiteboard);
        continueOrNewCanvasWhiteboardLayout = findViewById(R.id.layout_continue_or_new_canvas_whiteboard);
        btnContinueWhiteboard = findViewById(R.id.layout_continue_whiteboard);
        btnNewCanvasWhiteboard = findViewById(R.id.layout_new_canvas_whiteboard);
        tvContinueWhiteboard = findViewById(R.id.tv_continue_whiteboard);
        progressbarNewCanvasWhiteboard = findViewById(R.id.progress_new_canvas_whiteboard);
        tvNewCanvasWhiteboard = findViewById(R.id.tv_new_canvas_whiteboard);
        progressbarContinueWhiteboard = findViewById(R.id.progressbar_continue_whiteboard);
    }

    private void initListener() {
        btnOpenWhiteboard.setOnClickListener(mOnClickListener);
        btnJoinWhiteboard.setOnClickListener(mOnClickListener);
        btnContinueWhiteboard.setOnClickListener(mOnClickListener);
        btnNewCanvasWhiteboard.setOnClickListener(mOnClickListener);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick : " + v);
            if (v == btnOpenWhiteboard) {
                openWhiteBoard(false);
            } else if (v == btnJoinWhiteboard) {
                openWhiteBoard(false);
            } else if (v == btnContinueWhiteboard) {
                openWhiteBoard(false);
            } else if (v == btnNewCanvasWhiteboard) {
                openWhiteBoard(true);
            }
        }
    };

    //打开电视画板
    private void showOpenWhiteboard() {
        btnOpenWhiteboard.setVisibility(View.VISIBLE);
        joinWhiteboardLayout.setVisibility(View.GONE);
        continueOrNewCanvasWhiteboardLayout.setVisibility(View.GONE);
        btnOpenWhiteboard.setEnabled(true);
        tvOpenWhiteboard.setText("打开电视画板");
    }

    //加入
    private void showJoinWhiteboard(AccountInfo accountInfo) {
        btnOpenWhiteboard.setVisibility(View.GONE);
        joinWhiteboardLayout.setVisibility(View.VISIBLE);
        continueOrNewCanvasWhiteboardLayout.setVisibility(View.GONE);
        joinWhiteboardLayout.setEnabled(true);

        Log.d(TAG, "showOwner " + accountInfo);
        if (accountInfo == null)
            return;
        String name = accountInfo.nickName;
        if (this.accountInfo != null && TextUtils.equals(this.accountInfo.mobile, accountInfo.mobile)) {
            name = "我";
        }
        String text = name + "正在分享画板";
        tvJoinWhiteboardName.setText(text);
        GlideApp.with(this)
                .load(accountInfo.avatar)
                .error(R.drawable.whitebroard_icon_avatar)
                .into(ivJoinWhiteboardAvatar);
        tvJoinWhiteboard.setText("打开多人画板");
        tvJoinWhiteboard.setEnabled(true);
    }

    //继续编辑上次的画板，新建画板
    private void showContinueOrRecreateWhiteboard() {
        btnOpenWhiteboard.setVisibility(View.GONE);
        joinWhiteboardLayout.setVisibility(View.GONE);
        continueOrNewCanvasWhiteboardLayout.setVisibility(View.VISIBLE);
        tvContinueWhiteboard.setText("继续编辑上次的画板");
        tvNewCanvasWhiteboard.setText("新建画板（将删除原画板）");
        btnContinueWhiteboard.setEnabled(true);
        btnNewCanvasWhiteboard.setEnabled(true);
    }

    //电视画板启动中
    private void showOpeningWhiteboard(boolean newCanvas) {
        if (btnOpenWhiteboard.getVisibility() == View.VISIBLE) {
            tvOpenWhiteboard.setText("正在启动电视的画板");
            btnOpenWhiteboard.setEnabled(false);
            progressbarOpenWhiteboard.setVisibility(View.VISIBLE);
        }

        if (joinWhiteboardLayout.getVisibility() == View.VISIBLE) {
            tvJoinWhiteboard.setText("正在启动电视的画板");
            joinWhiteboardLayout.setEnabled(false);
            progressbarJoinWhiteboard.setVisibility(View.VISIBLE);
        }

        if (continueOrNewCanvasWhiteboardLayout.getVisibility() == View.VISIBLE) {
            btnContinueWhiteboard.setEnabled(false);
            btnNewCanvasWhiteboard.setEnabled(false);
            if (newCanvas) {
                tvNewCanvasWhiteboard.setText("正在启动电视的画板");
                progressbarNewCanvasWhiteboard.setVisibility(View.VISIBLE);
            } else {
                tvContinueWhiteboard.setText("正在启动电视的画板");
                progressbarContinueWhiteboard.setVisibility(View.VISIBLE);
            }
        }
    }

    //电视画板启动失败
    private void showOpenWhiteboardError() {
        if (btnOpenWhiteboard.getVisibility() == View.VISIBLE) {
            tvOpenWhiteboard.setText("打开电视画板");
            btnOpenWhiteboard.setEnabled(true);
            progressbarOpenWhiteboard.setVisibility(View.GONE);
        }

        if (joinWhiteboardLayout.getVisibility() == View.VISIBLE) {
            tvJoinWhiteboard.setText("打开多人画板");
            joinWhiteboardLayout.setEnabled(true);
            progressbarJoinWhiteboard.setVisibility(View.GONE);
        }

        if (continueOrNewCanvasWhiteboardLayout.getVisibility() == View.VISIBLE) {
            btnContinueWhiteboard.setEnabled(true);
            btnNewCanvasWhiteboard.setEnabled(true);
            progressbarNewCanvasWhiteboard.setVisibility(View.GONE);
            progressbarContinueWhiteboard.setVisibility(View.GONE);
            tvContinueWhiteboard.setText("继续编辑上次的画板");
            tvNewCanvasWhiteboard.setText("新建画板（将删除原画板）");
        }
    }



    private void openWhiteBoard(boolean newCanvas) {
        Log.d(TAG, "openWhiteBoard");
        if (!SmartApi.isDeviceConnect()) {
            SmartApi.startConnectDevice();
        } else {
            if (!SmartApi.isSameWifi()) {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            } else {
                register();
//                ToastUtils.getInstance().showGlobalLong("正在启动白板...");
                startTimeout(15000, new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: timeoutRunnable");
                        ToastUtils.getInstance().showGlobalLong("启动画板超时");
                        showOpenWhiteboardError();
                    }
                });
                showOpeningWhiteboard(newCanvas);
                sendStartWhiteBoardCmd(newCanvas);
            }
        }
    }

    private void startConnectServer(WhiteBoardServerInfo info) {
        Log.d(TAG, "startConnectServer url : " + info.url);
//        ToastUtils.getInstance().showGlobalLong("正在同步白板数据...");
        Log.d(TAG, "client start connect : " + info.url);
        WhiteBoardClientSocket.INSTANCE.setCallback(callback);
        WhiteBoardClientSocket.INSTANCE.start();
        WhiteBoardClientSocket.INSTANCE.connect(info.url);
    }

    private void requestOwner() {
        ClientCmdInfo clientCmdInfo = new ClientCmdInfo();
        clientCmdInfo.cmd = WhieBoardClientSSCmd.CMD_CLIENT_REQUEST_OWNER;
        clientCmdInfo.accountInfo = accountInfo;
        clientCmdInfo.cid = clientCmdInfo.accountInfo.mobile;
        String content = JSON.toJSONString(clientCmdInfo);
        sendToWhiteBoard(content);
    }

    private void sendStartWhiteBoardCmd(boolean newCanvas) {
        WBClientIOTChannelHelper.sendStartWhiteBoardMsg(accountInfo, newCanvas);
    }

    private void sendToWhiteBoard(String content) {
        WBClientIOTChannelHelper.sendChannelMsg(content);
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

    private IConnectCallback callback = new IConnectCallback() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    ToastUtils.getInstance().showGlobalLong("白板启动成功，正在连接白板...");
                }
            });
        }

        @Override
        public void onFail(String reason) {
            Log.d(TAG, "onFail : " + reason);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showOpenWhiteboardError();
//                    ToastUtils.getInstance().showGlobalLong("白板连接失败，正在重新尝试...");
                }
            });
        }

        @Override
        public void onFailOnce(String reason) {

        }

        @Override
        public void onClose() {
            Log.d(TAG, "onClose : ");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    ToastUtils.getInstance().showGlobalLong("白板连接已关闭！");
//                }
//            });
        }

        @Override
        public void onMessage(String msg) {
            Log.d(TAG, "收到Server socket消息： : " + msg);
            try {
                WhiteBoardServerCmdInfo info = JSON.parseObject(msg, WhiteBoardServerCmdInfo.class);
                WhiteBoardClientSocket.INSTANCE.setInitSyncData(info);
                WhiteBoardClientSocket.INSTANCE.setCallback(null);//clear
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        ToastUtils.getInstance().showGlobalLong("白板连接成功！");
                        stopTimeout();
                        startUIActivity();
                        finish();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private void startUIActivity() {
        Intent intent = new Intent();
        intent.setClass(this, WhiteBoardDrawActivity.class);
        startActivity(intent);
        finish();
    }

}
