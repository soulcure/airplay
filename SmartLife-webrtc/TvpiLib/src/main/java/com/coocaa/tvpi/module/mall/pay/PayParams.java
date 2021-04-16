package com.coocaa.tvpi.module.mall.pay;

public class PayParams {
    //支付宝需要的参数
    private String orderInfo;

    //微信支付需要的参数
    public String request;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }
}
