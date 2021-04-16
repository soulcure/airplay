package com.coocaa.swaiotos.virtualinput.laserpaint;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;

import androidx.annotation.Nullable;
import swaiotos.sensor.client.SensorClient;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.data.AccountInfo;

/**
 * 激光笔调试
 * @Author: yuzhan
 */
public class TestLaserPaintActivity extends Activity {

    private static final String TAG = "LaserPaint";

    private LinearLayout layout;
    private TextView textView;
    private SensorClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "TestSensorActivity onCreate ###");

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);
        setContentView(layout);

        textView = new TextView(this);
        textView.setText("激光笔手机端");
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setPadding(0, 20, 0, 20);
        layout.addView(textView);
    }

    private void load() {
        if(SmartApi.getUserInfo() == null) {
            SmartApi.showLoginUser();
        } else {
            if(client == null) {
                client = new SensorClient(this, new ClientBusinessInfo("client-laser-client", "client-laser-server", "激光笔", 1080-20, 1800), getAccountInfo());
                layout.addView(client.getView());
            }
        }
    }

    private AccountInfo getAccountInfo() {
        AccountInfo info = new AccountInfo();
        IUserInfo userInfo = SmartApi.getUserInfo();
        if(userInfo != null) {
            info.accessToken = userInfo.accessToken;
            info.avatar = userInfo.avatar;
            info.mobile = userInfo.mobile;
            info.open_id = userInfo.open_id;
            info.nickName = userInfo.nickName;
        }
        return info;
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
        if(client != null) {
            client.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(client != null) {
            client.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client != null) {
            client.stop();
        }
    }
}
