package com.coocaa.tvpi.module.remote;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpilib.R;

/**
 * @ClassName Remote2Activity
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/1
 */
public class DongleRemoteActivity extends BaseAppletActivity {
    private static final String TAG = DongleRemoteActivity.class.getSimpleName();
    private static final long VIBRATE_DURATION = 100L;

    private Context mContext;
    private ImageView directionIV, centerIV;
    private View backBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.dongle_remote_layout);

        if(mHeaderHandler != null) {
            mHeaderHandler.setBackgroundColor(Color.parseColor("#222222"));
        }
        setTitle("系统设置");

        initViews();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void exit() {
        super.exit();
        sendKey(KeyEvent.KEYCODE_HOME);
    }

    @Override
    public void onBackPressed() {
        sendKey(KeyEvent.KEYCODE_HOME);
        finish();
    }

    private void initViews() {
        centerIV = findViewById(R.id.remote_center);
        directionIV = findViewById(R.id.remote_direction_iv);
        backBtn = findViewById(R.id.remote_back);
    }

    private void initListener() {
        centerIV.setOnTouchListener(mOnTouchListener);
        directionIV.setOnTouchListener(onDirectionTouchListener);
        backBtn.setOnTouchListener(mOnTouchListener);
    }

    View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int id = v.getId();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (id == R.id.remote_back) {
                        backBtn.setBackgroundResource(R.drawable.remote_back_touch);
                        sendKey(KeyEvent.KEYCODE_BACK);
                    }else if (id == R.id.remote_center) {
                        centerIV.setBackgroundResource(R.drawable.remote_center_touch);
                        sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
                    }
                    playVibrate();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (id == R.id.remote_back) {
                        backBtn.setBackgroundResource(R.drawable.remote_back_normal);
                    }  else if (id == R.id.remote_center) {
                        centerIV.setBackgroundResource(R.drawable.remote_center_normal);
                    }
                    return true;
            }
            return false;
        }
    };

    View.OnTouchListener onDirectionTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "ACTION_DOWN: ");
                    directionDown(x, y);
                    playVibrate();
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "ACTION_UP: ");
                    directionIV.setBackgroundResource(R.drawable.remote_direction_bg);
                    return true;
            }
            return false;
        }
    };

    private void directionDown(float x, float y) {
        int w = directionIV.getWidth();
        int h = directionIV.getHeight();
        if ((y / x) < (h / w) && (y + h * x / w) < h) {
            Log.d(TAG, "onClick: 上");
            directionIV.setBackgroundResource(R.drawable.remote_direction_up);
            sendKey(KeyEvent.KEYCODE_DPAD_UP);
        } else if ((y / x) > (h / w) && (y + h * x / w) > h) {
            Log.d(TAG, "onClick: 下");
            directionIV.setBackgroundResource(R.drawable.remote_direction_down);
            sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
        } else if ((y / x) > (h / w) && (y + h * x / w) < h) {
            Log.d(TAG, "onClick: 左");
            directionIV.setBackgroundResource(R.drawable.remote_direction_left);
            sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
        } else if ((y / x) < (h / w) && (y + h * x / w) > h) {
            Log.d(TAG, "onClick: 右");
            directionIV.setBackgroundResource(R.drawable.remote_direction_right);
            sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
        }
    }


    private void sendKey(int code) {
        Log.d(TAG, "sendKey: " + SSConnectManager.getInstance().isConnected());
        if (!SSConnectManager.getInstance().isConnected()) {
            ConnectDialogActivity.start(DongleRemoteActivity.this);
            return;
        }else {
            CmdUtil.sendKey(code);
        }
    }
}
