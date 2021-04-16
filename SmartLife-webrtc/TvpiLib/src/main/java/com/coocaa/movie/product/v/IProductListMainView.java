package com.coocaa.movie.product.v;

import com.coocaa.movie.product.m.entity.ProductAccountInfo;
import com.coocaa.movie.product.m.entity.ProductEntity;
import com.coocaa.movie.product.m.entity.ProductItem;

public interface IProductListMainView {

    void updateAccountInfo(ProductAccountInfo info);

    void updateProductList(ProductEntity productEntity);

    void setPayBtnClickListener(ClickPayBtnListener ll);

    public interface ClickPayBtnListener {
        void clickPayBtn(ProductItem productItemData);
        void clickServiceAgreement();
        void clickBack();
    }
}
