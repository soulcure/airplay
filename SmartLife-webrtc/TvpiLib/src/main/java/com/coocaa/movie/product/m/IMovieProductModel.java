package com.coocaa.movie.product.m;

import com.coocaa.movie.product.m.entity.ProductAccountInfo;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.product.m.entity.ProductItem;
import com.coocaa.movie.web.product.PayQrcodeModel;

public interface IMovieProductModel {

    public void loadAccountInfo(NCallBack<ProductAccountInfo> callBack);

    public void loadProductList(NCallBack<ProductEntity> callBack);

    public void loadOrderUrl(ProductItem productItemData, NCallBack<PayQrcodeModel> callBack);
}
