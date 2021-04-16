package com.coocaa.smartscreen.data.account;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * 云信用户信息
 * Created by songxing on 2020/6/17
 */
public class YunXinUserInfo implements Serializable {
    //id
    public long yxAccountId;
    //昵称
    public String yxNickName;
    //签名
    public String yxSignature;
    //头像
    public String yxAvatar;
    //手机号或激活id
    public String yxRegisterCode;
    //注册类型，mobile  ，tv
    public String yxRegisterType;
    //openid
    public String yxOpenId;
    //创建时间
    public String createTime;
    //最后更新时间
    public String lastUpdateTime;
    //云信token
    public String yxThirdToken;
    //云信name
    public String yxThirdName;
    //多路视频支持数
    public String channelSize;

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "yxAccountId=" + yxAccountId +
                ", yxNickName='" + yxNickName + '\'' +
                ", yxSignature='" + yxSignature + '\'' +
                ", yxAvatar='" + yxAvatar + '\'' +
                ", yxRegisterCode='" + yxRegisterCode + '\'' +
                ", yxRegisterType='" + yxRegisterType + '\'' +
                ", yxOpenId='" + yxOpenId + '\'' +
                ", createTime='" + createTime + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", yxThirdToken='" + yxThirdToken + '\'' +
                ", yxThirdName='" + yxThirdName + '\'' +
                '}';
    }
}
