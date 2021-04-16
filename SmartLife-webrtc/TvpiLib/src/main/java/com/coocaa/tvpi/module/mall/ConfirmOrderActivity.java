package com.coocaa.tvpi.module.mall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.data.CreateOrderResult;
import com.coocaa.smartmall.data.mobile.data.PaymentResult;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.module.mall.pay.Pay;
import com.coocaa.tvpi.module.mall.pay.PayFactory;
import com.coocaa.tvpi.module.mall.pay.PayParams;
import com.coocaa.tvpi.module.mall.viewmodel.ConfirmOrderViewModel;
import com.coocaa.tvpi.util.NumParseUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;


/**
 * 确认订单界面
 * Created by songxing on 2020/8/25
 */
public class ConfirmOrderActivity extends BaseViewModelActivity<ConfirmOrderViewModel> {
    private static final String TAG = ConfirmOrderActivity.class.getSimpleName();
    private static final int REQUEST_CODE_INVOICE = 1;
    private static final int REQUEST_CODE_SELECT_ADDRESS = 2;
    private static final int REQUEST_CODE_ADD_ADDRESS = 3;

    private CommonTitleBar titleBar;
    //地址
    private RelativeLayout addressLayout;
    private TextView tvUsername;
    private TextView tvPhone;
    private TextView tvDefaultAddress;
    private TextView tvAddressDesc;
    //支付方式
    private RadioGroup payRadioGroup;
    //商品信息
    private ImageView ivProductCover;
    private TextView tvProductName;
    private TextView tvProductSku;
    private TextView tvProductQuantity;
    //发票
    private LinearLayout invoiceLayout;
    private TextView tvInvoiceType;
    //商品金额
    private TextView tvPrice;
    private TextView tvDiscounts;
    private TextView tvPostage;
    private TextView tvTotalPrice;
    private TextView tvPay;

    private AddressResult.GetAddressBean addressBean;
    private ProductDetailResult.DataBean detailBean;

    private String payType;
    private float totalPrice;


    public static void start(Context context, AddressResult.GetAddressBean addressBean,
                             ProductDetailResult.DataBean detailBean) {
        Intent starter = new Intent(context, ConfirmOrderActivity.class);
        starter.putExtra("address", addressBean);
        starter.putExtra("detail", detailBean);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);
        parserIntent();
        initView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void parserIntent() {
        if (getIntent() != null) {
            addressBean = (AddressResult.GetAddressBean) getIntent().getSerializableExtra("address");
            detailBean = (ProductDetailResult.DataBean) getIntent().getSerializableExtra("detail");
        }
    }

    private void initView() {
        titleBar = findViewById(R.id.titleBar);
        addressLayout = findViewById(R.id.addressLayout);
        tvUsername = findViewById(R.id.tvUsername);
        tvPhone = findViewById(R.id.tvPhone);
        tvDefaultAddress = findViewById(R.id.tvDefaultAddress);
        tvAddressDesc = findViewById(R.id.tvAddress);
        payRadioGroup = findViewById(R.id.radioGroup);
        ivProductCover = findViewById(R.id.ivProductCover);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductSku = findViewById(R.id.tvProductSku);
        tvProductQuantity = findViewById(R.id.tvProductCount);
        invoiceLayout = findViewById(R.id.invoiceLayout);
        tvInvoiceType = findViewById(R.id.tvInvoiceType);
        tvPrice = findViewById(R.id.tvPrice);
        tvDiscounts = findViewById(R.id.tvDiscounts);
        tvPostage = findViewById(R.id.tvFreight);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvPay = findViewById(R.id.tvStartPay);
        payType = PayFactory.PAY_TYPE_ALI;
        parserAddress(addressBean);
        setProductInfo(detailBean);
        setInvoiceInfo(detailBean.getInvoice_type());
    }

