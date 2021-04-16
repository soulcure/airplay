package com.coocaa.tvpi.module.timealbum;

import android.os.Bundle;
import android.view.View;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

/**
 * @ClassName TimeAlbumActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/14/21
 * @Version TODO (write something)
 */
public class TimeAlbumActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        setContentView(R.layout.activity_time_album);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}