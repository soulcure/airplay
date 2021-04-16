package com.coocaa.smartmall.data.mobile.data;


public class CreateOrderResult extends BaseResult {

    private CreateOrderBean data;

    public CreateOrderBean getData() {
        return data;
    }

    public void setData(CreateOrderBean data) {
        this.data = data;
    }

    public static class CreateOrderBean{

        private OrderBean order_info;
        private PayInfoBean payment_info;

        public OrderBean getOrder_info() {
            return order_info;
        }

        public void setOrder_info(OrderBean order_info) {
            this.order_info = order_info;
        }

        public PayInfoBean getPayment_info() {
            return payment_info;
        }

        public void setPayment_info(PayInfoBean payment_info) {
            this.payment_info = payment_info;
        }

        public static class OrderBean {
            private int current_state;
            private int id_order;
            private String id_customer;
            private String payment_type;
            private String total_paid;
            private String total_paid_product;
            private String total_paid_carrier;
            private String total_paid_carrier_wholesale;
            private String total_products_num;
            private String total_products_weight;
            private String order_number;
            private String province;
            private String city;
            private String area;
            private String address;
            private String username;
            private String phone;
            private String payment_info;
            private String payment_succ_info;
            private String invoice_tax;
            private String invoice_title;
            private String invoice_url;
            private String invoice_type;
            private String pay_date;
            private String carrier_date;
            private String invoice_date;
            private String remark;
            private String done_date;
            private String checkout_date;
            private String pay_cancel_date;
            private String unpay_cancel_date;
            private String date_add;
            private String date_upd;

            public int getCurrent_state() {
                return current_state;
            }

            public void setCurrent_state(int current_state) {
                this.current_state = current_state;
            }

            public int getId_order() {
                return id_order;
            }

            public void setId_order(int id_order) {
                this.id_order = id_order;
            }

            public String getId_customer() {
                return id_customer;
            }

            public void setId_customer(String id_customer) {
                this.id_customer = id_customer;
            }

            public String getPayment_type() {
                return payment_type;
            }

            public void setPayment_type(String payment_type) {
                this.payment_type = payment_type;
            }

            public String getTotal_paid() {
                return total_paid;
            }

            public void setTotal_paid(String total_paid) {
                this.total_paid = total_paid;
            }

            public String getTotal_paid_product() {
                return total_paid_product;
            }

            public void setTotal_paid_product(String total_paid_product) {
                this.total_paid_product = total_paid_product;
            }

            public String getTotal_paid_carrier() {
                return total_paid_carrier;
            }

            public void setTotal_paid_carrier(String total_paid_carrier) {
                this.total_paid_carrier = total_paid_carrier;
            }

            public String getTotal_paid_carrier_wholesale() {
                return total_paid_carrier_wholesale;
            }

            public void setTotal_paid_carrier_wholesale(String total_paid_carrier_wholesale) {
                this.total_paid_carrier_wholesale = total_paid_carrier_wholesale;
            }

            public String getTotal_products_num() {
                return total_products_num;
            }

            public void setTotal_products_num(String total_products_num) {
                this.total_products_num = total_products_num;
            }

            public String getTotal_products_weight() {
                return total_products_weight;
            }

            public void setTotal_products_weight(String total_products_weight) {
                this.total_products_weight = total_products_weight;
            }

            public String getOrder_number() {
                return order_number;
            }

            public void setOrder_number(String order_number) {
                this.order_number = order_number;
            }

            public String getProvince() {
                return province;
            }

