package com.coocaa.tvpi.module.mall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.OrderResult;
import com.coocaa.smartmall.data.mobile.data.PaymentResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.module.mall.adapter.OrderListAdapter;
import com.coocaa.tvpi.module.mall.pay.Pay;
import com.coocaa.tvpi.module.mall.pay.PayFactory;
import com.coocaa.tvpi.module.mall.pay.PayParams;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.tvpi.view.LoadTipsView.TYPE_FAILED;
import static com.coocaa.tvpi.view.LoadTipsView.TYPE_NODATA;

/**
 * 我的订单页面
 * Created by wuhaiyuan on 2020/8/25
 */
public class MyOrderActivity extends BaseActivity {

    private static final String TAG = MyOrderActivity.class.getSimpleName();

    private CommonTitleBar titleBar;
    private LoadTipsView loadTipsView;
    private RecyclerView orderRecyclerView;
    private OrderListAdapter orderListAdapter;


    public static void start(Context context) {
        context.startActivity(new Intent(context, MyOrderActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);

        StatusBarHelper.translucent(getWindow());
        StatusBarHelper.setStatusBarLightMode(this);

        initViews();
        getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    private void initViews() {
        titleBar = findViewById(R.id.titleBar);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                } else if (position == CommonTitleBar.ClickPosition.RIGHT) {
                    //启动客服页面
                    CustomerServiceActivity.start(MyOrderActivity.this);
                }
            }
        });

        loadTipsView = findViewById(R.id.my_order_loadtipsview);
        loadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });

        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(
                DimensUtils.dp2Px(this, 10),
                DimensUtils.dp2Px(this, 10));

        orderRecyclerView = findViewById(R.id.my_order_recyclerview);
        orderRecyclerView.addItemDecoration(decoration);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        orderListAdapter = new OrderListAdapter();
        orderRecyclerView.setAdapter(orderListAdapter);

        orderListAdapter.setOrderListCallback(new OrderListAdapter.OrderListCallback() {
            @Override
            public void onConfirmClick(OrderResult.DataBean data) {
                //order_status : 1待付款 2待发货 3待收货 4已完成 5未付款取消 6已付款取消 7待出库
                switch (data.getOrder_status()) {
                    case 1:
                        //立即付款
                        payOrder(data);
                        break;
                    case 2:
                        //提醒发货
                        break;
                    case 3:
                        //确认收货
                        confirmOrder(data.getOrder_id());
                        break;
                    case 4:
                        //申请售后
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        //待出库，现在数据提供的文字是待发货
                        break;
                }
            }

            @Override
            public void onCancelClick(OrderResult.DataBean data) {
                cancelOrder(data.getOrder_id());
            }
        });
    }

    private void getData() {
        loadTipsView.setVisibility(View.VISIBLE);
        loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        MobileRequestService.getInstance()
                .getOrder(new HttpSubscribe<OrderResult>() {
            @Override
            public void onSuccess(OrderResult result) {
                Log.d(TAG, "onSuccess: " + new Gson().toJson(result));
                if (null != result && null != result.getData() && result.getData().size() > 0) {
                    orderListAdapter.setList(result.getData());
                    loadTipsView.setVisibility(View.GONE);
                } else {
                    loadTipsView.setLoadTipsIV(TYPE_NODATA);
                }
            }

            @Override
            public void onError(HttpThrowable error) {
                Log.d(TAG, "onError: " + error.toString());
                loadTipsView.setLoadTipsIV(TYPE_FAILED);
            }
        });
    }

    private void payOrder(OrderResult.DataBean data) {
        PayParams payParams = new PayParams();
        Log.d(TAG, "payOrder: " + data.getPayment_info().getData().getJs_api_param());
        payParams.setOrderInfo(data.getPayment_info().getData().getJs_api_param());
        PayFactory.createPay(PayFactory.PAY_TYPE_ALI)
                .payOrder(this, payParams, new Pay.PayListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "payOrder onSuccess");
                        String orderNum = data.getOrder_no();
                        confirmPayment(orderNum);
                    }

                    @Override
                    public void onFail(String code, String message) {
                        Log.d(TAG, "payOrder onFail: " + message);
                        ToastUtils.getInstance().showGlobalShort(message);
                    }
                });
    }

    private void confirmPayment(String orderNum) {
        MobileRequestService.getInstance()
                .payment(new HttpSubscribe<PaymentResult>() {
                    @Override
                    public void onSuccess(PaymentResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                        if (!result.isState()) {
                            ToastUtils.getInstance().showGlobalShort(result.getMsg());
                            return;
                        }
//                        confirmOrderLiveData.setValue(result);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error);
                        ToastUtils.getInstance().showGlobalShort(error.getErrMsg());
                    }
                }, orderNum);
    }

    private void cancelOrder(int order_id) {
        MobileRequestService.getInstance()
                .cancletOrder(new HttpSubscribe<OrderResult>() {
                    @Override
                    public void onSuccess(OrderResult result) {
                        Log.d(TAG, "onSuccess: " + new Gson().toJson(result));
                        if (result.getCode() == 200) {
                            getData();
                        }
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.toString());
                    }
                }, order_id);
    }

    private void confirmOrder(int order_id) {
        MobileRequestService.getInstance()
                .confirmOrder(new HttpSubscribe<OrderResult>() {
                    @Override
                    public void onSuccess(OrderResult result) {
                        Log.d(TAG, "onSuccess: " + new Gson().toJson(result));
                        if (result.getCode() == 200) {
                            getData();
                        }
                    }

                    @Override
                    public void onError(HttpThrowable error) {

                    }
                }, order_id);
    }
}
