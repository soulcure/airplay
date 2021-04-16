package com.coocaa.smartmall.data.mobile.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.coocaa.smartmall.data.api.HttpApi;
import com.coocaa.smartmall.data.api.HttpManager;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.AddAddressResult;
import com.coocaa.smartmall.data.mobile.data.AddressRequest;
import com.coocaa.smartmall.data.mobile.data.AddressResult;
import com.coocaa.smartmall.data.mobile.data.BannerResult;
import com.coocaa.smartmall.data.mobile.data.BaseResult;
import com.coocaa.smartmall.data.mobile.data.CreateOrderResult;
import com.coocaa.smartmall.data.mobile.data.LoginResult;
import com.coocaa.smartmall.data.mobile.data.OrderRequest;
import com.coocaa.smartmall.data.mobile.data.OrderResult;
import com.coocaa.smartmall.data.mobile.data.PaymentResult;
import com.coocaa.smartmall.data.mobile.data.ProductDetailResult;
import com.coocaa.smartmall.data.mobile.data.ProductRecommendResult;
import com.coocaa.smartmall.data.mobile.util.ParamsUtil;
import com.coocaa.smartmall.data.tv.data.SmartMallRequestConfig;

import java.util.Map;

import retrofit2.Call;

public class MobileRequestService extends HttpManager<IMobileRequestMethod> {
    static enum RequestType {
        LOGIN, GET_DETAIL, GET_RECOMMEND, NEW_ADDR, EDIT_ADDR,
        DELETE_ADDR, GET_ADDR, GET_BANNER, GET_ORDER, NEW_ORDER, CANCLE_ORDER, CONFIRM_ORDER, PAY_MENT
    }

    @Override
    protected Class<IMobileRequestMethod> getServiceClass() {
        return IMobileRequestMethod.class;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return SmartMallRequestConfig.getInstance().mAllDefaultHeaders;
    }

    @Override
    protected String getBaseUrl() {
        return SmartMallRequestConfig.TAB_PRODUCT_BASE_URL;
    }

    private MobileRequestService() {
    }

    private static MobileRequestService instance;

    public static Context context;
    public static void init(Context context){
        MobileRequestService.context=context;
    }

    public static MobileRequestService getInstance() {
        if (instance == null) {
            instance = new MobileRequestService();
            instance.initToken();
        }
        return instance;
    }

    //账户关联接口
    public void login(HttpSubscribe<LoginResult> subscribe, String access_token) {
        request(RequestType.LOGIN, subscribe, access_token);
    }

    //手机端商品推荐列表接口
    public void getRecommend(HttpSubscribe<ProductRecommendResult> subscribe) {
        request(RequestType.GET_RECOMMEND, subscribe);
    }

    //手机端商品推荐列表接口
    public void getRecommend(HttpSubscribe<ProductRecommendResult> subscribe, int page, int pageSize) {
        request(RequestType.GET_RECOMMEND, subscribe, page, pageSize);
    }

    //手机端商品详情接口
    public void getDetail(HttpSubscribe<ProductDetailResult> subscribe, String product_id) {
        request(RequestType.GET_DETAIL, subscribe, product_id);
    }

    //获取地址列表接口
    public void getAddress(HttpSubscribe<AddressResult> subscribe) {
        request(RequestType.GET_ADDR, subscribe);
    }

    //添加地址接口
    public void newAddress(HttpSubscribe<AddAddressResult> subscribe, AddressRequest request) {
        request(RequestType.NEW_ADDR, subscribe, request.toMap());
    }

    //修改地址接口
    public void editAddress(HttpSubscribe<AddressResult> subscribe, AddressRequest request) {
        request(RequestType.EDIT_ADDR, subscribe, request.toMap());
    }

    //删除地址接口
    public void deleteAddress(HttpSubscribe<AddressResult> subscribe, int address_id) {
        request(RequestType.DELETE_ADDR, subscribe, address_id);
    }

    //手机端banner图接口
    public void getBanner(HttpSubscribe<BannerResult> subscribe) {
        request(RequestType.GET_BANNER, subscribe);
    }

    //手机端订单列表接口
    public void getOrder(HttpSubscribe<OrderResult> subscribe) {
        request(RequestType.GET_ORDER, subscribe);
    }

