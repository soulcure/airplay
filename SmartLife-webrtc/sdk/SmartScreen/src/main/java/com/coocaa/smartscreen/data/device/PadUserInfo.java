package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * @ClassName ValidCodeData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 3/5/21
 * @Version TODO (write something)
 */
public class PadUserInfo implements Serializable {
    public long zpAccountId;    //number 必须id
    public String zpLsid;    //string 非必须 账号sid
    public String zpNickName;    //string 必须 昵称
    public String zpHeadSculpture;    //string非必须 头像
    public String zpRegisterId;    //string 非必须 注册码，酷开账号注册是openid，设备注册是激活id
    public String zpRegisterType;    //string 非必须 注册类型:openid/tv/pad
    public String zpMac;    //string 非必须 mac
    public String zpChip;    //string 非必须 机芯
    public String zpModel;    //string 非必须 机型
    public String zpCcreensize;    //string 非必须 尺寸
    public String zpBrand;    //string 非必须 品牌标识
    public String zpLicense;    //string 非必须 牌照商
    public String zpPosition;    //string 非必须 所属房间
    public String createTime;   //string 非必须 注册时间
    public String zpFlag;	//string 非必须 状态:1有效,0无效
    public String lastUpdateTime;	//string非必须 最后更新时间
    public String deviceInfo;	//string 必须 设备硬件信息(注册时传入什么值，返回什么值)
    public String bindCode;	//string 非必须 临时绑定短码
    public String roomId;	//string 非必须 房间号
}
