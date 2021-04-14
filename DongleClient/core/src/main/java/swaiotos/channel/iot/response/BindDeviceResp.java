package swaiotos.channel.iot.response;

import java.util.List;

import swaiotos.channel.iot.db.bean.Device;

public class BindDeviceResp extends BaseResp {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public static class DataBean {
        private List<Device> userBindDeviceList;
        private List<Device> deviceBoundToUserList;

        public List<Device> getUserBindDeviceList() {
            return userBindDeviceList;
        }

        public List<Device> getDeviceBoundToUserList() {
            return deviceBoundToUserList;
        }
    }


}

