package com.coocaa.tvpi.module.web;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;

import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.np.NPAppletActivity;

/**
 * 拉起dongle端/TV端web页面
 * @Author: yuzhan
 */
public class SmartBrowserActivity extends NPAppletActivity {

    LinearLayout layout;
    EditText inputText;
    Button button;
    SDialog dialog;
    String clipboardContent;
    String TAG = "SmartBrowser";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        setContentView(layout);

        inputText = new EditText(this);
        inputText.setHint("请输入web页面地址");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 150;
        params.bottomMargin = 50;
        inputText.setLayoutParams(params);
        inputText.setTextSize(18);
        layout.addView(inputText);

        button = new Button(this);
        button.setTextColor(Color.BLACK);
        button.setTextSize(18);
        button.setText("确定");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validUrl(inputText.getText().toString())) {
                    ToastUtils.getInstance().showGlobalShort("请输入web页面地址");
                    return ;
                }
                if(!SmartApi.isDeviceConnect()) {
                    SmartApi.startConnectDevice();
                } else {
                    if(!SmartApi.isSameWifi()) {
                        SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
                    } else {
                        startWeb();
                    }
                }
            }
        });
        parseClipboard();

        setBackButtonVisible(false);
        setTitle("浏览器");

        Button upButton = new Button(this);
        upButton.setText("UP");
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("do", "web_control");
                jsonObject.put("cmd", "scrollY");
                jsonObject.put("extra", "down");
                SSConnectManager.getInstance().sendTextMessage(JSON.toJSONString(jsonObject), "ss-clientID-runtime-h5-channel");
            }
        });
        layout.addView(upButton);

        Button downButton = new Button(this);
        downButton.setText("DOWN");
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("do", "web_control");
                jsonObject.put("cmd", "scrollY");
                jsonObject.put("extra", "up");
                SSConnectManager.getInstance().sendTextMessage(JSON.toJSONString(jsonObject), "ss-clientID-runtime-h5-channel");
            }
        });
        layout.addView(downButton);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseClipboard();
    }

    private void parseClipboard() {
        clipboardContent = getClipboardContent();
        Log.d(TAG, "clipboardContent=" + clipboardContent);
        if(!TextUtils.isEmpty(clipboardContent) && validUrl(clipboardContent)) {
            showClipboardDialog();
        }
    }

    private void showClipboardDialog() {
        if(dialog == null) {
            dialog = new SDialog(this, "", "是否是否粘贴板中的web页面地址", "确定", "取消", new SDialog.SDialog2Listener() {
                @Override
                public void onClick(boolean l, View view) {
                    if(l) {
                        inputText.setText(clipboardContent);
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }
                }
            }, false);
        }
        dialog.show();
    }

    private boolean validUrl(String content) {
        if(content == null)
            return false;
        String lowerContent = content.toLowerCase();
        return lowerContent.startsWith("http://") || lowerContent.startsWith("https://");
    }

    private String getClipboardContent() {
        ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData data = cm.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                ClipData.Item item = data.getItemAt(0);
                if (item != null) {
                    CharSequence sequence = item.coerceToText(this);
                    if (sequence != null) {
                        return sequence.toString();
                    }
                }
            }
        }
        return null;
    }


    private void startWeb() {
        JSONObject content = new JSONObject();
        content.put("do", "launcher_browser");
        content.put("url", inputText.getText().toString());
        content.put("name", "web页面");
        content.put("pageType", "browser");

        SSConnectManager.getInstance().sendTextMessage(JSON.toJSONString(content), "ss-clientID-runtime-h5-channel");
    }
}
