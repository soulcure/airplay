package com.coocaa.tvpi.module.homepager.newmainpage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coocaa.tvpi.module.homepager.adapter.bean.DeviceState;
import com.coocaa.tvpilib.R;

/**
 * 智屏首页快捷命令UI
 * Created by songxing on 2020/3/27
 */
public class ShortcutCommandNewLayout extends LinearLayout {
    private static final String TAG = ShortcutCommandNewLayout.class.getSimpleName();

    private LinearLayout scanLayout;
    private LinearLayout mirrorLayout;
    private LinearLayout localPushLayout;
    private LinearLayout shortcutLayout;
    private TextView tvMirrorScreen;
    private ImageView ivMirrorScreen;

    private DeviceState deviceState;
    private ShortcutListener shortcutListener;

    public ShortcutCommandNewLayout(Context context) {
        this(context, null, 0);
    }

    public ShortcutCommandNewLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortcutCommandNewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public void setShortcutListener(ShortcutListener shortcutListener){
        this.shortcutListener = shortcutListener;
    }

    public void setMirrorScreenState(boolean isMirroring){
        if(isMirroring){
            ivMirrorScreen.setBackgroundResource(R.drawable.function_mirroring_screen);
            tvMirrorScreen.setText("镜像中");
        }else {
            ivMirrorScreen.setBackgroundResource(R.drawable.icon_header_mirror);
            tvMirrorScreen.setText("屏幕镜像");
        }
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_header_tool_new, this, true);
        scanLayout = findViewById(R.id.scanLayout);
        mirrorLayout = findViewById(R.id.mirrorLayout);
        localPushLayout = findViewById(R.id.pushLocalLayout);
        shortcutLayout = findViewById(R.id.shortcutLayout);
        tvMirrorScreen = findViewById(R.id.tv_mirror_screen);
        ivMirrorScreen = findViewById(R.id.iv_mirror_scree);

        //扫一扫
        scanLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shortcutListener != null){
                    shortcutListener.onScanClick();
                }
            }
        });

        //本地投屏
        localPushLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shortcutListener != null){
                    shortcutListener.onLocalPushClick();
                }
            }
        });

        //屏幕镜像
        mirrorLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shortcutListener != null){
                    shortcutListener.onMirrorScreenClick();
                }
            }
        });

        //遥控器
        shortcutLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shortcutListener != null){
                    shortcutListener.onShortcutClick();
                }
            }
        });
    }


    public interface ShortcutListener{
        void onScanClick();

        void onShortcutClick();

        void onMirrorScreenClick();

        void onLocalPushClick();
    }
}
