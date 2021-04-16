package com.coocaa.tvpi.module.local.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;

import com.coocaa.tvpilib.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LocalResStatesView extends RelativeLayout {
    public static final int STATE_NO_DATA = 0;
    public static final int STATE_NO_PERMISSION = 1;
    public static final int STATE_LOAD_FINISH = 2;
    public static final int STATE_LOADING = 3;

    @IntDef({STATE_NO_DATA,
            STATE_NO_PERMISSION,
            STATE_LOAD_FINISH,
            STATE_LOADING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private RelativeLayout stateLayout;
    private LinearLayout noPermissionLayout;
    private LinearLayout noDataLayout;
    private TextView tvNoData;
    private TextView tvPermissionTips;
    private ProgressBar progressBar;

    public LocalResStatesView(Context context) {
        this(context, null, 0);
    }

    public LocalResStatesView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocalResStatesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setViewLoadState(@State int type) {
        setViewLoadState(type, "");
    }

    public void setViewLoadState(@State int type, String noDataTipStr) {
        switch (type) {
            case STATE_NO_DATA:
                setVisibility(VISIBLE);
                noDataLayout.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                noPermissionLayout.setVisibility(GONE);
                if (!TextUtils.isEmpty(noDataTipStr)) {
                    tvNoData.setText(noDataTipStr);
                }
                break;
            case STATE_NO_PERMISSION:
                setVisibility(VISIBLE);
                noPermissionLayout.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                noDataLayout.setVisibility(GONE);
                if (!TextUtils.isEmpty(noDataTipStr)) {
                    tvPermissionTips.setText(noDataTipStr);
                }
                break;
            case STATE_LOAD_FINISH:
                setVisibility(GONE);
                break;
            case STATE_LOADING:
                setVisibility(VISIBLE);
                progressBar.setVisibility(VISIBLE);
                noDataLayout.setVisibility(GONE);
                noPermissionLayout.setVisibility(GONE);
                break;
            default:
                break;
        }
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_local_res_permission_and_empty, this);
        noPermissionLayout = findViewById(R.id.ll_no_permission);
        Button btOpenPermission = findViewById(R.id.bt_open_permission);
        progressBar = findViewById(R.id.progressbar);
        noDataLayout = findViewById(R.id.ll_no_data);
        tvNoData = findViewById(R.id.tv_no_data);
        stateLayout = findViewById(R.id.rl_state);
        tvPermissionTips = findViewById(R.id.tv_permission_tips);
        stateLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //消耗点击事件
            }
        });

        btOpenPermission.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    context.startActivity(intent);
                }
            }
        });
    }
}
