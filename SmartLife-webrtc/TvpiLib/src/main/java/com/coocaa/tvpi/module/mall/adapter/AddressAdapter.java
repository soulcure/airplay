package com.coocaa.tvpi.module.mall.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartscreen.data.store.AddressResp;
import com.coocaa.tvpi.module.mall.UpdateAddressActivity;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

public class AddressAdapter extends BaseQuickAdapter<AddressResult.GetAddressBean, BaseViewHolder> {

    public AddressAdapter() {
        super(R.layout.item_address);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, AddressResult.GetAddressBean addressResp) {
        holder.setText(R.id.tvUsername, addressResp.getUser_name());
        String userPhone = addressResp.getUser_phone();
        if(userPhone.length() >= 7) {
            holder.setText(R.id.tvPhone, userPhone.replace(userPhone.substring(3, 7), "***"));
        }else {
            holder.setText(R.id.tvPhone,userPhone);
        }
        holder.setText(R.id.tvAddress, addressResp.getFull_address());
        holder.setVisible(R.id.tvDefaultAddress, addressResp.getDefault_address() == 1);

        holder.getView(R.id.ivArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateAddressActivity.start(getContext(),addressResp);
            }
        });
    }
}
