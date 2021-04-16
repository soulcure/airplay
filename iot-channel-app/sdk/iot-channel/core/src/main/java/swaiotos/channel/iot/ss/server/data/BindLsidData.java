package swaiotos.channel.iot.ss.server.data;

import java.io.Serializable;
import java.util.List;

public class BindLsidData implements Serializable {
    public List<DeviceItem> userBindDeviceList;
    public List<DeviceItem> deviceBoundToUserList;

    public static class DeviceItem implements Serializable {
        public String zpLsid;
        public String zpChip;
        public String zpModel;
        public String deviceName;
        public String zpNickName;
        public String zpHeadSculpture;
        public String zpRegisterId;    //激活id
        public String zpRegisterType;   //设备类型  pad  tv openid
        public String simulring;
        public String friendZpPosition;
        public String zp_attribute_json;
        public String zp_status;       //设备状态  0:下线 1:上线
        public String zp_device_json;    //设备信息
        public int isTemp = 0;           //临时状态
        public String roomId;            //房间号
        public String merchantName;     //商家名称
        public String merchantIcon;     //商家图标
        public String spaceName;        //空间名称
        public String merchantId;       //商家ID
        public String spaceId;          //空间ID
        public String merchantCoverPhoto;   //商家Cover
        public long lastConnectTime;       //最后连接时间
        public String merchantNameAlias;   //商家简称
    }
}
