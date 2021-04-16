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
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import swaiotos.channel.iot.ss.device.Device;


public class HeaderFunctionAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {

    private FastClick fastClick = new FastClick();

    public HeaderFunctionAdapter() {
        super(R.layout.item_function_dongle);
    }

    @Override
    protected void convert(BaseViewHolder holder, FunctionBean functionBean) {
        holder.setText(R.id.tv_function_name,functionBean.name);
        ImageView image = holder.findView(R.id.iv_function);
        GlideApp.with(getContext())
                .load(functionBean.icon)
                .centerCrop()
                .signature(new ObjectKey(SmartConstans.getBuildInfo().buildTimestamp))
                .into(image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fastClick.isFaskClick()) {
                    TvpiClickUtil.onClick(getContext(),functionBean.uri());
                    submitAppletClick(functionBean.id,functionBean.name);
                }
            }
        });
    }


    private void submitAppletClick(String appletId,String appletName) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("device_connected",device == null ? "false" : "true")
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("applet_id", appletId)
                .append("applet_name", appletName);
        LogSubmit.event("mainpage_applet_clicked", params.getParams());
    }
}
