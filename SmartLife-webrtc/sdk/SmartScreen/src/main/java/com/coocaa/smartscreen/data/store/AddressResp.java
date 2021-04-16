package com.coocaa.smartscreen.data.store;

import java.io.Serializable;

/**
 * 商城收货地址
 * Created by songxing on 2020/8/5
 */
public class AddressResp implements Serializable {
    private int address_id;
    private boolean default_address;
    private String user_name;
    private String user_phone;
    private String area;
    private String detailed_address;
    private String full_address;

    public int getAddressId() {
        return address_id;
    }

    public void setAddressId(int addressId) {
        this.address_id = addressId;
    }

    public boolean isDefaultAddress() {
        return default_address;
    }

    public void setDefaultAddress(boolean defaultAddress) {
        this.default_address = defaultAddress;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String userName) {
        this.user_name = userName;
    }

    public String getUserPhone() {
        return user_phone;
    }

    public void setUserPhone(String userPhone) {
        this.user_phone = userPhone;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDetailedAddress() {
        return detailed_address;
    }

    public void setDetailedAddress(String detailedAddress) {
        this.detailed_address = detailedAddress;
    }

    public String getFullAddress() {
        return full_address;
    }

    public void setFullAddress(String fullAddress) {
        this.full_address = fullAddress;
    }

    @Override
    public String toString() {
        return "AddressResp{" +
                "addressId=" + address_id +
                ", default_address=" + default_address +
                ", user_name='" + user_name + '\'' +
                ", user_phone='" + user_phone + '\'' +
                ", area='" + area + '\'' +
                ", detailed_address='" + detailed_address + '\'' +
                ", full_address='" + full_address + '\'' +
                '}';
    }
}
