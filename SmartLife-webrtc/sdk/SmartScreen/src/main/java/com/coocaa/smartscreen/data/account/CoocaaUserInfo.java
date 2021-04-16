package com.coocaa.smartscreen.data.account;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 酷开用户信息
 * Created by IceStorm on 2017/12/25.
 */

public class CoocaaUserInfo implements Serializable {
    private static final long serialVersionUID = -5013653087809398260L;

    public String address;
    public String avatar;
    public String birthday;
//    public String city;
    public String corp;
//    public String district;
//    public String education_grade;
    public String email;
    public int gender; // 男 1，女 2，中性 3
    public String idcard;
//    public String line;
    public String mobile;
    public String nick_name;
//    public String occupation;
    public String open_id;
//    public String oss_id;
    public String postcode;
//    public String province;
    public String qq;
    public String region;
    public String region_id;
    public String revenue;
//    public String score;
    public long sky_id;
//    public String skype;
    public String slogan;
    public String tel1;
    public String tel2;
//    public int visit_num;
//    public String wechat;
    public String weibo;
//    public String balance;
    /**第三方登录，0表示酷开，1qq，2微信，3微博*/
    public int third_account; // 1为未完善酷开资料的账号, 0为完善了酷开资料的账号(可与第三方账号进行解绑)
    public List<CoocaaUserInfoExternModel> external_info;
    // public String grant_users;
//    public String last_name;

    /**获取到的用户登录token*/
    public String access_token;
    /**获取到的用户登录userId*/
    public String userId;

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }

    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getOpen_id() {
        return open_id;
    }
    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }

    public String getRegion_id() {
        return region_id;
    }
    public void setRegion_id(String region_id) {
        this.region_id = region_id;
    }

    public String getCorp() {
        return corp;
    }
    public void setCorp(String corp) {
        this.corp = corp;
    }

    public String getNick_name() {
        return nick_name;
    }
    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }

    public String getTel1() {
        return tel1;
    }
    public void setTel1(String tel1) {
        this.tel1 = tel1;
    }

    public String getTel2() {
        return tel2;
    }
    public void setTel2(String tel2) {
        this.tel2 = tel2;
    }

    public String getSlogan() {
        return slogan;
    }
    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public int getGender() {
        return gender;
    }
    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getQq() {
        return qq;
    }
    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getRevenue() {
        return revenue;
    }
    public void setRevenue(String revenue) {
        this.revenue = revenue;
    }

    public long getSky_id() {
        return sky_id;
    }
    public void setSky_id(long sky_id) {
        this.sky_id = sky_id;
    }

    public String getIdcard() {
        return idcard;
    }
    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }

    public String getPostcode() {
        return postcode;
    }
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public int getThird_account() {
        return third_account;
    }
    public void setThird_account(int third_account) {
        this.third_account = third_account;
    }

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWeibo() {
        return weibo;
    }
    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    public String getAccessToken() {
        return access_token;
    }
    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 绑定第三方的账号的信息
    public class CoocaaUserInfoExternModel {
        public String external_flag;
        public String external_id;
        public String bind_time;
    }
    /*
    * "external_flag" : "qq",
    "external_id" : "athirdaccountopenid",
    "bind_time" : "2018-01-17 09:28:47"
    * */

    // 授权用户信息
    public class CoocaaUserInfoGrantUserModel {
        public String title;
        public String open_id;
        public int grade;
        public String parameters;
    }

    @Override
    public String toString() {
        return "CoocaaUserInfo{" +
                "address='" + address + '\'' +
                ", avatar='" + avatar + '\'' +
                ", birthday='" + birthday + '\'' +
                ", corp='" + corp + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", idcard='" + idcard + '\'' +
                ", mobile='" + mobile + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", open_id='" + open_id + '\'' +
                ", postcode='" + postcode + '\'' +
                ", qq='" + qq + '\'' +
                ", region='" + region + '\'' +
                ", region_id='" + region_id + '\'' +
                ", revenue='" + revenue + '\'' +
                ", sky_id=" + sky_id +
                ", slogan='" + slogan + '\'' +
                ", tel1='" + tel1 + '\'' +
                ", tel2='" + tel2 + '\'' +
                ", weibo='" + weibo + '\'' +
                ", third_account=" + third_account +
                ", external_info=" + external_info +
                ", access_token='" + access_token + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}

