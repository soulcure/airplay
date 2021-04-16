package com.coocaa.smartmall.data.mobile.data;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

public class OrderRequest extends BaseRequest {

    /**
     * address_id : 9
     * payment_type : alipay
     * product_id : 1
     * product_count : 2
     * invoice_type : 0
     * invoice_title :
     * invoice_tax :
     * total_prices : 599
     */

    private String address_id;
    private String payment_type;
    private String product_id;
    private int product_count;
    private int invoice_type;
    private String invoice_title;
    private String invoice_tax;
    private String total_prices;

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public int getProduct_count() {
        return product_count;
    }

    public void setProduct_count(int product_count) {
        this.product_count = product_count;
    }

    public int getInvoice_type() {
        return invoice_type;
    }

    public void setInvoice_type(int invoice_type) {
        this.invoice_type = invoice_type;
    }

    public String getInvoice_title() {
        return invoice_title;
    }

    public void setInvoice_title(String invoice_title) {
        this.invoice_title = invoice_title;
    }

    public String getInvoice_tax() {
        return invoice_tax;
    }

    public void setInvoice_tax(String invoice_tax) {
        this.invoice_tax = invoice_tax;
    }

    public String getTotal_prices() {
        return total_prices;
    }

    public void setTotal_prices(String total_prices) {
        this.total_prices = total_prices;
    }

}
