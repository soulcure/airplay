package com.coocaa.tvpi.module.mall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mall.adapter.MallDetailPicAdapter;
import com.coocaa.tvpi.module.mall.view.DetailInfoView;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpilib.R;

import static com.coocaa.tvpi.view.LoadTipsView.TYPE_LOADING;
import static com.coocaa.tvpi.view.LoadTipsView.TYPE_NODATA;

/**
 * 详情页面
 * Created by luoxi on 2020/8/27
 */
public class MallDetailActivity extends BaseActivity {
    private RecyclerView storeRecyclerView;
    private MallDetailPicAdapter storeMainAdapter;
    DetailInfoView detailInfoView;
    ProductDetailResult.DataBean detailBean;
    private LoadTipsView loadTipsView;
    public static final int DETAIL_ADDRESS = 1000;
    public static void start(Context context,String product_id) {
        Intent intent=new Intent(context,MallDetailActivity.class);
        intent.putExtra("product_id",product_id);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this,0xfff2f2f2);
        StatusBarHelper.setStatusBarLightMode(this);
        setContentView(R.layout.activity_mall_detail);
        initViews();
        getData();
    }

    private void initViews() {
        findViewById(R.id.bt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.bt_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginActivity.checkLogin(v.getContext())){return;}
                if(detailBean!=null){
                ConfirmOrderActivity.start(MallDetailActivity.this,detailInfoView.addressBean,detailBean);}
            }
        });
        loadTipsView = findViewById(R.id.my_order_loadtipsview);
        loadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData();
            }
        });
    }

    private void getData() {
        Intent intent=getIntent();
        String product_id=intent.getStringExtra("product_id");
        if(TextUtils.isEmpty(product_id)){
            Toast.makeText(this,"product_id 为空，无法获取产品详情信息！",Toast.LENGTH_SHORT).show();
            return;
        }
        loadTipsView.setVisibility(View.VISIBLE);
        loadTipsView.setLoadTipsIV(TYPE_LOADING);
        MobileRequestService.getInstance().setLoginToken(UserInfoCenter.getInstance().getAccessToken()).getDetail(new HttpSubscribe<ProductDetailResult>() {
            @Override
            public void onSuccess(ProductDetailResult result) {
                if(result!=null){
                    ProductDetailResult.DataBean data=result.getData();
                    if(data!=null){
                        detailBean=data;
                        init(data);
                        loadTipsView.setVisibility(View.GONE);
                        return;
                    }
                }

                loadTipsView.setLoadTipsIV(TYPE_NODATA);
            }

            @Override
            public void onError(HttpThrowable error) {
                loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_FAILED);
            }
        }, product_id);
    }



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode== Activity.RESULT_OK){
//            AddressResult.GetAddressBean bean=  (AddressResult.GetAddressBean) data.getSerializableExtra("address");
//            if(bean!=null){
//                if(detailInfoView!=null)detailInfoView.setAddressBean(bean);
//            }
//
//        }
//    }

    private void init(ProductDetailResult.DataBean bean) {
        detailInfoView=new DetailInfoView(this);
        detailInfoView.initInfo(bean);
        storeRecyclerView = findViewById(R.id.recyclerview);
        storeMainAdapter = new MallDetailPicAdapter();
        storeRecyclerView.setAdapter(storeMainAdapter);
        storeMainAdapter.setHeaderView(detailInfoView);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(1, RecyclerView.VERTICAL);
        storeRecyclerView.setLayoutManager(layoutManager);
        storeMainAdapter.setList(bean.getProduct_details());
    }


}
