package com.swaiot.webrtc.response;

import java.util.List;


public class BindDeviceResp extends BaseResp {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public static class DataBean {
        private List<String> userBindDeviceList;
        private List<String> deviceBoundToUserList;

        public List<String> getUserBindDeviceList() {
            return userBindDeviceList;
        }

        public List<String> getDeviceBoundToUserList() {
            return deviceBoundToUserList;
        }
    }


}

