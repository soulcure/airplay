package com.coocaa.tvpi.module.pay.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class CCPayReq implements Parcelable {
    /**
     *  支付方式 目前只有微信和支付宝
     */
    public String type;

    //-----------------------------------微信需要的参数-------------------------------------------//
/*    应用ID	appid	String(32)	是	wx8888888888888888	微信开放平台审核通过的应用APPID
    商户号	partnerid	String(32)	是	1900000109	微信支付分配的商户号
    预支付交易会话ID	prepayid	String(64)	是	WX1217752501201407033233368018	微信返回的支付交易会话ID
    扩展字段	package	String(128)	是	Sign=WXPay	暂填写固定值Sign=WXPay
    随机字符串	noncestr	String(32)	是	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法
    时间戳	timestamp	String(10)	是	1412000000	时间戳，请见接口规则-参数规定 签名*/

    public String appId;
    public String partnerId;
    public String prepayId;
    public String nonceStr;
    public String packageValue;
    public String timeStamp;
    public String sign;
    public String extData;

    //-----------------------------------支付宝需要的参数-------------------------------------------//

    //拼接完的订单信息
    public String orderInfo;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CCPayReq{");
        sb.append("type='").append(type).append('\'');
        sb.append(", appId='").append(appId).append('\'');
        sb.append(", partnerId='").append(partnerId).append('\'');
        sb.append(", prepayId='").append(prepayId).append('\'');
        sb.append(", nonceStr='").append(nonceStr).append('\'');
        sb.append(", packageValue='").append(packageValue).append('\'');
        sb.append(", timeStamp='").append(timeStamp).append('\'');
        sb.append(", sign='").append(sign).append('\'');
        sb.append(", extData='").append(extData).append('\'');
        sb.append(", orderInfo='").append(orderInfo).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.appId);
        dest.writeString(this.partnerId);
        dest.writeString(this.prepayId);
        dest.writeString(this.nonceStr);
        dest.writeString(this.packageValue);
        dest.writeString(this.timeStamp);
        dest.writeString(this.sign);
        dest.writeString(this.extData);
        dest.writeString(this.orderInfo);
    }

    public CCPayReq() {
    }

    protected CCPayReq(Parcel in) {
        this.type = in.readString();
        this.appId = in.readString();
        this.partnerId = in.readString();
        this.prepayId = in.readString();
        this.nonceStr = in.readString();
        this.packageValue = in.readString();
        this.timeStamp = in.readString();
        this.sign = in.readString();
        this.extData = in.readString();
        this.orderInfo = in.readString();
    }

    public static final Creator<CCPayReq> CREATOR = new Creator<CCPayReq>() {
        @Override
        public CCPayReq createFromParcel(Parcel source) {
            return new CCPayReq(source);
        }

        @Override
        public CCPayReq[] newArray(int size) {
            return new CCPayReq[size];
        }
    };
}
