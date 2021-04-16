//package com.coocaa.tvpi.module.remote;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.os.Vibrator;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//
//import com.coocaa.publib.base.DialogActivity;
//import com.coocaa.publib.utils.ToastUtils;
//import com.coocaa.publib.utils.SpUtil;
//import com.coocaa.smartscreen.connect.SSConnectManager;
//import com.coocaa.smartscreen.data.app.AppModel;
//import com.coocaa.smartscreen.data.channel.AppStoreParams;
//import com.coocaa.smartscreen.utils.CmdUtil;
//import com.coocaa.tvpi.util.StatusBarHelper;
//import com.coocaa.tvpilib.R;
//import com.umeng.analytics.MobclickAgent;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static android.view.View.GONE;
//import static android.view.View.VISIBLE;
//import static com.coocaa.tvpi.common.UMengEventId.REMOTE_CONTROL_OPERATION;
//
///**
// * @ClassName RemoteActivity
// * @User heni
// * @Date 2020/4/16
// */
//public class RemoteActivity extends DialogActivity {
//    private static final String TAG = RemoteActivity.class.getSimpleName();
//    private static final long VIBRATE_DURATION = 100L;
//
//    private Context mContext;
//    private View mLayout, remoteDialogLayout;
//    private ImageView directionIV, centerIV;
//    private View powerBtn, volumeDownBtn, volumeUpBtn, moreBtn, homeBtn, backBtn, menuBtn, redBtn, greenBtn, blueBtn;
//    private RemoteTouchView remoteTouchView;
//    private RemoteMoreView remoteMoreView;
//    private View remoteKeyView;
//
//    private boolean vibrate;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mContext = this;
//        initViews();
//        initListener();
//        StatusBarHelper.translucent(this, getResources().getColor(R.color.transparent));
//        setRemoteMode(SpUtil.getInt(this, RemoteMoreView.REMOTE_MODE_KEY, 0));
//        vibrate = SpUtil.getBoolean(this, SpUtil.Keys.REMOTE_VIBRATE, true);
//
////        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_START_APP.toString();
////        AppStoreParams params = new AppStoreParams();
////        params.pkgName = "com.ccos.tvlauncher";
////        params.mainACtivity = "com.ccos.tvlauncher.HomeActivity";
////        SSConnectManager.getInstance().sendAppCmd(cmdString, params.toJson());
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        MobclickAgent.onPageStart(TAG);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        MobclickAgent.onPageEnd(TAG);
//        if(!isFinishing()) {
//            finish(); //fix ZHP-674
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
////        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_START_APP.toString();
////        AppStoreParams params = new AppStoreParams();
////        params.pkgName = "com.coocaa.dongle.launcher";
////        params.mainACtivity = "com.coocaa.dongle.launcher.home.HomeActivity";
////        SSConnectManager.getInstance().sendAppCmd(cmdString, params.toJson());
//    }
//
//    private void initViews() {
//        mLayout = LayoutInflater.from(this).inflate(R.layout.remote_layout, null);
//        RelativeLayout.LayoutParams params =
//                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT);
//        contentRl.addView(mLayout, params);
//
//        remoteDialogLayout = mLayout.findViewById(R.id.remote_dialog_layout);
//        powerBtn = mLayout.findViewById(R.id.remote_view_power_off);
//        volumeDownBtn = mLayout.findViewById(R.id.remote_view_volume_down);
//        volumeUpBtn = mLayout.findViewById(R.id.remote_view_volume_up);
//        moreBtn = mLayout.findViewById(R.id.remote_view_more);
//        centerIV = mLayout.findViewById(R.id.remote_view_center);
//        directionIV = mLayout.findViewById(R.id.remote_view_direction_iv);
//        homeBtn = mLayout.findViewById(R.id.remote_view_home);
//        backBtn = mLayout.findViewById(R.id.remote_view_back);
//        menuBtn = mLayout.findViewById(R.id.remote_view_settings);
//        redBtn = mLayout.findViewById(R.id.remote_view_color_red);
//        greenBtn = mLayout.findViewById(R.id.remote_view_color_green);
//        blueBtn = mLayout.findViewById(R.id.remote_view_color_blue);
//
//        centerIV = findViewById(R.id.remote_view_center);
//        directionIV = findViewById(R.id.remote_view_direction_iv);
//        remoteKeyView = mLayout.findViewById(R.id.remote_view_key_mode_view);
//        remoteTouchView = mLayout.findViewById(R.id.remote_view_touch_mode_view);
//        remoteTouchView.setRemoteCtrlCallback(remoteTouchCallback);
//        remoteMoreView = mLayout.findViewById(R.id.remote_view_more_view);
//        remoteMoreView.setRemoteMoreCallback(remoteMoreCallback);
//    }
//
//    private void initListener() {
//        powerBtn.setOnTouchListener(mOnTouchListener);
//        volumeDownBtn.setOnTouchListener(mOnTouchListener);
//        volumeUpBtn.setOnTouchListener(mOnTouchListener);
//        moreBtn.setOnTouchListener(mOnTouchListener);
//        homeBtn.setOnTouchListener(mOnTouchListener);
//        backBtn.setOnTouchListener(mOnTouchListener);
//        menuBtn.setOnTouchListener(mOnTouchListener);
//        redBtn.setOnTouchListener(mOnTouchListener);
//        greenBtn.setOnTouchListener(mOnTouchListener);
//        blueBtn.setOnTouchListener(mOnTouchListener);
//
//        centerIV.setOnTouchListener(mOnTouchListener);
//        directionIV.setOnTouchListener(onDirectionTouchListener);
//    }
//
//    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            int id = v.getId();
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    if (id == R.id.remote_view_power_off) {
//                        powerBtn.setBackgroundResource(R.drawable.icon_remote_power_off_touch);
//                        sendKey(KeyEvent.KEYCODE_POWER);
//                    } else if (id == R.id.remote_view_volume_down) {
//                        mLayout.findViewById(R.id.remote_view_volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_down_touch);
//                        sendKey(KeyEvent.KEYCODE_VOLUME_DOWN);
//                    } else if (id == R.id.remote_view_volume_up) {
//                        mLayout.findViewById(R.id.remote_view_volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_up_touch);
//                        sendKey(KeyEvent.KEYCODE_VOLUME_UP);
//                    } else if (id == R.id.remote_view_more) {
//                        moreBtn.setBackgroundResource(R.drawable.icon_remote_more_touch);
//                        remoteDialogLayout.setVisibility(GONE);
//                        remoteMoreView.setVisibility(VISIBLE);
//
//                        //todo
////                        moreBtn.setBackgroundResource(R.drawable.icon_remote_key_c_press);
////                        sendKey(962);
//                    } else if (id == R.id.remote_view_home) {
//                        homeBtn.setBackgroundResource(R.drawable.icon_remote_home_touch);
//                        sendKey(KeyEvent.KEYCODE_HOME);
//                    } else if (id == R.id.remote_view_back) {
//                        backBtn.setBackgroundResource(R.drawable.icon_remote_back_touch);
//                        sendKey(KeyEvent.KEYCODE_BACK);
//                    } else if (id == R.id.remote_view_settings) {
//                        menuBtn.setBackgroundResource(R.drawable.icon_remote_settings_touch);
//                        sendKey(KeyEvent.KEYCODE_MENU);
//                    } else if (id == R.id.remote_view_center) {
//                        centerIV.setBackgroundResource(R.drawable.bg_remote_btn_selected);
//                        sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
//                    } else if (id == R.id.remote_view_color_red) {
//                        redBtn.setBackgroundResource(R.drawable.icon_remote_color_red_touch);
//                    } else if (id == R.id.remote_view_color_green) {
//                        greenBtn.setBackgroundResource(R.drawable.icon_remote_color_green_touch);
//                    } else if (id == R.id.remote_view_color_blue) {
//                        blueBtn.setBackgroundResource(R.drawable.icon_remote_color_blue_touch);
//                    }
//                    playVibrate();
//                    return true;
//                case MotionEvent.ACTION_UP:
//                    if (id == R.id.remote_view_power_off) {
//                        powerBtn.setBackgroundResource(R.drawable.icon_remote_power_off);
//                    } else if (id == R.id.remote_view_volume_down) {
//                        mLayout.findViewById(R.id.remote_view_volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_up_down);
//                    } else if (id == R.id.remote_view_volume_up) {
//                        mLayout.findViewById(R.id.remote_view_volume_up_down).setBackgroundResource(R.drawable.icon_remote_volume_up_down);
//                    } else if (id == R.id.remote_view_more) {
//                        moreBtn.setBackgroundResource(R.drawable.icon_remote_more);
////                        moreBtn.setBackgroundResource(R.drawable.icon_remote_key_c);
//                    } else if (id == R.id.remote_view_home) {
//                        homeBtn.setBackgroundResource(R.drawable.icon_remote_home);
//                    } else if (id == R.id.remote_view_back) {
//                        backBtn.setBackgroundResource(R.drawable.icon_remote_back);
//                    } else if (id == R.id.remote_view_settings) {
//                        menuBtn.setBackgroundResource(R.drawable.icon_remote_settings);
//                    } else if (id == R.id.remote_view_center) {
//                        centerIV.setBackgroundResource(R.drawable.bg_remote_btn_normal);
//                    } else if (id == R.id.remote_view_color_red) {
//                        redBtn.setBackgroundResource(R.drawable.icon_remote_color_red);
//                    } else if (id == R.id.remote_view_color_green) {
//                        greenBtn.setBackgroundResource(R.drawable.icon_remote_color_green);
//                    } else if (id == R.id.remote_view_color_blue) {
//                        blueBtn.setBackgroundResource(R.drawable.icon_remote_color_blue);
//                    }
//                    return true;
//            }
//            return false;
//        }
//    };
//
//    View.OnTouchListener onDirectionTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            float x = event.getX();
//            float y = event.getY();
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    Log.d(TAG, "ACTION_DOWN: ");
//                    directionDown(x, y);
//                    playVibrate();
//                    return true;
//                case MotionEvent.ACTION_MOVE:
//                    Log.d(TAG, "ACTION_MOVE: ");
//                    return true;
//                case MotionEvent.ACTION_UP:
//                    Log.d(TAG, "ACTION_UP: ");
//                    directionIV.setBackgroundResource(R.drawable.bg_remote_direction);
//                    return true;
//            }
//            return false;
//        }
//    };
//
//    private void directionDown(float x, float y) {
//        int w = directionIV.getWidth();
//        int h = directionIV.getHeight();
//        if ((y / x) < (h / w) && (y + h * x / w) < h) {
//            Log.d(TAG, "onClick: 上");
//            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_up);
//            sendKey(KeyEvent.KEYCODE_DPAD_UP);
//        } else if ((y / x) > (h / w) && (y + h * x / w) > h) {
//            Log.d(TAG, "onClick: 下");
//            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_down);
//            sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
//        } else if ((y / x) > (h / w) && (y + h * x / w) < h) {
//            Log.d(TAG, "onClick: 左");
//            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_left);
//            sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
//        } else if ((y / x) < (h / w) && (y + h * x / w) > h) {
//            Log.d(TAG, "onClick: 右");
//            directionIV.setBackgroundResource(R.drawable.bg_remote_direction_right);
//            sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
//        }
//    }
//
//    private void setRemoteMode(int mode) {
//        if (mode == 0) {
//            remoteKeyView.setVisibility(VISIBLE);
//            remoteTouchView.setVisibility(GONE);
//        } else if (mode == 1) {
//            remoteKeyView.setVisibility(GONE);
//            remoteTouchView.setVisibility(VISIBLE);
//        }
//    }
//
//    private void playVibrate() {
//        if (vibrate) {
//            Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//            vibrator.vibrate(VIBRATE_DURATION);
//        }
//    }
//
//    RemoteTouchView.RemoteTouchCallback remoteTouchCallback =
//            new RemoteTouchView.RemoteTouchCallback() {
//                @Override
//                public void onActionDown() {
//
//                }
//
//                @Override
//                public void onActionUp() {
//
//                }
//
//                @Override
//                public void onConfirm() {
//                    Log.d(TAG, "remoteTouchCallback onConfirm: ");
//                    sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
//                }
//
//                @Override
//                public void onMoveLeft() {
//                    Log.d(TAG, "remoteTouchCallback onMoveLeft: ");
//                    sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
//                }
//
//                @Override
//                public void onMoveRight() {
//                    Log.d(TAG, "remoteTouchCallback onMoveRight: ");
//                    sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
//                }
//
//                @Override
//                public void onMoveTop() {
//                    Log.d(TAG, "remoteTouchCallback onMoveTop: ");
//                    sendKey(KeyEvent.KEYCODE_DPAD_UP);
//                }
//
//                @Override
//                public void onMoveBottom() {
//                    Log.d(TAG, "remoteTouchCallback onMoveBottom: ");
//                    sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
//                }
//            };
//
//    RemoteMoreView.RemoteMoreCallback remoteMoreCallback =
//            new RemoteMoreView.RemoteMoreCallback() {
//                @Override
//                public void onModeSelected(int mode) {
//                    remoteDialogLayout.setVisibility(VISIBLE);
//                    if (mode == 0) {
//                        remoteKeyView.setVisibility(VISIBLE);
//                        remoteTouchView.setVisibility(GONE);
//                    } else if (mode == 1) {
//                        remoteKeyView.setVisibility(GONE);
//                        remoteTouchView.setVisibility(VISIBLE);
//                    }
//                }
//
//                @Override
//                public void onOkBtnClick() {
//                    remoteDialogLayout.setVisibility(VISIBLE);
//                    remoteMoreView.setVisibility(GONE);
//                    vibrate = SpUtil.getBoolean(RemoteActivity.this, SpUtil.Keys.REMOTE_VIBRATE, true);
//                }
//            };
//
//    private void sendKey(int code) {
//        if (SSConnectManager.getInstance().isConnected()) {
//            CmdUtil.sendKey(code);
//            Map<String, String> map = new HashMap<>();
//            map.put("event", code + "");
//            MobclickAgent.onEvent(this, REMOTE_CONTROL_OPERATION, map);
//        } else {
//            ToastUtils.getInstance().showGlobalLong("请先连接设备");
//        }
//    }
//
//    private void startApp(AppModel appModel) {
////        Log.d(tag, "startApp");
//        String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_START_APP.toString();
//        AppStoreParams params = new AppStoreParams();
//        params.pkgName = appModel.pkg;
//        params.mainACtivity = appModel.mainActivity;
//        CmdUtil.sendAppCmd(cmdString, params.toJson());
//        ToastUtils.getInstance().showGlobalShort("指令已发送，请在电视端查看");
//    }
//
//}
