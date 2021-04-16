package com.coocaa.smartmall.data.mobile.data;

import java.io.Serializable;
import java.util.List;

public class ProductRecommendResult extends BaseResult {

    @Override
    public String toString() {
        return "ProductRecommendResult{" +
                "code=" + code +
                ", total=" + total +
                ", state=" + state +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    /**
     * code : 200
     * total : 8
     * state : true
     * data : [{"sequence":33,"product_title":"智能开关模块","grade":18,"product_discount_price":"299.00","product_id":"25","product_price":"309.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821194142xvb34i.jpg"},{"sequence":30,"product_title":"智能LED球泡灯","grade":19,"product_discount_price":"359.00","product_id":"24","product_price":"399.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821142127w8qfnt.png"},{"sequence":27,"product_title":"智能蓝牙网关","grade":19,"product_discount_price":"199.00","product_id":"23","product_price":"209.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821135725xqb8nn.jpg"},{"sequence":25,"product_title":"Swaiot PANEL","grade":20,"product_discount_price":"1299.00","product_id":"22","product_price":"1599.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821111106w2civr.jpg"},{"sequence":15,"product_title":"三合一网关","grade":9,"product_discount_price":"220.00","product_id":"20","product_price":"259.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200807142334fh41i6.jpg"},{"sequence":14,"product_title":"三控场景开关","grade":10,"product_discount_price":"189.00","product_id":"19","product_price":"209.00","image_url":"http://update-nj.skyworth-cloud.com/nj_apk/weixin/2020072411250761ape1.jpg"}]
     * msg : 成功
     */

    private int code;
    private int total;
    private boolean state;
    private String msg;
    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        @Override
        public String toString() {
            return "DataBean{" +
                    "sequence=" + sequence +
                    ", product_title='" + product_title + '\'' +
                    ", grade=" + grade +
                    ", product_discount_price='" + product_discount_price + '\'' +
                    ", product_id='" + product_id + '\'' +
                    ", product_price='" + product_price + '\'' +
                    ", image_url='" + image_url + '\'' +
                    '}';
        }

        /**
         * sequence : 33
         * product_title : 智能开关模块
         * grade : 18
         * product_discount_price : 299.00
         * product_id : 25
         * product_price : 309.00
         * image_url : http://update-nj.skyworth-cloud.com/nj_apk/weixin/20200821194142xvb34i.jpg
         */

        private int sequence;
        private String product_title;
        private int grade;
        private String product_discount_price;
        private String product_id;
        private String product_price;
        private String image_url;

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public String getProduct_title() {
            return product_title;
        }

        public void setProduct_title(String product_title) {
            this.product_title = product_title;
        }

        public int getGrade() {
            return grade;
        }

        public void setGrade(int grade) {
            this.grade = grade;
        }

        public String getProduct_discount_price() {
            return product_discount_price;
        }

        public void setProduct_discount_price(String product_discount_price) {
            this.product_discount_price = product_discount_price;
        }

        public String getProduct_id() {
            return product_id;
        }

        public void setProduct_id(String product_id) {
            this.product_id = product_id;
        }

        public String getProduct_price() {
            return product_price;
        }

        public void setProduct_price(String product_price) {
            this.product_price = product_price;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }
    }
}