            public void setProvince(String province) {
                this.province = province;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getArea() {
                return area;
            }

            public void setArea(String area) {
                this.area = area;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public String getPayment_info() {
                return payment_info;
            }

            public void setPayment_info(String payment_info) {
                this.payment_info = payment_info;
            }

            public String getPayment_succ_info() {
                return payment_succ_info;
            }

            public void setPayment_succ_info(String payment_succ_info) {
                this.payment_succ_info = payment_succ_info;
            }

            public String getInvoice_tax() {
                return invoice_tax;
            }

            public void setInvoice_tax(String invoice_tax) {
                this.invoice_tax = invoice_tax;
            }

            public String getInvoice_title() {
                return invoice_title;
            }

            public void setInvoice_title(String invoice_title) {
                this.invoice_title = invoice_title;
            }

            public String getInvoice_url() {
                return invoice_url;
            }

            public void setInvoice_url(String invoice_url) {
                this.invoice_url = invoice_url;
            }

            public String getInvoice_type() {
                return invoice_type;
            }

            public void setInvoice_type(String invoice_type) {
                this.invoice_type = invoice_type;
            }

            public String getPay_date() {
                return pay_date;
            }

            public void setPay_date(String pay_date) {
                this.pay_date = pay_date;
            }

            public String getCarrier_date() {
                return carrier_date;
            }

            public void setCarrier_date(String carrier_date) {
                this.carrier_date = carrier_date;
            }

            public String getInvoice_date() {
                return invoice_date;
            }

            public void setInvoice_date(String invoice_date) {
                this.invoice_date = invoice_date;
            }

            public String getRemark() {
                return remark;
            }

            public void setRemark(String remark) {
                this.remark = remark;
            }

            public String getDone_date() {
                return done_date;
            }

            public void setDone_date(String done_date) {
                this.done_date = done_date;
            }

            public String getCheckout_date() {
                return checkout_date;
            }

            public void setCheckout_date(String checkout_date) {
                this.checkout_date = checkout_date;
            }

            public String getPay_cancel_date() {
                return pay_cancel_date;
            }

            public void setPay_cancel_date(String pay_cancel_date) {
                this.pay_cancel_date = pay_cancel_date;
            }

            public String getUnpay_cancel_date() {
                return unpay_cancel_date;
            }

            public void setUnpay_cancel_date(String unpay_cancel_date) {
                this.unpay_cancel_date = unpay_cancel_date;
            }

            public String getDate_add() {
                return date_add;
            }

            public void setDate_add(String date_add) {
                this.date_add = date_add;
            }

            public String getDate_upd() {
                return date_upd;
            }

            public void setDate_upd(String date_upd) {
                this.date_upd = date_upd;
            }

            @Override
            public String toString() {
                return "OrderBean{" +
                        "current_state=" + current_state +
                        ", id_order='" + id_order + '\'' +
                        ", id_customer='" + id_customer + '\'' +
                        ", payment_type='" + payment_type + '\'' +
                        ", total_paid='" + total_paid + '\'' +
                        ", total_paid_product='" + total_paid_product + '\'' +
                        ", total_paid_carrier='" + total_paid_carrier + '\'' +
                        ", total_paid_carrier_wholesale='" + total_paid_carrier_wholesale + '\'' +
                        ", total_products_num='" + total_products_num + '\'' +
                        ", total_products_weight='" + total_products_weight + '\'' +
                        ", order_number='" + order_number + '\'' +
                        ", province='" + province + '\'' +
                        ", city='" + city + '\'' +
                        ", area='" + area + '\'' +
                        ", address='" + address + '\'' +
                        ", username='" + username + '\'' +
                        ", phone='" + phone + '\'' +
                        ", payment_info='" + payment_info + '\'' +
                        ", payment_succ_info='" + payment_succ_info + '\'' +
                        ", invoice_tax='" + invoice_tax + '\'' +
                        ", invoice_title='" + invoice_title + '\'' +
                        ", invoice_url='" + invoice_url + '\'' +
                        ", invoice_type='" + invoice_type + '\'' +
                        ", pay_date='" + pay_date + '\'' +
                        ", carrier_date='" + carrier_date + '\'' +
                        ", invoice_date='" + invoice_date + '\'' +
                        ", remark='" + remark + '\'' +
                        ", done_date='" + done_date + '\'' +
                        ", checkout_date='" + checkout_date + '\'' +
                        ", pay_cancel_date='" + pay_cancel_date + '\'' +
                        ", unpay_cancel_date='" + unpay_cancel_date + '\'' +
                        ", date_add='" + date_add + '\'' +
                        ", date_upd='" + date_upd + '\'' +
                        '}';
            }
        }

        public static class PayInfoBean{
            private String sign;
            private String sign_type;
            private String random_str;
            private String js_api_param;

            public String getSign() {
                return sign;
            }

            public void setSign(String sign) {
                this.sign = sign;
            }

            public String getSign_type() {
                return sign_type;
            }

            public void setSign_type(String sign_type) {
                this.sign_type = sign_type;
            }

            public String getRandom_str() {
                return random_str;
            }

            public void setRandom_str(String random_str) {
                this.random_str = random_str;
            }

            public String getJs_api_param() {
                return js_api_param;
            }

            public void setJs_api_param(String js_api_param) {
                this.js_api_param = js_api_param;
            }

            @Override
            public String toString() {
                return "PayInfoBean{" +
                        "sign='" + sign + '\'' +
                        ", sign_type='" + sign_type + '\'' +
                        ", random_str='" + random_str + '\'' +
                        ", js_api_param='" + js_api_param + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "CreateOrderBean{" +
                    "order_info=" + order_info +
                    ", payment_info=" + payment_info +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CreateOrderResult{" +
                "data=" + data +
                '}';
    }
}
