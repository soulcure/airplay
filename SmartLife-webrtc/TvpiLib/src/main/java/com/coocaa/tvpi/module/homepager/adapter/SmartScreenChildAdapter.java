package com.coocaa.tvpi.module.homepager.adapter;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodBean;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpilib.R;

import androidx.constraintlayout.widget.ConstraintLayout;

import swaiotos.channel.iot.ss.device.Device;


public class SmartScreenChildAdapter extends BaseQuickAdapter<PlayMethodBean, BaseViewHolder> {

    private FastClick fastClick;

    public SmartScreenChildAdapter() {
        super(R.layout.item_smartscreen_image);
        fastClick = new FastClick();
    }

    @Override
    protected void convert(BaseViewHolder holder, PlayMethodBean smartScreenBean) {
        ImageView imageView = holder.findView(R.id.image);
        assert imageView != null;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
        params.dimensionRatio = "h," + (smartScreenBean.scale == 0 ? 1 : 1 / smartScreenBean.scale);
        GlideApp.with(getContext())
                .load(smartScreenBean.poster)
                .centerCrop()
                .into(imageView);
        holder.setText(R.id.tvTitle, smartScreenBean.title);
        holder.setText(R.id.tvSubTitle, smartScreenBean.subTitle);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fastClick.isFaskClick()) {
                    if (smartScreenBean.action != null) {
                        TvpiClickUtil.onClick(getContext(), smartScreenBean.action.uri());
                        submitBlockClick(smartScreenBean.action.id,smartScreenBean.title,holder.getAdapterPosition());
                    }
                }
            }
        });
    }

    private void submitBlockClick(String blockId,String blockedName,int position) {
        Device device = SSConnectManager.getInstance().getDevice();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        LogParams params = LogParams.newParams()
                .append("ss_device_id", device == null ? "disconnected" : device.getLsid())
                .append("ss_device_type", device == null ? "disconnected" : device.getZpRegisterType())
                .append("account", coocaaUserInfo == null ? "not_login" : coocaaUserInfo.getOpen_id())
                .append("block_id", blockId)
                .append("block_name", blockedName)
                .append("pos_id", position+"");
        LogSubmit.event("mainpage_block_clicked", params.getParams());
    }
}
