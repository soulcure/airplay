package com.coocaa.tvpi.module.live;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.base.BaseActionBarAppletActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

public class LiveActivity extends BaseActionBarAppletActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStateBarColor();
        }
        FragmentManager manager = getSupportFragmentManager();
        TVLiveFragment tvLiveFragment = new TVLiveFragment();
        tvLiveFragment.setAppletInfo(mNPAppletInfo)
                .setAppletHeaderHandler(mHeaderHandler)
                .setNetworkForceKey(getNetworkForceKey());
        manager.beginTransaction().replace(R.id.fl_container, tvLiveFragment).commit();
//        setTitle("TV直播");
        setActionBarBackgroundColor(R.color.color_white);
        initTitle();

    }

    private void initTitle() {
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle("电视频道");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setStateBarColor(){
        Window window = getWindow();
        //After LOLLIPOP not translucent status bar
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //Then call setStatusBarColor.
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.white));
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
    }
}
