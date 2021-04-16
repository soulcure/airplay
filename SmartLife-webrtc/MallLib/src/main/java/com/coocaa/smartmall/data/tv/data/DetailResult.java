package com.coocaa.smartmall.data.tv.data;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;

public class DetailResult implements Serializable {
    /**
     * resultcode : 200
     * reason : success
     * product_detail : {"tags":["全屋互联","视觉盛宴","JB音箱"],"display_type":"image","price":"299.00","product_id":"1","name":"智能音箱","images":["http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718145330tk9ws1.png","http://update-nj.skyworth-cloud.com/nj_apk/weixin/202007181454023pqio6.png"]}
     */

    private String resultcode;
    private String reason;
    private Detail product_detail;



    public String getResultcode() {
        return resultcode;
    }

    public void setResultcode(String resultcode) {
        this.resultcode = resultcode;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Detail getProduct_detail() {
        return product_detail;
    }

    public void setProduct_detail(Detail product_detail) {
        this.product_detail = product_detail;
    }


    public static class Detail implements Serializable {
        public static final String DISPLAY_TYPE_IMAGE="image";
        public static final String DISPLAY_TYPE_VIDEO="video";

        /**
         * tags : ["全屋互联","视觉盛宴","JB音箱"]
         * display_type : image
         * price : 299.00
         * product_id : 1
         * name : 智能音箱
         * images : ["http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200718145330tk9ws1.png","http://update-nj.skyworth-cloud.com/nj_apk/weixin/202007181454023pqio6.png"]
         */
        @JSONField(serialize = false)
        private String image_url;//产品小图
        private String display_type;
        private String price;
        private String product_id;
        private String name;
        private List<String> tags;
        private List<String> images;//展示大图

        public String getQrcode_url() {
            return qrcode_url;
        }

        public void setQrcode_url(String qrcode_url) {
            this.qrcode_url = qrcode_url;
        }

        private List<String> videos;
        private String qrcode_url;
        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public Detail(){}
        public void setImageUrl(String product_image_url){
            image_url = product_image_url;
        }
        public String getImageUrl(){
            return image_url;
        }

        public void setName(String detail_name){
            name = detail_name;
        }
        public String getProductName(){
            return name;
        }

        public String getProductId() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public String getName() {
            return name;
        }



        public void setVideo(List<String> deatil_video){
            this.videos = deatil_video;
        }
        public List<String> getVideos(){
            return videos;
        }

        public void setDisplayType(String detail_display_type){
            this.display_type = detail_display_type;
        }
        public String getDisplayType(){
            return this.display_type;
        }

        public void setTags(List<String> detail_tags){
            this.tags = detail_tags;
        }
        public List<String> getTags(){
            return this.tags;
        }

        public void setImages(List<String> detail_images){
            this.images = detail_images;
        }
        public List<String> getImages(){
            return this.images;
        }



    }

}
