package com.coocaa.tvpi.module.mine;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import androidx.fragment.app.FragmentManager;

/**
 * @author chenaojun
 */
public class MineActivity extends BaseActivity implements UnVirtualInputable {

    private ImageView imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        initView();
        initListener();
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.container,new MineFragment()).commit();
    }


    private void initView() {
        imgBack = findViewById(R.id.back_img);
    }

    private void initListener() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}