package com.coocaa.tvpi.module.logcat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.tvpilib.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import swaiotos.channel.iot.utils.ThreadManager;

/**
 * Describe:
 * Created by AwenZeng on 2021/03/03
 */
public class ShowLogActivity extends BaseAppletActivity {

    private TextView mContentTv;
    private ProgressBar mProgressBar;
    private StringBuilder mStringBuilder;
    public static final String FILE_PAHT = "path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_log);
        mContentTv = findViewById(R.id.content);
        mProgressBar = findViewById(R.id.mLogProgressBar);
        String path = getIntent().getStringExtra(FILE_PAHT);
        mStringBuilder = new StringBuilder();
        showUI(path);
    }

    private void showUI(String path){
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                loadData(path);
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContentTv.setText(mStringBuilder.toString());
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }


    private void loadData(String path) {
        File file = new File(path);
        String str = null;
        try {
            InputStream is = new FileInputStream(file);
            InputStreamReader input = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(input);
            while ((str = reader.readLine()) != null) {
                mStringBuilder.append(str);
                mStringBuilder.append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
