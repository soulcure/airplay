package com.coocaa.tvpi.module.mall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.StringUtils;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.module.mall.view.AddressItemView;
import com.coocaa.tvpi.module.mall.viewmodel.AddAddressViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

/**
 * 添加地址
 * Created by songxing on 2020/8/25
 */
public class AddAddressActivity extends BaseViewModelActivity<AddAddressViewModel> {
    private static final String TAG = AddAddressActivity.class.getSimpleName();
    private CommonTitleBar titleBar;
    private AddressItemView nameItem;
    private AddressItemView phoneItem;
    private AddressItemView areaItem;
    private AddressItemView addressDescItem;
    private Switch isDefaultSwitch;

    public static void start(Context context) {
        Intent starter = new Intent(context, AddAddressActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        initView();
        setListener();
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        nameItem = findViewById(R.id.addressItemName);
        phoneItem = findViewById(R.id.addressItemPhone);
        areaItem = findViewById(R.id.addressItemArea);
        addressDescItem = findViewById(R.id.addressItemAreaDesc);
        isDefaultSwitch = findViewById(R.id.defaultSwitch);
        phoneItem.setInputType(InputType.TYPE_CLASS_PHONE);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    if (verifyAddress()) {
                        addAddress();
                    }
                }
            }
        });
    }

    private void addAddress() {
        viewModel.addAddress(isDefaultSwitch.isChecked(), nameItem.getText(), phoneItem.getText(),
                areaItem.getText(), addressDescItem.getText())
                .observe(this, addAddressObserver);
    }

    private Observer<String> addAddressObserver = new Observer<String>() {
        @Override
        public void onChanged(String addressId) {
            Log.d(TAG, "addAddressObserver onChanged" + addressId);
            if (!TextUtils.isEmpty(addressId)) {
                ToastUtils.getInstance().showGlobalShort("添加成功");
                Intent intent = new Intent();
                intent.putExtra("address", createAddressBean(addressId));
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    private AddressResult.GetAddressBean createAddressBean(String addressId){
        AddressResult.GetAddressBean addressBean = new AddressResult.GetAddressBean();
        addressBean.setAddress_id(addressId);
        addressBean.setDefault_address(isDefaultSwitch.isChecked() ?  1 : 0);
        addressBean.setUser_name(nameItem.getText());
        addressBean.setUser_phone(phoneItem.getText());
        addressBean.setArea(areaItem.getText());
        addressBean.setDetailed_address(addressDescItem.getText());
        return addressBean;
    }

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
