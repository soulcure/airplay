package swaiotos.channel.iot.response;


import java.util.List;

import swaiotos.channel.iot.response.BaseResp;

public class DeviceStatusResp extends BaseResp {
    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public static class DataBean {
        private String screen_id;
        private int online_status;

        public String getSid() {
            return screen_id;
        }

        public int getOnlineStatus() {
            return online_status;
        }

    }
}
