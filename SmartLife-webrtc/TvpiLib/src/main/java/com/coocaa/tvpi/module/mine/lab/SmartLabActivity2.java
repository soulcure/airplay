package com.coocaa.tvpi.module.mine.lab;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.StatusBarHelper;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * @Author: yuzhan
 */
public class SmartLabActivity2 extends BaseActivity implements UnVirtualInputable {
    private LinearLayout layout;


    private FragmentManager manager;
    private SmartLabFragment2 fragment;

    private final static int CONTENT_ID = 10086;

    private String TAG = "SmartLabActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setId(CONTENT_ID);
        setContentView(layout);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);

        fragment = new SmartLabFragment2();
        manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(CONTENT_ID, fragment, "blankFragment");
        ft.commit();
    }
}
