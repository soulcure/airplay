package com.coocaa.smartscreen.data.store;

import java.util.List;

/**
 * 商城商品详情实体
 * Created by songxing on 2020/8/5
 */
public class StoreProductDetailResp {
    private String product_id;
    private String product_name;
    private float price;
    private float discounted_price;
    private String praise_percent;
    private String product_describe;
    private String full_address;
    private String product_type;
    private String postage;
    private List<ImageResp> images;
    private List<ProductDetailsResp> product_details;


    public static class ImageResp {
        private int id_image;
        private String image_url;

        public int getIdImage() {
            return id_image;
        }

        public void setIdImage(int idImage) {
            this.id_image = idImage;
        }

        public String getImageUrl() {
            return image_url;
        }

        public void setImageUrl(String imageUrl) {
            this.image_url = imageUrl;
        }

        @Override
        public String toString() {
            return "ImageResp{" +
                    "id_image=" + id_image +
                    ", image_url='" + image_url + '\'' +
                    '}';
        }
    }

    public static class ProductDetailsResp{
        private String image_details;

        public String getImageDetails() {
            return image_details;
        }

        public void setImageDetails(String imageDetails) {
            this.image_details = imageDetails;
        }

        @Override
        public String toString() {
            return "ProductDetailsResp{" +
                    "image_details='" + image_details + '\'' +
                    '}';
        }
    }

    public String getProductId() {
        return product_id;
    }

    public void setProductId(String productId) {
        this.product_id = productId;
    }

    public String getProductName() {
        return product_name;
    }

    public void setProductName(String productName) {
        this.product_name = productName;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getDiscountedPrice() {
        return discounted_price;
    }

    public void setDiscountedPrice(float discounted_price) {
        this.discounted_price = discounted_price;
    }

    public String getPraisePercent() {
        return praise_percent;
    }

    public void setPraisePercent(String praisePercent) {
        this.praise_percent = praisePercent;
    }

    public String getProductDescribe() {
        return product_describe;
    }

    public void setProductDescribe(String productDescribe) {
        this.product_describe = productDescribe;
    }

    public String getFullAddress() {
        return full_address;
    }

    public void setFullAddress(String fullAddress) {
        this.full_address = fullAddress;
    }

    public String getProductType() {
        return product_type;
    }

    public void setProductType(String productType) {
        this.product_type = productType;
    }

    public String getPostage() {
        return postage;
    }

    public void setPostage(String postage) {
        this.postage = postage;
    }

    public List<ImageResp> getImages() {
        return images;
    }

    public void setImages(List<ImageResp> images) {
        this.images = images;
    }

    public List<ProductDetailsResp> getProductDetails() {
        return product_details;
    }

    public void setProductDetails(List<ProductDetailsResp> detailsResps) {
        this.product_details = detailsResps;
    }

    @Override
    public String toString() {
        return "StoreProductDetailResp{" +
                "product_id=" + product_id +
                ", product_name='" + product_name + '\'' +
                ", price=" + price +
                ", discounted_price=" + discounted_price +
                ", praise_percent='" + praise_percent + '\'' +
                ", product_describe='" + product_describe + '\'' +
                ", full_address='" + full_address + '\'' +
                ", product_type='" + product_type + '\'' +
                ", postage='" + postage + '\'' +
                ", images=" + images +
                ", product_details=" + product_details +
                '}';
    }


}
