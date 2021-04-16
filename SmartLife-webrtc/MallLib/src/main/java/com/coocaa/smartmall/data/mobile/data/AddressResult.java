package com.coocaa.smartmall.data.mobile.data;

import java.io.Serializable;
import java.util.List;

public class AddressResult extends BaseResult{


    private List<GetAddressBean> get_address;

    public List<GetAddressBean> getGet_address() {
        return get_address;
    }

    public void setGet_address(List<GetAddressBean> get_address) {
        this.get_address = get_address;
    }

    public static class GetAddressBean implements Serializable {
        /**
         * default_address : 0
         * area : 南京市雨花区
         * user_name : 丹丹
         * detailed_address : 云密城L栋9楼
         * full_address : 南京市雨花区云密城L栋9楼
         * address_id : 8
         * user_phone : 15195916303
         */

        private int default_address;
        private String area;
        private String user_name;
        private String detailed_address;
        private String full_address;
        private String address_id;
        private String user_phone;

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

        public String getUser_name() {
            return user_name;
        }

        public void setUser_name(String user_name) {
            this.user_name = user_name;
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

        public String getAddress_id() {
            return address_id;
        }

        public void setAddress_id(String address_id) {
            this.address_id = address_id;
        }

        public String getUser_phone() {
            return user_phone;
        }

        public void setUser_phone(String user_phone) {
            this.user_phone = user_phone;
        }

        @Override
        public String toString() {
            return "GetAddressBean{" +
                    "default_address=" + default_address +
                    ", area='" + area + '\'' +
                    ", user_name='" + user_name + '\'' +
                    ", detailed_address='" + detailed_address + '\'' +
                    ", full_address='" + full_address + '\'' +
                    ", address_id='" + address_id + '\'' +
                    ", user_phone='" + user_phone + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AddressResult{" +
                "get_address=" + get_address +
                '}';
    }
}
