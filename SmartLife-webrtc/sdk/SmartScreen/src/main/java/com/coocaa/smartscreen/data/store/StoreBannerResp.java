package com.coocaa.smartscreen.data.store;
/**
 * 商城首页Banner实体
 * Created by songxing on 2020/8/5
 */
public class StoreBannerResp {

    private int product_id;
    private String image_url;

    public int getProductId() {
        return product_id;
    }

    public void setProductId(int productId) {
        this.product_id = productId;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String imageUrl) {
        this.image_url = imageUrl;
    }

    @Override
    public String toString() {
        return "StoreBannerBean{" +
                "product_id=" + product_id +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}
