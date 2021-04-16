package com.coocaa.smartmall.data.mobile.data;

import java.util.List;

public class OrderResult extends BaseResult {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * product_type :
         * product_icon :
         * product_name : 智能音箱
         * order_status : 5
         * order_status_msg : 未付款取消
         * product_price : 299.000000
         * product_count : 2
         * order_no : VXPZD1595398700
         * product_id : 1
         * total_price : 598.000000
         * order_id : 1
         */

        private String product_type;
        private String product_icon;
        private String product_name;
        //order_status : 1待付款 2待发货 3待收货 4已完成 5未付款取消 6已付款取消 7待发货
        private int order_status;
        private String order_status_msg;
        private String product_price;
        private int product_count;
        private String order_no;
        private int product_id;
        private String total_price;
        private int order_id;
        private PaymentInfo payment_info;

        public String getProduct_type() {
            return product_type;
        }

        public void setProduct_type(String product_type) {
            this.product_type = product_type;
        }

        public String getProduct_icon() {
            return product_icon;
        }

        public void setProduct_icon(String product_icon) {
            this.product_icon = product_icon;
        }

        public String getProduct_name() {
            return product_name;
        }

        public void setProduct_name(String product_name) {
            this.product_name = product_name;
        }

        public int getOrder_status() {
            return order_status;
        }

        public void setOrder_status(int order_status) {
            this.order_status = order_status;
        }

        public String getOrder_status_msg() {
            return order_status_msg;
        }

        public void setOrder_status_msg(String order_status_msg) {
            this.order_status_msg = order_status_msg;
        }

        public String getProduct_price() {
            return product_price;
        }

        public void setProduct_price(String product_price) {
            this.product_price = product_price;
        }

        public int getProduct_count() {
            return product_count;
        }

        public void setProduct_count(int product_count) {
            this.product_count = product_count;
        }

        public String getOrder_no() {
            return order_no;
        }

        public void setOrder_no(String order_no) {
            this.order_no = order_no;
        }

        public int getProduct_id() {
            return product_id;
        }

        public void setProduct_id(int product_id) {
            this.product_id = product_id;
        }

        public String getTotal_price() {
            return total_price;
        }

        public void setTotal_price(String total_price) {
            this.total_price = total_price;
        }

        public int getOrder_id() {
            return order_id;
        }

        public void setOrder_id(int order_id) {
            this.order_id = order_id;
        }

        public PaymentInfo getPayment_info() {
            return payment_info;
        }

        public void setPayment_info(PaymentInfo payment_info) {
            this.payment_info = payment_info;
        }
    }
}
