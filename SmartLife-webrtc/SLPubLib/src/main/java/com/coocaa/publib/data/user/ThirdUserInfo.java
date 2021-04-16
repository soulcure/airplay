package com.coocaa.publib.data.user;


import java.util.List;

/**
 * Created by guxiaolong on 2020/4/24.
 * 通过accesstoken获取用户的第三方信息
 */
public class ThirdUserInfo {
    private long sky_id;
    private int update_image_times; //更新头像次数
    private String open_id; //酷开openID，唯一标示ID，对接端需要使用此字段进行唯一用户识别
    private int visit_num; //访问次数
    private List<ExternalInfo> external_info; //第三方信息
    private String avatar; //头像
    private String simplePwd;
    private Long cardNo; //会员卡号
    private String tel1; //备注电话1
    private String nick_name; //昵称
    private int balance; //余额
    private long create_date; //创建时间
    private String email; //邮箱
    private String check_add_mobile; //是否不全手机号
    private ThirdAvatar avatars; //不同尺寸的头像
    private int gender; //性别，男1，女2，中性3
    private int base_status; //用户是否可以使用电视机，1 可以，2 不可以
    private String qq;
    private String third_account; //当password和email不为null时值为0，否则1
    private int account_type; //账号类型
    private String mobile; //手机号码
    private String revenue;

    public long getSky_id() {
        return sky_id;
    }

    public void setSky_id(long sky_id) {
        this.sky_id = sky_id;
    }

    public int getUpdate_image_times() {
        return update_image_times;
    }

    public void setUpdate_image_times(int update_image_times) {
        this.update_image_times = update_image_times;
    }

    public String getOpen_id() {
        return open_id;
    }

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }

    public int getVisit_num() {
        return visit_num;
    }

    public void setVisit_num(int visit_num) {
        this.visit_num = visit_num;
    }

    public List<ExternalInfo> getExternal_info() {
        return external_info;
    }

    public void setExternal_info(List<ExternalInfo> external_info) {
        this.external_info = external_info;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSimplePwd() {
        return simplePwd;
    }

    public void setSimplePwd(String simplePwd) {
        this.simplePwd = simplePwd;
    }

    public Long getCardNo() {
        return cardNo;
    }

    public void setCardNo(Long cardNo) {
        this.cardNo = cardNo;
    }

    public String getTel1() {
        return tel1;
    }

    public void setTel1(String tel1) {
        this.tel1 = tel1;
    }

    public String getNick_name() {
        return nick_name;
    }

    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public long getCreate_date() {
        return create_date;
    }

    public void setCreate_date(long create_date) {
        this.create_date = create_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCheck_add_mobile() {
        return check_add_mobile;
    }

    public void setCheck_add_mobile(String check_add_mobile) {
        this.check_add_mobile = check_add_mobile;
    }

    public ThirdAvatar getAvatars() {
        return avatars;
    }

    public void setAvatars(ThirdAvatar avatars) {
        this.avatars = avatars;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getBase_status() {
        return base_status;
    }

    public void setBase_status(int base_status) {
        this.base_status = base_status;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getThird_account() {
        return third_account;
    }

    public void setThird_account(String third_account) {
        this.third_account = third_account;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRevenue() {
        return revenue;
    }

    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    @Override
    public String toString() {
        return "ThirdUserInfo{" +
                "sky_id=" + sky_id +
                ", update_image_times=" + update_image_times +
                ", open_id='" + open_id + '\'' +
                ", visit_num=" + visit_num +
                ", external_info=" + external_info +
                ", avatar='" + avatar + '\'' +
                ", simplePwd='" + simplePwd + '\'' +
                ", cardNo=" + cardNo +
                ", tel1='" + tel1 + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", balance=" + balance +
                ", create_date=" + create_date +
                ", email='" + email + '\'' +
                ", check_add_mobile='" + check_add_mobile + '\'' +
                ", avatars=" + avatars +
                ", gender=" + gender +
                ", base_status=" + base_status +
                ", qq='" + qq + '\'' +
                ", third_account='" + third_account + '\'' +
                ", account_type=" + account_type +
                ", mobile='" + mobile + '\'' +
                ", revenue='" + revenue + '\'' +
                '}';
    }
}
