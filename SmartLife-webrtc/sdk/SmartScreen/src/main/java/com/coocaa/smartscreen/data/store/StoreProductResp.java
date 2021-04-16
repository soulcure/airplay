package com.coocaa.smartscreen.data.store;
/**
 * 商城商品列表实体
 * Created by songxing on 2020/8/5
 */
public class StoreProductResp {
    private int product_id;
    private float product_price;
    private float product_discount_price;
    private String product_title;
    private String image_url;

    public int getProductId() {
        return product_id;
    }

    public void setProductId(int productId) {
        this.product_id = productId;
    }

    public float getProductPrice() {
        return product_price;
    }

    public void setProductPrice(float productPrice) {
        this.product_price = productPrice;
    }

    public float getProductDiscountPrice() {
        return product_discount_price;
    }

    public void setProductDiscountPrice(float productDiscountPrice) {
        this.product_discount_price = productDiscountPrice;
    }

    public String getProductTitle() {
        return product_title;
    }

    public void setProductTitle(String productTitle) {
        this.product_title = productTitle;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String imageUrl) {
        this.image_url = imageUrl;
    }



}