    private void setListener() {
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        payRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbAliPay) {
                    payType = PayFactory.PAY_TYPE_ALI;
                } else if (checkedId == R.id.rbWeiXinPay) {
                    payType = PayFactory.PAY_TYPE_WEIXIN;
                } else if (checkedId == R.id.rbCloudPay) {
                    payType = PayFactory.PAY_TYPE_CLOUD;
                }
            }
        });

        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressBean == null || TextUtils.isEmpty(addressBean.getAddress_id())) {
                    Intent intent = new Intent(ConfirmOrderActivity.this, AddAddressActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_ADD_ADDRESS);
                } else {
                    Intent intent = new Intent(ConfirmOrderActivity.this, AddressListActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_SELECT_ADDRESS);
                }
            }
        });

        invoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfirmOrderActivity.this, InvoiceInfoActivity.class);
                intent.putExtra("type", detailBean.getInvoice_type());
                intent.putExtra("invoiceTitle", detailBean.getInvoice_title());
                intent.putExtra("invoiceTax", detailBean.getInvoice_tax());
                startActivityForResult(intent, REQUEST_CODE_INVOICE);
            }
        });

        tvPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressBean == null || TextUtils.isEmpty(addressBean.getAddress_id())) {
                    ToastUtils.getInstance().showGlobalLong("请先添加地址信息");
                    return;
                }
                createOrder();
            }
        });
    }

    private void createOrder() {
        viewModel.createOrder(
                String.valueOf(detailBean.getProduct_id()),
                1,
                String.valueOf(totalPrice),
                addressBean.getAddress_id(),
                payType,
                detailBean.getInvoice_type(),
                detailBean.getInvoice_title(),
                detailBean.getInvoice_tax()
        ).observe(this, createOrderObserver);
    }

    private Observer<CreateOrderResult> createOrderObserver = new Observer<CreateOrderResult>() {
        @Override
        public void onChanged(CreateOrderResult result) {
            Log.d(TAG, "createOrderObserver onChanged: " + result);
            if (result == null
                    || result.getData() == null
                    || result.getData().getPayment_info() == null
                    || result.getData().getOrder_info() == null) {
                Log.d(TAG, "createOrderObserver onChanged: orderInfo or payInfo is null ");
                return;
            }
            payOrder(result);
        }
    };

    private void payOrder(CreateOrderResult result) {
        PayParams payParams = new PayParams();
        payParams.setOrderInfo(result.getData().getPayment_info().getJs_api_param());
        PayFactory.createPay(payType)
                .payOrder(ConfirmOrderActivity.this, payParams, new Pay.PayListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "payOrder onSuccess");
                        String orderNum = result.getData().getOrder_info().getOrder_number();
                        confirmPayment(orderNum);
                    }

                    @Override
                    public void onFail(String code, String message) {
                        Log.d(TAG, "payOrder onFail: " + message);
                        MyOrderActivity.start(ConfirmOrderActivity.this);
                        ToastUtils.getInstance().showGlobalShort(message);
                        finish();
                    }
                });
    }

    private void confirmPayment(String orderNum) {
        viewModel.confirmPayment(orderNum)
                .observe(this, confirmPaymentObserver);
    }

    private Observer<PaymentResult> confirmPaymentObserver = new Observer<PaymentResult>() {
        @Override
        public void onChanged(PaymentResult payMentResult) {
            Log.d(TAG, "confirmPaymentObserver onChanged: " + payMentResult);
            ToastUtils.getInstance().showGlobalShort("支付成功");
            MyOrderActivity.start(ConfirmOrderActivity.this);
            finish();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_INVOICE) {
                int invoiceType = data.getIntExtra("invoiceType", 1);
                String invoiceTitle = data.getStringExtra("invoiceTitle");
                String invoiceTax = data.getStringExtra("invoiceTax");
                detailBean.setInvoice_type(invoiceType);
                detailBean.setInvoice_title(invoiceTitle);
                detailBean.setInvoice_tax(invoiceTax);
                setInvoiceInfo(invoiceType);
            } else if (requestCode == REQUEST_CODE_SELECT_ADDRESS) {
                addressBean = (AddressResult.GetAddressBean) data.getSerializableExtra("address");
                setAddressInfo(addressBean);
            }else if(requestCode == REQUEST_CODE_ADD_ADDRESS){
                addressBean = (AddressResult.GetAddressBean) data.getSerializableExtra("address");
                setAddressInfo(addressBean);
            }
        }
    }

    private void parserAddress(AddressResult.GetAddressBean address) {
        if (address == null || TextUtils.isEmpty(address.getAddress_id())) {
            //商品详情页没有填写地址
            ToastUtils.getInstance().showGlobalLong("请先添加地址信息");
            tvUsername.setText("请填写收货地址");
            tvAddressDesc.setVisibility(View.GONE);
            tvDefaultAddress.setVisibility(View.GONE);
            return;
        }

        //商品详情页只传了地址id过来需要自己获取地址
        if (!TextUtils.isEmpty(address.getAddress_id())
                && TextUtils.isEmpty(address.getUser_name())
                && TextUtils.isEmpty(address.getUser_phone())) {
            viewModel.getAddressById(address.getAddress_id())
                    .observe(ConfirmOrderActivity.this, new Observer<AddressResult.GetAddressBean>() {
                        @Override
                        public void onChanged(AddressResult.GetAddressBean bean) {
                            Log.d(TAG, "getAddressById onChanged: " + bean);
                            //存在这个地址id
                            if (bean != null) {
                                addressBean = bean;
                                setAddressInfo(addressBean);
                            }
                        }
                    });
        }else {
            //完整的地址
            setAddressInfo(address);
        }
    }

    private void setAddressInfo(AddressResult.GetAddressBean address) {
        //商品详情页传了完整的地址信息过来
        tvUsername.setText(address.getUser_name());
        String userPhone = address.getUser_phone();
        if (userPhone.length() >= 7) {
            userPhone = address.getUser_phone().replace(userPhone.substring(3, 7), "***");
        }
        tvPhone.setText(userPhone);
        tvAddressDesc.setText(String.format("%s%s", address.getArea(), address.getDetailed_address()));
        tvDefaultAddress.setVisibility(address.getDefault_address() == 1 ? View.VISIBLE : View.GONE);
        tvAddressDesc.setVisibility(View.VISIBLE);
    }

    private void setInvoiceInfo(int invoiceType) {
        if (invoiceType == 0) {
            tvInvoiceType.setText("不开发票");
        } else if (invoiceType == 1) {
            tvInvoiceType.setText("普通发票-个人");
        } else {
            tvInvoiceType.setText("普通发票-公司");
        }
    }

    @SuppressLint("DefaultLocale")
    private void setProductInfo(ProductDetailResult.DataBean detailBean) {
        if (detailBean == null) return;
        if (detailBean.getImages() != null && !detailBean.getImages().isEmpty()) {
            String imageUrl = detailBean.getImages().get(0).getImage_url();
            GlideApp.with(this)
                    .load(imageUrl)
                    .into(ivProductCover);
        }
        tvProductName.setText(detailBean.getProduct_name());
        tvProductSku.setText(detailBean.getProduct_type());
//        int quantity = detailBean.getQuantity();
        int quantity = 1;
        float price = NumParseUtil.parseFloat(detailBean.getPrice());
        float discountsPrice = NumParseUtil.parseFloat(detailBean.getDiscounted_price());
        float postage = NumParseUtil.parseFloat(detailBean.getPostage());
        totalPrice = discountsPrice * quantity + postage;
        tvProductQuantity.setText(String.format("x%d", quantity));
        tvPrice.setText(String.format("¥%.2f", Math.abs(price) <= 1e-6 ? discountsPrice : price));
        tvDiscounts.setText(String.format("- ¥%.2f", Math.abs(price) <= 1e-6 ? 0f : (price - discountsPrice)));
        tvPostage.setText(String.format("+ ¥%.2f", postage));
        tvTotalPrice.setText(String.format("¥%.2f", totalPrice));
    }
}
