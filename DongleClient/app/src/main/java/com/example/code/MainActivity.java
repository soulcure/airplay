package com.example.code;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;


public class MainActivity extends Activity implements View.OnClickListener {
    public static final String TAG = "yao";
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        findViewById(R.id.btn_local).setOnClickListener(this);
        findViewById(R.id.btn_aidl).setOnClickListener(this);

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_local:
                startActivity(new Intent(this,MainLocalActivity.class));
                break;
            case R.id.btn_aidl:
                startActivity(new Intent(this,MainAidlActivity.class));
                break;
            default:
                break;
        }
    }

}