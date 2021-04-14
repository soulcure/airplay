package com.swaiot.webrtc.response;

public class RoomOnlineResp extends BaseResp{
    private DataBean hostOnline;

    public DataBean getHostOnline() {
        return hostOnline;
    }


    public static class DataBean {
        private int hostOnline;

        public int getHostOnline() {
            return hostOnline;
        }
    }
}
