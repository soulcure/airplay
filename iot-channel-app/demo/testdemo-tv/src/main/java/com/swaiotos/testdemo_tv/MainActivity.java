package com.swaiotos.testdemo_tv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coocaa.statemanager.view.UiUtil;
import com.coocaa.statemanager.view.countdown.TimeOutCallBack;
import com.coocaa.statemanager.view.manager.ViewManagerImpl;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Context mContext = null;
    private LinearLayout ll = null;
    private TextView tv = null;
    int code = 12312312;

    @SuppressLint("HandlerLeak")
    Handler myHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            Log.d("c-test", "handleMessage  :" + str);
            tv.setText(str);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtil.init(this);
        mContext = this;
        ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        tv = new TextView(this);
        tv.setWidth(200);
        tv.setHeight(50);
        tv.setText("统计中...");
        tv.setTextSize(35);

        Button btn2 = new Button(this);
        btn2.setText("重置");
        btn2.setHeight(50);
        btn2.setWidth(300);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.Instance.reset();
            }
        });
        ll.addView(tv);
        ll.addView(btn2);
        setContentView(ll);
        DataManager.Instance.setListener(new DataManager.ReplayListener() {
            @Override
            public void onRefresh(String str) {
                Message msg = new Message();
                msg.obj = str;
                Log.d("c-test", "onrefresh  :" + str);
                myHandle.sendMessage(msg);
            }
        });


        Button btna = new Button(this);
        btna.setText("add global window");
        LinearLayout.LayoutParams btnaParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btna, btnaParams);
        btna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().showUserGlobalWindow(MainActivity.this, "12222222122");
            }
        });

        Button btn3 = new Button(this);
        btn3.setText("remove global window");
        LinearLayout.LayoutParams btn3Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn3, btn3Params);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().dismissUserGlobalWindow();
            }
        });

        Button btn8 = new Button(this);
        btn8.setText("update text");
        LinearLayout.LayoutParams btn8Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn8, btn8Params);
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().updateUserGlobalWindow("13345656789");
            }
        });

        final Button btn4 = new Button(this);
        btn4.setText("show disconnect view");
        LinearLayout.LayoutParams btn4Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn4, btn4Params);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().showDisconnectDialog(MainActivity.this, "952700", "15348310806", new TimeOutCallBack() {
                    @Override
                    public void onFinish() {
                        Log.i(TAG, "onFinish: disconnect finish");
                    }
                });
            }
        });

        Button btn5 = new Button(this);
        btn5.setText("cancel count down");
        LinearLayout.LayoutParams btn5Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn5, btn5Params);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().cancelDisconnectDialog();
            }
        });

        final Button btn6 = new Button(this);
        btn6.setText("loading dialog");
        LinearLayout.LayoutParams btn6Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn6, btn6Params);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().showLoadingDialog(MainActivity.this, "952700", "15347310009");
            }
        });

        final Button btn7 = new Button(this);
        btn7.setText("no device connect");
        LinearLayout.LayoutParams btn7Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn7, btn7Params);
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().showNoDeviceDialog(MainActivity.this, new TimeOutCallBack() {
                    @Override
                    public void onFinish() {
                        Log.i(TAG, "onFinish: showNoDevice");
                    }
                });
            }
        });

        Button btn9 = new Button(this);
        btn9.setText("show qr");
        LinearLayout.LayoutParams btn9Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn9, btn9Params);
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code++;
                ViewManagerImpl.getSingleton().showQrCodeGlobalWindow(MainActivity.this, code + "", "");
            }
        });

        Button btn10 = new Button(this);
        btn10.setText("remove qr");
        LinearLayout.LayoutParams btn10Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.addView(btn10, btn10Params);
        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManagerImpl.getSingleton().dismissQrCodeGlobalWindow();
            }
        });
    }
}