    //手机端生成订单接口
    public void newOrder(HttpSubscribe<CreateOrderResult> subscribe, OrderRequest request) {
        request(RequestType.NEW_ORDER, subscribe, request.toMap());
    }

    //取消订单接口
    public void cancletOrder(HttpSubscribe<OrderResult> subscribe, int order_id) {
        request(RequestType.CANCLE_ORDER, subscribe, order_id);
    }

    //确认收货接口
    public void confirmOrder(HttpSubscribe<OrderResult> subscribe, int order_id) {
        request(RequestType.CONFIRM_ORDER, subscribe, order_id);
    }

    //支付回调确认接口
    public void payment(HttpSubscribe<PaymentResult> subscribe, String orderNum) {
        request(RequestType.PAY_MENT, subscribe, orderNum);
    }

    private void addHeader(String key,String value){
        Map<String, String> headers = getHeaders();
        if (headers != null &&!TextUtils.isEmpty(key) ) {
            headers.put(key, value);
        }
    }

    //設置用戶登錄的token
    public MobileRequestService setLoginToken(String loginToken) {
        addHeader("Access-Token",loginToken);
        if(context!=null){
            SharedPreferences sp=context.getSharedPreferences("smart_mall_config",Context.MODE_PRIVATE);
            sp.edit().putString("loginToken",loginToken).commit();
        }
        return this;
    }
    public void initToken(){
        if(context!=null){
            SharedPreferences sp=context.getSharedPreferences("smart_mall_config",Context.MODE_PRIVATE);
            String loginToken= sp.getString("loginToken","");
            addHeader("Access-Token",loginToken);
        }
    }
    public void clearLoginToken() {
        setLoginToken("");
        //添加登錄的token
    }

    private void request(RequestType requestType,final HttpSubscribe subscribe, Object... params) {
        Call call = null;
        IMobileRequestMethod method = getHttpService();
        switch (requestType) {
            case LOGIN:
                call = method.login((String) params[0]);
                break;
            case GET_DETAIL:
                call = method.getDetail((String) params[0]);
                break;
            case GET_RECOMMEND:
                int page = 1;
                int pageSize = 50;
                if (params.length >= 1) {
                    page = (int) params[0];
                }
                if (params.length >= 2) {
                    pageSize = (int) params[1];
                }
                call = method.getRecommend(page, pageSize);
                break;
            case GET_ADDR:
                call = method.getAddress();
                break;
            case EDIT_ADDR:
                call = method.editAddress(ParamsUtil.map2RequestBody((Map<String, Object>) params[0]));
                break;
            case DELETE_ADDR:
                call = method.deleteAddress((Integer) params[0]);
                break;
            case NEW_ADDR:
                call = method.newAddress(ParamsUtil.map2RequestBody((Map<String, Object>) params[0]));
                break;
            case GET_ORDER:
                call = method.getOrder();
                break;
            case NEW_ORDER:
                call = method.newOrder(ParamsUtil.map2RequestBody((Map<String, Object>) params[0]));
                break;
            case CANCLE_ORDER:
                call = method.cancleOrder((Integer) params[0]);
                break;
            case CONFIRM_ORDER:
                call = method.commitOrder((Integer) params[0]);
                break;
            case PAY_MENT:
                call = method.payMent((String) params[0]);
                break;
            case GET_BANNER:
                call = method.getBanner();
                break;
        }
        HttpApi.getInstance().request(call, new HttpSubscribe<BaseResult>() {
            @Override
            public void onSuccess(BaseResult result) {
                if(result!=null&&result.getCode()==401){
                    //需要登录
                    if(context!=null){
                        Map<String, String> headers = getHeaders();
                        String loginToken=null;
                        if (headers != null ) {
                            loginToken= headers.get("Access-Token");
                            //添加登錄的token
                        }
                        if (loginToken!= null ) {
                            MobileRequestService.getInstance().login(null,loginToken);
                        } else{
                            Intent intent=new Intent("com.coocaa.tvpi.LOGIN");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("backMainActivity", true);
                            context.startActivity(intent);
                        }
                    }
                }else {
                    if(subscribe!=null)subscribe.onSuccess(result);
                }
            }

            @Override
            public void onError(HttpThrowable error) {
                if(subscribe!=null)subscribe.onError(error);
            }
        });
    }
}
