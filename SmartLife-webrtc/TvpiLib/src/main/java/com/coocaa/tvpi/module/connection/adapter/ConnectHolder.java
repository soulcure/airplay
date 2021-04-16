package com.coocaa.tvpi.module.connection.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.tvpilib.R;
import com.makeramen.roundedimageview.RoundedImageView;

import androidx.recyclerview.widget.RecyclerView;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName DeviceHolder
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/20
 * @Version TODO (write something)
 */
public class ConnectHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ConnectHolder.class.getSimpleName();

    private static final float OFFLINE_ALPHA = 0.2f;

    private View rootView;
    private RoundedImageView imgDeviceIcon;
    private TextView tvName;
//    private TextView tvModel;
//    private TextView tvActiveId;
    private TextView tvInfo;
    private TextView tvOffline;
    private ImageView ivTick;
    private ImageView ivState;
    private ImageView ivLoading;
    private RelativeLayout btnDisconnect;
    private RelativeLayout btnUnbind;
    private RotateAnimation rotateAnimation;


    public ConnectHolder(View itemView) {
        super(itemView);

        rootView = itemView.findViewById(R.id.connect_holder_root_layout);
        imgDeviceIcon = itemView.findViewById(R.id.connect_holder_device_icon);
        tvName = itemView.findViewById(R.id.connect_holder_device_tvname);
//        tvModel = itemView.findViewById(R.id.connect_holder_device_tvmodel);
//        tvActiveId = itemView.findViewById(R.id.connect_holder_device_activeid);
        tvInfo = itemView.findViewById(R.id.connect_holder_device_tvinfo);
        tvOffline = itemView.findViewById(R.id.connect_holder_device_offline);
        ivTick = itemView.findViewById(R.id.connect_holder_device_tick);
        btnDisconnect = itemView.findViewById(R.id.connect_holder_disconnect);
        btnUnbind = itemView.findViewById(R.id.connect_holder_unbind);
        ivState = itemView.findViewById(R.id.connect_holder_device_state);
        ivLoading = itemView.findViewById(R.id.device_change_loading);
        ivLoading.setVisibility(View.GONE);
    }

    public void onBind(Device device, boolean showConnection) {
        if (null == device) {
            return;
        }

        DeviceInfo deviceInfo = device.getInfo();
        if (null != deviceInfo) {
            switch (deviceInfo.type()) {
                case TV:
                    TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                    tvName.setText(tvDeviceInfo.mNickName);
                    tvInfo.setText("激活ID " + tvDeviceInfo.activeId);
                    break;
            }
        }

        GlideApp.with(rootView.getContext())
                .load(device.getMerchantIcon())
                .placeholder(R.drawable.icon_connect_tv_normal)
                .centerCrop()
                .into(imgDeviceIcon);

        tvName.setText(SSConnectManager.getInstance().getDeviceName(device));

        Session session = SSConnectManager.getInstance().getConnectSession();

        if (device.getLsid() != null
                && session != null
                && device.getLsid().equals(session.getId())
                && SSConnectManager.getInstance().isConnected()) {
            ivTick.setVisibility(View.VISIBLE);
            btnDisconnect.setVisibility(View.VISIBLE);
        } else {
            ivTick.setVisibility(View.GONE);
            btnDisconnect.setVisibility(View.GONE);
        }

        if (showConnection) {
            if (rotateAnimation == null) {
                rotateAnimation = new RotateAnimation(0f, 359f, android.view.animation.Animation.RELATIVE_TO_SELF,
                        0.5f, android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(300);//设置动画持续时间
                LinearInterpolator lin = new LinearInterpolator();
                rotateAnimation.setInterpolator(lin);
                rotateAnimation.setRepeatCount(-1);//设置重复次数
                rotateAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                rotateAnimation.setStartOffset(10);//执行前的等待时间
            }

            ivLoading.setVisibility(View.VISIBLE);
            ivLoading.startAnimation(rotateAnimation);
        } else {
            ivLoading.setVisibility(View.GONE);
            ivLoading.clearAnimation();
        }

    }

    public void setItemBackground(int resid) {
        if (null != rootView) {
            rootView.setBackgroundResource(resid);
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        rootView.setOnClickListener(onClickListener);
    }

    public void setOnDisconnectClickListener(View.OnClickListener onClickListener) {
        btnDisconnect.setOnClickListener(onClickListener);
    }

    public void setOnUnbindClickListener(View.OnClickListener onClickListener) {
        btnUnbind.setOnClickListener(onClickListener);
    }
}
