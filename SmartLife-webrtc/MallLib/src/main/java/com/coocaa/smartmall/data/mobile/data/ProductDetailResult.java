package com.coocaa.smartmall.data.mobile.data;

import java.io.Serializable;
import java.util.List;

public class ProductDetailResult extends BaseResult {


    /**
     * data : {"quantity":99,"praise_percent":0,"postage":"包邮","product_describe":"智能灯泡，无极调光调色，5W，3000K-6500K","product_type":"白色","images":[{"id_image":57,"image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142127w8qfnt.png"}],"price":"399.00","product_id":24,"invoice_tax":"","discounted_price":"359.00","product_details":[{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821141957xvn9sl.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142013lpycak.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142023s7vrl0.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142035fn8b33.jpg"}],"full_address":"","invoice_type":0,"product_name":"智能LED球泡灯","invoice_title":""}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * quantity : 99
         * praise_percent : 0
         * postage : 包邮
         * product_describe : 智能灯泡，无极调光调色，5W，3000K-6500K
         * product_type : 白色
         * images : [{"id_image":57,"image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142127w8qfnt.png"}]
         * price : 399.00
         * product_id : 24
         * invoice_tax :
         * discounted_price : 359.00
         * product_details : [{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821141957xvn9sl.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142013lpycak.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142023s7vrl0.jpg"},{"image_details":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142035fn8b33.jpg"}]
         * full_address :
         * invoice_type : 0
         * product_name : 智能LED球泡灯
         * invoice_title :
         */

        private int quantity;
        private int praise_percent;
        private String postage;
        private String product_describe;
        private String product_type;
        private String price;
        private int product_id;
        private String invoice_tax;
        private String discounted_price;
        private String full_address;

        public String getFull_address_id() {
            return full_address_id;
        }

        public void setFull_address_id(String full_address_id) {
            this.full_address_id = full_address_id;
        }

        private String full_address_id;
        private int invoice_type;
        private String product_name;
        private String invoice_title;
        private List<ImagesBean> images;
        private List<ProductDetailsBean> product_details;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getPraise_percent() {
            return praise_percent;
        }

        public void setPraise_percent(int praise_percent) {
            this.praise_percent = praise_percent;
        }

        public String getPostage() {
            return postage;
        }

        public void setPostage(String postage) {
            this.postage = postage;
        }

        public String getProduct_describe() {
            return product_describe;
        }

        public void setProduct_describe(String product_describe) {
            this.product_describe = product_describe;
        }

        public String getProduct_type() {
            return product_type;
        }

        public void setProduct_type(String product_type) {
            this.product_type = product_type;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public int getProduct_id() {
            return product_id;
        }

        public void setProduct_id(int product_id) {
            this.product_id = product_id;
        }

        public String getInvoice_tax() {
            return invoice_tax;
        }

        public void setInvoice_tax(String invoice_tax) {
            this.invoice_tax = invoice_tax;
        }

        public String getDiscounted_price() {
            return discounted_price;
        }

        public void setDiscounted_price(String discounted_price) {
            this.discounted_price = discounted_price;
        }

        public String getFull_address() {
            return full_address;
        }

        public void setFull_address(String full_address) {
            this.full_address = full_address;
        }

        public int getInvoice_type() {
            return invoice_type;
        }

        public void setInvoice_type(int invoice_type) {
            this.invoice_type = invoice_type;
        }

        public String getProduct_name() {
            return product_name;
        }

        public void setProduct_name(String product_name) {
            this.product_name = product_name;
        }

        public String getInvoice_title() {
            return invoice_title;
        }

        public void setInvoice_title(String invoice_title) {
            this.invoice_title = invoice_title;
        }

        public List<ImagesBean> getImages() {
            return images;
        }

        public void setImages(List<ImagesBean> images) {
            this.images = images;
        }

        public List<ProductDetailsBean> getProduct_details() {
            return product_details;
        }

        public void setProduct_details(List<ProductDetailsBean> product_details) {
            this.product_details = product_details;
        }

        public static class ImagesBean implements Serializable{
            /**
             * id_image : 57
             * image_url : http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142127w8qfnt.png
             */

            private int id_image;
            private String image_url;

            public int getId_image() {
                return id_image;
            }

            public void setId_image(int id_image) {
                this.id_image = id_image;
            }

            public String getImage_url() {
                return image_url;
            }

            public void setImage_url(String image_url) {
                this.image_url = image_url;
            }
        }

        public static class ProductDetailsBean implements Serializable{
            /**
             * image_details : http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821141957xvn9sl.jpg
             */

            private String image_details;

            public String getImage_details() {
                return image_details;
            }

            public void setImage_details(String image_details) {
                this.image_details = image_details;
            }
        }
    }
}
