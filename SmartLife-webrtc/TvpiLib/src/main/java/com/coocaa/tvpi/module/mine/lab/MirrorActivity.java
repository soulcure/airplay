package com.coocaa.tvpi.module.mine.lab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import swaiotos.runtime.np.NPAppletActivity;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_CAST_PHONE;
import static com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController.MIRROR_SCREEN_REQUEST_CODE;

public class MirrorActivity extends NPAppletActivity {
    private LinearLayout layout;

    private FragmentManager manager;
    private MirrorFragment fragment;

    private final static int CONTENT_ID = 10086;

    private String TAG = "MirrorActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setId(CONTENT_ID);
        setContentView(layout);
        mHeaderHandler.setHeaderVisible(true);
        mHeaderHandler.setBackgroundColor(Color.TRANSPARENT);
        mHeaderHandler.setTitle("");
        mHeaderHandler.setDarkMode(false);
        fragment = new MirrorFragment();
        manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(CONTENT_ID, fragment, "blankFragment");
        ft.commit();
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
    }

    @Override
    protected boolean isFloatHeader() {
        return true;
    }



}
