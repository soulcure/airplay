package com.coocaa.tvpi.module.mall.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddressRequest;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.event.AddressEvent;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mall.AddressListActivity;
import com.coocaa.tvpi.util.ImageUtils;
import com.coocaa.tvpilib.R;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.listener.OnPageChangeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.coocaa.tvpi.module.mall.MallDetailActivity.DETAIL_ADDRESS;
import static com.coocaa.tvpi.module.mall.adapter.MallMainAdapter.getPriceIntStr;

public class DetailInfoView  extends RelativeLayout {
    public AddressResult.GetAddressBean addressBean;
    public DetailInfoView(Context context) {
        super(context);
        init(context);
    }

    public DetailInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DetailInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    private void init(Context context){
        View.inflate(context, R.layout.layout_mall_detail_info,this);
    }

    public void setAddressBean(AddressResult.GetAddressBean addressBean) {
        this.addressBean = addressBean;
        TextView address=findViewById(R.id.address);
        if(addressBean==null){
            if(address!=null)address.setText("请填写详细地址");
            return;
        }
        if(!TextUtils.isEmpty(addressBean.getFull_address())){
            if(address!=null)address.setText(addressBean.getFull_address());
        }else{
            if(address!=null)address.setText("请填写详细地址");
        }
    }

    public void initInfo(final ProductDetailResult.DataBean data){

        TextView title=findViewById(R.id.title);
        TextView discount_price=findViewById(R.id.discount_price);
        TextView favorable_rate=findViewById(R.id.favorable_rate);
        TextView title_desc=findViewById(R.id.title_desc);
        TextView freight_charge=findViewById(R.id.freight_charge);
        TextView type=findViewById(R.id.type);
        TextView price=findViewById(R.id.price);
        title.setText(data.getProduct_name());
        String priceStr=data.getDiscounted_price();
        if(TextUtils.isEmpty(priceStr)){
            priceStr="0";
        }
        discount_price.setText("¥"+getPriceIntStr(priceStr));
        if(!TextUtils.isEmpty(data.getPrice())){
            price.setText("¥"+getPriceIntStr(data.getPrice()));
            price.setVisibility(VISIBLE);
        } else {
            price.setVisibility(GONE);
        }
        price.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG ); //中间横线
//        favorable_rate.setText(data.getQuantity()+"%");
        title_desc.setText(data.getProduct_describe());
        freight_charge.setText(data.getPostage());
        type.setText(data.getProduct_type());
        findViewById(R.id.address_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!LoginActivity.checkLogin(v.getContext())){return;}
                Intent intent=new Intent(getContext(),AddressListActivity.class);
                intent.putExtra("needResult", true);
                ((Activity)getContext()).startActivityForResult(intent,DETAIL_ADDRESS);
            }
        });
        initBanner(data);
        getAll();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    private void initBanner(ProductDetailResult.DataBean data){
        final   TextView banner_index=findViewById(R.id.banner_index);
        Banner banner =findViewById(R.id.banner);
        banner.isAutoLoop(false);
        List<ProductDetailResult.DataBean.ImagesBean> images=data.getImages();
        final int size=images==null?0:images.size();
        banner.setAdapter(new BannerImageAdapter<ProductDetailResult.DataBean.ImagesBean>(images) {
            @Override
            public void onBindView(BannerImageHolder holder, ProductDetailResult.DataBean.ImagesBean data, int position, int size) {
                ImageUtils.load(holder.imageView,data.getImage_url());
            }
        }).addOnPageChangeListener(new OnPageChangeListener() {//addBannerLifecycleObserver(this)
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
               if(state==0&&size>1) {
                   banner_index.setText((banner.getCurrentItem())+"/"+size);
               }
            }
        });
        if(size>0){
            banner_index.setText(1+"/"+size);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddressEvent addressEvent) {
        if(addressEvent==null)return;
        if(addressEvent.type==AddressEvent.SELECT){
            setAddressBean(addressEvent.bean);
            setDefaultAddr(addressEvent.bean);
        }else if(addressEvent.type==AddressEvent.DELETE){
            if(addressBean!=null&&addressEvent.bean!=null){
                if(addressBean.getAddress_id().equals(addressEvent.bean.getAddress_id())){
                   getAll();
                }
            }
        }else if(addressEvent.type==AddressEvent.UPDATE){
            if(addressBean!=null&&addressEvent.bean!=null){
                if(addressBean.getAddress_id().equals(addressEvent.bean.getAddress_id())){
                    setAddressBean(addressEvent.bean);
                }
            }
        }else if(addressEvent.type==AddressEvent.ADD){
            if(addressBean==null&&addressEvent.bean!=null){
                setAddressBean(addressEvent.bean);
            }
        }
    }
    public void getAll(){
        if(!UserInfoCenter.getInstance().isLogin()){
            return;
        }
        MobileRequestService.getInstance().getAddress(new HttpSubscribe<AddressResult>() {
            @Override
            public void onSuccess(AddressResult result) {
                AddressResult.GetAddressBean bean=null;
                if(result!=null){
                    List<AddressResult.GetAddressBean> addrs=result.getGet_address();
                    if(addrs!=null&&addrs.size()>0){
                        for(AddressResult.GetAddressBean addr:addrs){
                            if(addr.getDefault_address()==1){
                                bean=addr;
                                break;
                            }
                        }
                            if(bean==null){
                                bean=addrs.get(0);
                                setDefaultAddr(bean);
                            }
                    }

                }
                setAddressBean(bean);
            }

            @Override
            public void onError(HttpThrowable error) {

            }
        });
    }

    private void setDefaultAddr(AddressResult.GetAddressBean defaultBean){
        if(!UserInfoCenter.getInstance().isLogin()||defaultBean==null){
            return;
        }
        if(defaultBean.getDefault_address()==1){
            return;
        }
        final AddressRequest request = new AddressRequest();//把选中的地址设置为默认地址
        request.setAddress_id(defaultBean.getAddress_id());
        request.setDefault_address(1);
        request.setUser_name(defaultBean.getUser_name());
        request.setUser_phone(defaultBean.getUser_phone());
        request.setArea(defaultBean.getArea());
        request.setDetailed_address(defaultBean.getDetailed_address());
        request.setFull_address(defaultBean.getFull_address());
        MobileRequestService.getInstance().editAddress(null,request);
    }
}
