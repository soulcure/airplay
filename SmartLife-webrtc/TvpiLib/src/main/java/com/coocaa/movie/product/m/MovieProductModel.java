package com.coocaa.movie.product.m;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.movie.product.m.entity.ProductAccountInfo;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.product.m.entity.ProductItem;
import com.coocaa.movie.util.MovieConstant;
import com.coocaa.movie.web.base.HttpExecption;
import com.coocaa.movie.web.product.BaseSourceItem;
import com.coocaa.movie.web.product.PayHttpCallback;
import com.coocaa.movie.web.product.PayHttpMethods;
import com.coocaa.movie.web.product.PayOrder;
import com.coocaa.movie.web.product.PayQrcodeModel;
import com.coocaa.movie.web.product.PaySourceModel;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MovieProductModel implements IMovieProductModel{

    PayHttpMethods payHttpMethods;
    ProductEntity curProductEntity;

    public MovieProductModel() {
        payHttpMethods = new PayHttpMethods();
    }

    @Override
    public void loadAccountInfo(NCallBack<ProductAccountInfo> callBack) {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                if(!UserInfoCenter.getInstance().isLogin()) {
                    if(callBack != null)
                        callBack.onFailed("用户未登录");
                } else {
                    CoocaaUserInfo userInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
                    if(userInfo == null) {
                        if(callBack != null)
                            callBack.onFailed("获取账户信息失败");
                        return;
                    } else {
                        payHttpMethods.getSourceList(new PayHttpCallback<PaySourceModel>() {
                                                         @Override
                                                         public void callback(PaySourceModel paySourceModel) {
                                                             ProductAccountInfo accountInfo = new ProductAccountInfo();
                                                             accountInfo.headIcon = userInfo.avatar;
                                                             accountInfo.nickName = userInfo.nick_name;
                                                             accountInfo.vipTime = "您还不是VIP";

                                                             if(paySourceModel != null && paySourceModel.getSources() != null)
                                                             {
                                                                 for (BaseSourceItem item : paySourceModel.getSources())
                                                                 {
                                                                     Log.i("ccapi","vip sourceId : " + item.getSource_id() + "; requestSourceSign = " + item.getSource_sign());
                                                                     if(MovieConstant.source_id == item.getSource_id() && item.getValid_type() == 1)
                                                                     {
                                                                         SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
                                                                         String due = sdf.format(new Date(item.getValid_scope().end * 1000L));
                                                                         accountInfo.vipTime = due + "到期";
                                                                     }
                                                                 }
                                                             }

                                                             if(callBack != null)
                                                                 callBack.onSuccess(accountInfo);
                                                         }

                                                         @Override
                                                         public void error(HttpExecption e) {
                                                             ProductAccountInfo accountInfo = new ProductAccountInfo();
                                                             accountInfo.headIcon = userInfo.avatar;
                                                             accountInfo.nickName = userInfo.nick_name;
                                                             accountInfo.vipTime = "没有获取到VIP信息";
                                                             if(callBack != null)
                                                                 callBack.onSuccess(accountInfo);
                                                         }
                                                     }, userInfo.open_id, "2", "",
                                String.valueOf(MovieConstant.source_id), "5", "", "", "", "-1", "", "");
                    }
                }
            }
        });
    }

    @Override
    public void loadProductList(NCallBack<ProductEntity> callBack) {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                CoocaaUserInfo userInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
                try {
                    payHttpMethods.getProductList(new PayHttpCallback<ProductEntity>() {
                        @Override
                        public void callback(ProductEntity productEntity) {
                            curProductEntity = productEntity;
                            if(callBack != null)
                                callBack.onSuccess(productEntity);
                        }

                        @Override
                        public void error(HttpExecption e) {
                            if(callBack != null)
                                callBack.onFailed(String.valueOf(e.getCode()));
                        }
                    }, userInfo.open_id, TextUtils.isEmpty(userInfo.open_id)?"0":"2", "",
                            "", "", String.valueOf(MovieConstant.source_id), "5", "", "", "", "", "", "");
                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onFailed("获取产品包失败");
                }
            }
        });
    }

    @Override
    public void loadOrderUrl(ProductItem productItemData, NCallBack<PayQrcodeModel> callBack) {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                CoocaaUserInfo userInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
                if(userInfo == null) {
                    if(callBack != null)
                        callBack.onFailed("请先登录再购买");
                    return;
                }
                try {

                    PayOrder payOrder = new PayOrder();
                    payOrder.product_id = String.valueOf(productItemData.getProduct_id());
                    payOrder.title = productItemData.getProduct_name();
                    payOrder.price = String.valueOf(productItemData.getDiscount_fee());
                    payOrder.count = "1";
                    if(productItemData.isSupport_other_discount() && productItemData.getDiscount_products() != null && productItemData.getDiscount_products().size() > 0) {
                        payOrder.discount_info = productItemData.getDiscount_products().get(0).discount_info;
                        payOrder.discount_price = String.valueOf(productItemData.getDiscount_products().get(0).discount_product_fee);
                    }
                    List<PayOrder> payOrders = new ArrayList<>();
                    payOrders.add(payOrder);

                    payHttpMethods.getOrderUrl(new PayHttpCallback<PayQrcodeModel>() {
                        @Override
                        public void callback(PayQrcodeModel payQrcodeModel) {
                            if(callBack != null)
                                callBack.onSuccess(payQrcodeModel);
                        }

                        @Override
                        public void error(HttpExecption e) {
                            if(callBack != null)
                                callBack.onFailed(e.getMsg());

                        }
                    }, userInfo.open_id, TextUtils.isEmpty(userInfo.open_id) ? "0" : "2", "", "", "", "",
                            curProductEntity.dmp_code,curProductEntity.policy_id, String.valueOf(curProductEntity.scheme_id),
                            "", payOrders, curProductEntity.productSource, "", productItemData.getProduct_id());

                } catch (Exception e) {
                    e.printStackTrace();
                    callBack.onFailed("获取付费订单失败");
                }
            }
        });
    }
}
