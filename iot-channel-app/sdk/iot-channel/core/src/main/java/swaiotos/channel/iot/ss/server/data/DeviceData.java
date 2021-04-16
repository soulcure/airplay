package swaiotos.channel.iot.ss.server.data;

/**
 * @ProjectName: iot-channel-swaiotos
 * @Package: swaiotos.channel.iot.okgo.entity
 * @ClassName: DeviceData
 * @Description: 设备的状态、属性等信息
 * @Author: wangyuehui
 * @CreateDate: 2020/4/26 14:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/26 14:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class DeviceData {

    private String sid;             //被绑定的sid(TV)
    private String attributeJson;   //网络变更信息
    private String deviceInfo;      //设备的硬件属性信息
    private String status;          //在线状态:1为在线,0为下线
    private String deviceType;      //设备类型
    private String roomId;          //房间号（临时绑定时）
    private int isTemp = 0;         //1：临时连接设备 0：默认设备
    private String merchantName;     //商家名称
    private String merchantIcon;     //商家图标
    private String spaceName;        //空间名称
    private String merchantId;       //商家ID
    private String spaceId;          //空间ID
    private String merchantCoverPhoto;  //商家Cover
    private long lastConnectTime;       //最后连接时间
    private String merchantNameAlias;   //商家简称

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAttributeJson() {
        return attributeJson;
    }

    public void setAttributeJson(String attributeJson) {
        this.attributeJson = attributeJson;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getIsTemp() {
        return isTemp;
    }

    public void setIsTemp(int isTemp) {
        this.isTemp = isTemp;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantIcon() {
        return merchantIcon;
    }

    public void setMerchantIcon(String merchantIcon) {
        this.merchantIcon = merchantIcon;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getMerchantCoverPhoto() {
        return merchantCoverPhoto;
    }

    public void setMerchantCoverPhoto(String merchantCoverPhoto) {
        this.merchantCoverPhoto = merchantCoverPhoto;
    }

    public long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public String getMerchantNameAlias() {
        return merchantNameAlias;
    }

    public void setMerchantNameAlias(String merchantNameAlias) {
        this.merchantNameAlias = merchantNameAlias;
    }
}
