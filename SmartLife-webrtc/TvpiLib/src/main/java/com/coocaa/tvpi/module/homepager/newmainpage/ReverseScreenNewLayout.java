package com.coocaa.tvpi.module.homepager.newmainpage;

import android.Manifest;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.connection.ScanActivity2;
import com.coocaa.tvpi.module.homepager.adapter.bean.DeviceState;
import com.coocaa.tvpi.module.homepager.view.ReverseScreenImageView;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;

/**
 * 智屏首页同屏布局
 * Created by songxing on 2020/3/25
 */
public class ReverseScreenNewLayout extends LinearLayout {
    private static final String TAG = ReverseScreenNewLayout.class.getSimpleName();

    private View noAddDeviceRoot;
    private View offlineRoot;
    private View aiStandbyRoot;
    private View onlineImageRoot;
    private View noInternetRoot;

    private ReverseScreenImageView reverseScreenImageView;

    private boolean isWindowVisibility = false;

    private DeviceState deviceState;

    public ReverseScreenNewLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public ReverseScreenNewLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReverseScreenNewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_reverse_controller_new, this, true);
        onlineImageRoot = findViewById(R.id.layout_screen_online_imageview);
        noAddDeviceRoot = findViewById(R.id.layout_screen_no_add_device);
        offlineRoot = findViewById(R.id.layout_screen_offline);
        aiStandbyRoot = findViewById(R.id.layout_screen_ai_standby);
        noInternetRoot = findViewById(R.id.layout_screen_no_internet);
        reverseScreenImageView = onlineImageRoot.findViewById(R.id.iv_revers_screen);
        TextView tvAdd = noAddDeviceRoot.findViewById(R.id.tv_add_device);
        tvAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionsUtil.getInstance().requestPermission(getContext(), new PermissionListener() {
                    @Override
                    public void permissionGranted(String[] permission) {
                        ScanActivity2.start(getContext());
                    }
                    @Override
                    public void permissionDenied(String[] permission) {
                        ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.request_camera_permission_tips));
                    }
                }, Manifest.permission.CAMERA);
            }
        });
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
        setUIByState();
    }

    private void setUIByState() {
        noAddDeviceRoot.setVisibility(GONE);
        offlineRoot.setVisibility(GONE);
        aiStandbyRoot.setVisibility(GONE);
        onlineImageRoot.setVisibility(GONE);
        reverseScreenImageView.stopScreenshot();
        switch (deviceState) {
            case STATE_NO_ADD_DEVICE:
                noAddDeviceRoot.setVisibility(VISIBLE);
                break;
            case STATE_OFFLINE:
                offlineRoot.setVisibility(VISIBLE);
                break;
            case STATE_AI_STANDBY:
                aiStandbyRoot.setVisibility(VISIBLE);
                break;
            case STATE_ONLINE_IMAGE:
                onlineImageRoot.setVisibility(VISIBLE);
                if(isWindowVisibility) {    //可见状态下才开启截图
                    reverseScreenImageView.startScreenshot();
                }
                break;
            case STATE_NO_INTERNET:
                noInternetRoot.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        isWindowVisibility = visibility == View.VISIBLE;
        Log.d(TAG, "onWindowVisibilityChanged:" + visibility);
        if (deviceState != DeviceState.STATE_ONLINE_IMAGE) {
            return;
        }
        if (reverseScreenImageView != null) {
            if (visibility == View.VISIBLE) {
                Log.d(TAG, "onWindowVisibilityChanged: startScreenshot");
                reverseScreenImageView.startScreenshot();
            } else {
                Log.d(TAG, "onWindowVisibilityChanged: stopScreenshot");
                reverseScreenImageView.stopScreenshot();
            }
        }
    }
}
