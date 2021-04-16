package com.coocaa.tvpi.base;

import android.os.Bundle;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.view.CommonTitleBar;

public class BaseTitleBarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setCommonTitleBar(CommonTitleBar commonTitleBar) {
        commonTitleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }
            }
        });
    }
}
