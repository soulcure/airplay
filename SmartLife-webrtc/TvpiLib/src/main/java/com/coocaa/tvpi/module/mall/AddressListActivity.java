package com.coocaa.tvpi.module.mall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.event.AddressEvent;
import com.coocaa.tvpi.module.mall.adapter.AddressAdapter;
import com.coocaa.tvpi.module.mall.viewmodel.AddressListViewModel;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * 收货地址列表
 * Created by songxing on 2020/8/25
 */
public class AddressListActivity extends BaseViewModelActivity<AddressListViewModel> {
    private static final String TAG = AddressListActivity.class.getSimpleName();

    private DefaultLoadStateView loadStateView;
    private CommonTitleBar titleBar;
    private RecyclerView addressRecyclerView;
    private AddressAdapter addressAdapter;

    private boolean needResult = true;

    public static void start(Context context) {
        Intent starter = new Intent(context, AddressListActivity.class);
        starter.putExtra("needResult", false);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        parserIntent();
        initView();
        setListener();
//        getAddressList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAddressList();
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parserIntent() {
        if (getIntent() != null) {
            needResult = getIntent().getBooleanExtra("needResult", true);
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        loadStateView = findViewById(R.id.loadStateView);
        addressRecyclerView = findViewById(R.id.rvAddress);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        addressAdapter = new AddressAdapter();
        addressRecyclerView.setLayoutManager(manager);
        addressAdapter.setEmptyView(getEmptyView());
        addressRecyclerView.setAdapter(addressAdapter);
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    AddAddressActivity.start(AddressListActivity.this);
                }
            }
        });

        addressAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (needResult) {   //从选择地址过来，点击item直接选中
                    EventBus.getDefault().post(new AddressEvent(addressAdapter.getData().get(position),AddressEvent.SELECT));
                    Intent intent = new Intent();
                    intent.putExtra("address", addressAdapter.getData().get(position));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }else {     //不是从选择地址过来点击item跳转到地址详情
                    UpdateAddressActivity.start(AddressListActivity.this,addressAdapter.getData().get(position));
                }
            }
        });
    }

    private void getAddressList() {
        viewModel.getAddressList().observe(this, getAddressObserver);
    }

    private Observer<List<AddressResult.GetAddressBean>> getAddressObserver = new Observer<List<AddressResult.GetAddressBean>>() {
        @Override
        public void onChanged(List<AddressResult.GetAddressBean> getAddressBeans) {
            Log.d(TAG, "getAddressObserver onChanged: " + getAddressBeans);
            addressAdapter.setList(getAddressBeans);
        }
    };

    private View getEmptyView() {
        return getLayoutInflater().inflate(R.layout.empty_default, addressRecyclerView, false);
    }
}
