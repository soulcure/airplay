package com.coocaa.tvpi.module.homepager.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.signature.ObjectKey;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import swaiotos.channel.iot.ss.device.Device;

public class InteractiveGamesAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {
    public InteractiveGamesAdapter() {
        super(R.layout.item_smartscreenw52_function);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, FunctionBean functionBean) {
        holder.setText(R.id.tv_function_name, functionBean.name);
        ImageView image = holder.findView(R.id.iv_function);
        if (image != null) {
            GlideApp.with(getContext())
                    .load(functionBean.icon)
                    .centerCrop()
                    .placeholder(R.drawable.bg_gray_e6e6e6_round_6)
                    .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                    .into(image);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TvpiClickUtil.onClick(getContext(), functionBean.uri());
                submitAppletClick(functionBean.id, functionBean.name);
            }
        });
    }

    private void submitAppletClick(String appletId, String appletName) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("device_connected", device == null ? "false" : "true")
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("applet_id", appletId)
                .append("applet_name", appletName);
        LogSubmit.event("mainpage_applet_clicked", params.getParams());
    }
}
