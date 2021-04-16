package swaiotos.channel.iot.common.entity;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.entity
 * @ClassName: UserLsIDData
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 16:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 16:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class UserLSIDData {
    /**
     * ├─ zpAccountId	    number	必须		id
     * ├─ zpLsid	        string	非必须		账号sid
     * ├─ zpNickName	    string	非必须		昵称
     * ├─ zpHeadSculpture	string	非必须		头像
     * ├─ zpRegisterId	    string	非必须		注册码，酷开账号注册是openid，设备注册是激活id
     * ├─ zpRegisterType	string	非必须		注册类型:openid/tv/pad
     * ├─ zpMac	            string	非必须		mac
     * ├─ zpChip	        string	非必须		机芯
     * ├─ zpModel	        string	非必须		机型
     * ├─ zpCcreensize	    string	非必须		尺寸
     * ├─ zpBrand	        string	非必须		品牌标识
     * ├─ zpLicense	        string	非必须		牌照商
     * ├─ zpPosition	    string	非必须		所属房间
     * ├─ createTime	    string	非必须		注册时间
     * ├─ zpFlag	        string	非必须		状态:1有效,0无效
     * ├─ lastUpdateTime	string	非必须		最后更新时间
     * ├─ deviceInfo	    string	必须		设备信息
     *
     * */
    private int zpAccountId;
    private String zpLsid;
    private String zpNickName;
    private String zpHeadSculpture;
    private String zpRegisterId;
    private String zpRegisterType;
    private String zpMac;
    private String zpChip;
    private String zpModel;
    private String zpCcreensize;
    private String zpBrand;
    private String zpLicense;
    private String zpPosition;
    private String createTime;
    private String zpFlag;
    private String lastUpdateTime;
    private String deviceInfo;
    private String bindCode;
    private String roomId;

    public int getZpAccountId() {
        return zpAccountId;
    }

    public void setZpAccountId(int zpAccountId) {
        this.zpAccountId = zpAccountId;
    }

    public String getZpLsid() {
        return zpLsid;
    }

    public void setZpLsid(String zpLsid) {
        this.zpLsid = zpLsid;
    }

    public String getZpNickName() {
        return zpNickName;
    }

    public void setZpNickName(String zpNickName) {
        this.zpNickName = zpNickName;
    }

    public String getZpHeadSculpture() {
        return zpHeadSculpture;
    }

    public void setZpHeadSculpture(String zpHeadSculpture) {
        this.zpHeadSculpture = zpHeadSculpture;
    }

    public String getZpRegisterId() {
        return zpRegisterId;
    }

    public void setZpRegisterId(String zpRegisterId) {
        this.zpRegisterId = zpRegisterId;
    }

    public String getZpRegisterType() {
        return zpRegisterType;
    }

    public void setZpRegisterType(String zpRegisterType) {
        this.zpRegisterType = zpRegisterType;
    }

    public String getZpMac() {
        return zpMac;
    }

    public void setZpMac(String zpMac) {
        this.zpMac = zpMac;
    }

    public String getZpChip() {
        return zpChip;
    }

    public void setZpChip(String zpChip) {
        this.zpChip = zpChip;
    }

    public String getZpModel() {
        return zpModel;
    }

    public void setZpModel(String zpModel) {
        this.zpModel = zpModel;
    }

    public String getZpCcreensize() {
        return zpCcreensize;
    }

    public void setZpCcreensize(String zpCcreensize) {
        this.zpCcreensize = zpCcreensize;
    }

    public String getZpBrand() {
        return zpBrand;
    }

    public void setZpBrand(String zpBrand) {
        this.zpBrand = zpBrand;
    }

    public String getZpLicense() {
        return zpLicense;
    }

    public void setZpLicense(String zpLicense) {
        this.zpLicense = zpLicense;
    }

    public String getZpPosition() {
        return zpPosition;
    }

    public void setZpPosition(String zpPosition) {
        this.zpPosition = zpPosition;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getZpFlag() {
        return zpFlag;
    }

    public void setZpFlag(String zpFlag) {
        this.zpFlag = zpFlag;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getBindCode() {
        return bindCode;
    }

    public void setBindCode(String bindCode) {
        this.bindCode = bindCode;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
