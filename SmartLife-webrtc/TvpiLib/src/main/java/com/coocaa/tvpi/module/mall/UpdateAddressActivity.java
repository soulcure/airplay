package com.coocaa.tvpi.module.mall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.StringUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.module.mall.view.AddressItemView;
import com.coocaa.tvpi.module.mall.viewmodel.UpdateAddressViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

/**
 * 修改地址
 * Created by songxing on 2020/8/25
 */
public class UpdateAddressActivity extends BaseViewModelActivity<UpdateAddressViewModel> {
    private static final String TAG = UpdateAddressActivity.class.getSimpleName();

    private CommonTitleBar titleBar;
    private AddressItemView nameItem;
    private AddressItemView phoneItem;
    private AddressItemView areaItem;
    private AddressItemView addressDescItem;
    private Switch isDefaultSwitch;
    private TextView tvDeleteAddress;

    private AddressResult.GetAddressBean address;

    public static void start(Context context, AddressResult.GetAddressBean addressResp) {
        Intent starter = new Intent(context, UpdateAddressActivity.class);
        starter.putExtra("address", addressResp);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_address);
        parserIntent();
        initView();
        setListener();
    }

    private void parserIntent() {
        if (getIntent() != null) {
            address = (AddressResult.GetAddressBean) getIntent().getSerializableExtra("address");
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        nameItem = findViewById(R.id.addressItemName);
        phoneItem = findViewById(R.id.addressItemPhone);
        areaItem = findViewById(R.id.addressItemArea);
        addressDescItem = findViewById(R.id.addressItemAreaDesc);
        isDefaultSwitch = findViewById(R.id.defaultSwitch);
        tvDeleteAddress = findViewById(R.id.tvDelete);
        nameItem.setText(address.getUser_name());
        phoneItem.setText(address.getUser_phone());
        areaItem.setText(address.getArea());
        addressDescItem.setText(address.getDetailed_address());
        isDefaultSwitch.setChecked(address.getDefault_address() == 1);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    if (verifyAddress()) {
                        updateAddress();
                    }
                }
            }
        });

        tvDeleteAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }

    private void updateAddress() {
        viewModel.updateAddress(isDefaultSwitch.isChecked(), address.getAddress_id(), nameItem.getText(),
                phoneItem.getText(), areaItem.getText(), addressDescItem.getText())
                .observe(this, updateAddressObserver);
    }

    private Observer<Boolean> updateAddressObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            Log.d(TAG, "updateAddressObserver onChanged" + success);
            if (success) {
                ToastUtils.getInstance().showGlobalShort("修改成功");
                finish();
            }
        }
    };

    private void showDeleteDialog() {
        final SDialog dialog = new SDialog(this,"确定删除该收货地址吗？", R.string.cancel, R.string.delete,
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if(!l) {
                            deleteAddress();
                        }
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void deleteAddress() {
        viewModel.deleteAddress(address.getAddress_id())
                .observe(this, deleteAddressObserver);
    }

    private Observer<Boolean> deleteAddressObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            Log.d(TAG, "deleteAddressObserver onChanged: " + success);
            if (success) {
                ToastUtils.getInstance().showGlobalShort("删除成功");
                finish();
            }
        }
    };

    private boolean verifyAddress() {
        if (TextUtils.isEmpty(nameItem.getText())) {
            ToastUtils.getInstance().showGlobalShort("请填写收货人姓名");
            return false;
        }
        if (TextUtils.isEmpty(phoneItem.getText())) {
            ToastUtils.getInstance().showGlobalShort("请填写收货人手机号");
            return false;
        }
        if (!StringUtils.isMobileNO(phoneItem.getText())) {
            ToastUtils.getInstance().showGlobalShort("请输入正确的手机号");
            return false;
        }
        if (TextUtils.isEmpty(areaItem.getText())) {
            ToastUtils.getInstance().showGlobalShort("请填写所在地区");
            return false;
        }
        if (TextUtils.isEmpty(addressDescItem.getText())) {
            ToastUtils.getInstance().showGlobalShort("请填写详细地址");
            return false;
        }
        return true;
    }
}
