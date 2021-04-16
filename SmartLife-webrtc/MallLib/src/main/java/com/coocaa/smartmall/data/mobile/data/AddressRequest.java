package com.coocaa.smartmall.data.mobile.data;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

public class AddressRequest  extends BaseRequest {

    @Override
    public String toString() {
        return "AddressRequest{" +
                "address_id='" + address_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_phone='" + user_phone + '\'' +
                ", default_address=" + default_address +
                ", area='" + area + '\'' +
                ", detailed_address='" + detailed_address + '\'' +
                ", full_address='" + full_address + '\'' +
                '}';
    }

    /**
     * address_id : 9
     * user_name : 路飞
     * user_phone : 13055556666
     * default_address : 1
     * area : 深圳市南山区
     * detailed_address : 蛇口沃尔玛2楼205
     * full_address : 深圳市南山区蛇口沃尔玛2楼205
     */

    private String address_id;
    private String user_name;
    private String user_phone;
    private int default_address;
    private String area;
    private String detailed_address;
    private String full_address;

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public int getDefault_address() {
        return default_address;
    }

    public void setDefault_address(int default_address) {
        this.default_address = default_address;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDetailed_address() {
        return detailed_address;
    }

    public void setDetailed_address(String detailed_address) {
        this.detailed_address = detailed_address;
    }

    public String getFull_address() {
        return full_address;
    }

    public void setFull_address(String full_address) {
        this.full_address = full_address;
    }


}
