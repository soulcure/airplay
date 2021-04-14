package swaiotos.channel.iot.response;

import com.google.gson.Gson;

public class DeviceDataResp extends BaseResp {

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public static class DataBean {
        private String sid;             //被绑定的sid(TV)
        private String attributeJson;   //网络变更信息
        private String deviceInfo;      //设备的硬件属性信息
        private int status;          //在线状态:1为在线,0为下线
        private String deviceType;      //设备类型
        private String roomId;          //房间号（临时绑定时）
        private int isTemp = 0;         //1：临时连接设备 0：默认设备
        private String merchantName;     //商家名称
        private String merchantIcon;     //商家图标
        private String merchantCoverPhoto;  //商家Cover
        private String spaceName;        //空间名称
        private String merchantId;       //商家ID
        private String spaceId;          //空间ID

        public String getSid() {
            return sid;
        }

        public String getAttributeJson() {
            return attributeJson;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public int getStatus() {
            return status;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public String getRoomId() {
            return roomId;
        }

        public int getIsTemp() {
            return isTemp;
        }

        public String getMerchantName() {
            return merchantName;
        }

        public String getMerchantIcon() {
            return merchantIcon;
        }

        public String getMerchantCoverPhoto() {
            return merchantCoverPhoto;
        }

        public String getSpaceName() {
            return spaceName;
        }

        public String getMerchantId() {
            return merchantId;
        }

        public String getSpaceId() {
            return spaceId;
        }


        public String toJson() {
            return new Gson().toJson(this);
        }


        @Override
        public String toString() {
            return "DataBean{" +
                    "sid='" + sid + '\'' +
                    ", attributeJson='" + attributeJson + '\'' +
                    ", deviceInfo='" + deviceInfo + '\'' +
                    ", status=" + status +
                    ", deviceType='" + deviceType + '\'' +
                    ", roomId='" + roomId + '\'' +
                    ", isTemp=" + isTemp +
                    ", merchantName='" + merchantName + '\'' +
                    ", merchantIcon='" + merchantIcon + '\'' +
                    ", merchantCoverPhoto='" + merchantCoverPhoto + '\'' +
                    ", spaceName='" + spaceName + '\'' +
                    ", merchantId='" + merchantId + '\'' +
                    ", spaceId='" + spaceId + '\'' +
                    '}';
        }
    }

}

