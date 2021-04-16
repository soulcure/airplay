package com.coocaa.smartscreen.data.videocall;

import java.io.Serializable;
import java.util.Objects;

/**
 * 电视视频联系人实体
 * created by songxing on 2019/11/28
 */
public class ContactsResp implements Serializable {
    //好友状态，0，等待授权；1通过验证(是好友)；2,被拒绝；3，被好友删除
    public static final int STATE_WAIT_ACCEPT = 0; //包含自己等待别人授权和別人等待自己授权的情况
    public static final int STATE_AGREE = 1;
    public static final int STATE_REFUSE = 2;
    public static final int STATE_DELETED = 3;

    public long yxAccountId;
    public long friendYxAccountId;
    public String friendRemark;
    public int rlsFlag;    //好友状态，0，等待授权；1通过验证(是好友)；2,被拒绝；3，被好友删除
    public String yxRegisterCode;
    public String yxOpenId;
    public String yxRegisterType;
    public boolean isSupportHomeCare;
    public String yxAvatar;
    public int callNum;
    public String lastTime;
    public String channelSize;//多路视频支持数
    public boolean isCheck;
    public String last_update_time;

    @Override
    public String toString() {
        return "ContactsResp{" +
                "yxAccountId=" + yxAccountId +
                ", friendYxAccountId=" + friendYxAccountId +
                ", friendRemark='" + friendRemark + '\'' +
                ", rlsFlag=" + rlsFlag +
                ", yxRegisterCode=" + yxRegisterCode +
                ", yxOpenId='" + yxOpenId + '\'' +
                ", yxRegisterType='" + yxRegisterType + '\'' +
                ", isSupportHomeCare=" + isSupportHomeCare +
                ", yxAvatar='" + yxAvatar + '\'' +
                ", callNum=" + callNum +
                ", lastTime='" + lastTime + '\'' +
                ", last_update_time='" + last_update_time + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactsResp)) return false;
        ContactsResp that = (ContactsResp) o;
        return yxAccountId == that.yxAccountId &&
                Objects.equals(yxOpenId, that.yxOpenId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yxAccountId, yxOpenId);
    }
}
