package com.coocaa.smartscreen.data.store;
/**
 * 订单
 * Created by songxing on 2020/8/5
 */
public class OrderResp {
    private String order_id;
    private String product_id;
    private String  product_type;
    private String  product_icon;
    private String  product_name;
    private int  order_status;
    private String  order_status_msg;
    private float  total_price;
    private float  product_price;
    private String  order_no;

    public String getOrderId() {
        return order_id;
    }

    public void setOrderId(String orderId) {
        this.order_id = orderId;
    }

    public String getProductId() {
        return product_id;
    }

    public void setProductId(String productId) {
        this.product_id = productId;
    }

    public String getProductType() {
        return product_type;
    }

    public void setProductType(String productType) {
        this.product_type = productType;
    }

    public String getProductIcon() {
        return product_icon;
    }

    public void setProductIcon(String productIcon) {
        this.product_icon = productIcon;
    }

    public String getProductName() {
        return product_name;
    }

    public void setProductName(String productName) {
        this.product_name = productName;
    }

    public int getOrderStatus() {
        return order_status;
    }

    public void setOrderStatus(int orderStatus) {
        this.order_status = orderStatus;
    }

    public String getOrderStatusMsg() {
        return order_status_msg;
    }

    public void setOrderStatusMsg(String orderStatusMsg) {
        this.order_status_msg = orderStatusMsg;
    }

    public float getTotalPrice() {
        return total_price;
    }

    public void setTotalPrice(float totalPrice) {
        this.total_price = totalPrice;
    }

    public float getProductPrice() {
        return product_price;
    }

    public void setProductPrice(float productPrice) {
        this.product_price = productPrice;
    }

    public String getOrderNo() {
        return order_no;
    }

    public void setOrderNo(String orderNo) {
        this.order_no = orderNo;
    }

    @Override
    public String toString() {
        return "OrderResp{" +
                "order_id=" + order_id +
                ", product_id=" + product_id +
                ", product_type='" + product_type + '\'' +
                ", product_icon='" + product_icon + '\'' +
                ", product_name='" + product_name + '\'' +
                ", order_status=" + order_status +
                ", order_status_msg='" + order_status_msg + '\'' +
                ", total_price='" + total_price + '\'' +
                ", product_price=" + product_price +
                ", order_no='" + order_no + '\'' +
                '}';
    }
}
