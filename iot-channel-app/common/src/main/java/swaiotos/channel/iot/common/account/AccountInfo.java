package swaiotos.channel.iot.common.account;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020-04-01
 */
public class AccountInfo {
    public String user_id;//酷开openID，唯一标示ID，对接端需要使用此字段进行唯一用户识别
    public String token; //token
    public String address;//地址
    public String nick_name;//用户昵称
    public String birthday;//出生日期
    public int gender;//性别，男1,女2,中性3
    public String slogan;//签名
    public String mobile;//手机号码
    public String avatar;//头像
}
