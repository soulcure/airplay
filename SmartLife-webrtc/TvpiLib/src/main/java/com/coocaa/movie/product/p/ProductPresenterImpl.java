package com.coocaa.movie.product.p;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.coocaa.movie.MovieWebViewActivity;
import com.coocaa.movie.product.m.IMovieProductModel;
import com.coocaa.movie.product.m.MovieProductModel;
import com.coocaa.movie.product.m.NCallBack;
import com.coocaa.movie.product.m.entity.ProductAccountInfo;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.product.m.entity.ProductItem;
import com.coocaa.movie.product.v.IProductListMainView;
import com.coocaa.movie.product.v.ProductListMainView;
import com.coocaa.movie.util.MovieConstant;
import com.coocaa.movie.web.product.PayQrcodeModel;

import java.util.Iterator;

public class ProductPresenterImpl implements IProductPresenter{

    IProductListMainView productListMainView;
    IMovieProductModel productModel;
    Activity activity;

    public ProductPresenterImpl(ProductListMainView productListMainView, Activity activity) {
        this.productListMainView = productListMainView;
        this.activity = activity;
        this.productListMainView.setPayBtnClickListener(payBtnListener);
        productModel = new MovieProductModel();
    }

    IProductListMainView.ClickPayBtnListener payBtnListener = new IProductListMainView.ClickPayBtnListener() {
        @Override
        public void clickPayBtn(ProductItem productItemData) {
            productModel.loadOrderUrl(productItemData, new NCallBack<PayQrcodeModel>() {
                @Override
                public void onSuccess(PayQrcodeModel payQrcodeModel) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            String url = payQrcodeModel.getUrl().replace("api-business.skysrt.com", "beta-pay.coocaa.com");// https://beta-pay.coocaa.com/v1/qrcode/login.html?qrcode_id=de0b931d55508a04714627ac89e542cd);
                            Intent intent = new Intent("android.intent.action.VIEW");
                            Uri uri = Uri.parse(payQrcodeModel.getUrl());
                            intent.setData(uri);
//                            intent.putExtra("url", payQrcodeModel.getUrl());
                            activity.startActivity(intent);
                        }
                    });

                }

                @Override
                public void onFailed(String s) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "获取订单失败："+s, Toast.LENGTH_LONG).show();
                        }
                    });

                }
            });
        }

        @Override
        public void clickServiceAgreement() {
            String serviceAgreementUrl = MovieConstant.PAY_DOMAIN+"phoneapp/server/";
            if("qq".equals(MovieConstant.getSource()))
                serviceAgreementUrl+="tencent.html";
            else if("iqiyi".equals(MovieConstant.getSource()))
                serviceAgreementUrl+="iqiyi.html";
            else
                serviceAgreementUrl+="tencent.html";
            Intent intent = new Intent(activity, MovieWebViewActivity.class);
            intent.putExtra("url", serviceAgreementUrl);
            activity.startActivity(intent);
        }

        @Override
        public void clickBack() {
            activity.finish();
        }
    };

    @Override
    public void prepareData() {
        productModel.loadAccountInfo(new NCallBack<ProductAccountInfo>() {
            @Override
            public void onSuccess(ProductAccountInfo info) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        productListMainView.updateAccountInfo(info);
                    }
                });

            }

            @Override
            public void onFailed(String s) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProductAccountInfo info = new ProductAccountInfo();
                        info.nickName = s;
                        productListMainView.updateAccountInfo(info);
                    }
                });

            }
        });

        productModel.loadProductList(new NCallBack<ProductEntity>() {
            @Override
            public void onSuccess(ProductEntity productEntity) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if("qq".equals(MovieConstant.getSource()))
                            productEntity.productSource = "超级影视VIP";
                        else if("iqiyi".equals(MovieConstant.getSource()))
                            productEntity.productSource = "奇异果VIP";
                        else
                            productEntity.productSource ="影视VIP";
                        if(productEntity.products != null && productEntity.products.size() > 0) {
                            Iterator<ProductItem> it = productEntity.products.iterator();
                            while (it.hasNext()) {
                                ProductItem item = it.next();
                                if(item != null && item.getProduct_level() == 7) {
                                    it.remove();
                                }
                            }
                        }
                        productListMainView.updateProductList(productEntity);
                    }
                });
            }

            @Override
            public void onFailed(String s) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "获取产品包失败"+s, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
