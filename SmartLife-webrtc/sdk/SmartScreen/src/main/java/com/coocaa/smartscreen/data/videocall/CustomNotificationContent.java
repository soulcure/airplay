package com.coocaa.smartscreen.data.videocall;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.coocaa.smartscreen.data.videocall.transinfo.TeamChatTransInfo;

/**
 * 云信自定义消息内容实体
 * created by songxing on 2019/12/11
 */
public class CustomNotificationContent {

    public static final int TP_NOTIFICATION_ADD_FRIEND = 1;  //添加好友(直接添加为好友,无需验证)
    public static final int TP_NOTIFICATION_REQUEST_FRIEND = 2;    //请求添加好友
    public static final int TP_NOTIFICATION_VERIFY = 3;     //通过添加好友请求
    public static final int TP_NOTIFICATION_REJECT = 4;   //拒绝添加好友请求
    public static final int TP_NOTIFICATION_DELETE_FRIEND = 5;     //删除好友
    public static final int TP_NOTIFICATION_VIDEO_CHAT = 6;     //视频通话
    public static final int TP_NOTIFICATION_ROTATE_REMOTE_VIDEO = 7;     //旋转远端视频画面

    //检测远端摄像头是否可旋转
    public static final int TP_NOTIFICATION_CHECK_REMOTE_CAMERA_FOR_ROTABLE = 8;

    //远端摄像头状态,message取值范围为[0，1]，1表示为可旋转,0表示不可旋转
    public static final int TP_NOTIFICATION_REMOTE_CAMERA_ROTABLE_STATUS = 9;
    public static final int ROTABLE_TRUE = 0;
    public static final int ROTABLE_FALSE = 1;

    //旋转远端摄像头,mesage取值范围为[0，1，2，3],分别表示调节方向：上、左、下、右
    public static final int TP_NOTIFICATION_ROTATE_REMOTE_CAMERA = 10;
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = 3;

    //设置远端的麦克风状态，message取值范围为[0，1]，1表示为静音状态，麦克风关闭，0表示对方麦克风打开
    public static final int TP_NOTIFICATION_SET_REMOTE_MUTE = 11;


    public static final int TP_NOTIFICATION_REQUEST_MONITOR_AUTHORIZATION = 12;     //请求监控授权状态
    public static final int TP_NOTIFICATION_MONITOR_AUTHORIZATION_STATUS_DENIED = 13;     //拒绝监控授权
    public static final int TP_NOTIFICATION_MONITOR_AUTHORIZATION_STATUS_AUTHORIZED = 14;  //批准监控授权

    //多人通话拒绝通话
    public static final int TP_NOTIFICATION_MEETING_REJECT_CALL =  15;
    //多人通话发起会议邀请
    public static final int TP_NOTIFICATION_MEETING_INVITE =  16;
    //会议中拉⼈
    public static final int TP_NOTIFICATION_PULL_INVITERS = 17;
    //呼叫连接超时
    public static final int TP_NOTIFICATION_MEETING_INVITE_TIMEOUT = 18;
    //呼叫主动挂断
    public static final int TP_NOTIFICATION_MEETING_HANGUP = 19;

    public static final int TP_NOTIFICATION_USER_BUSY = 20;

    public static final int TP_NOTIFICATION_MEETING_LOCK = 21;

    public static final int TP_NOTIFICATION_MONITOR_LOCK = 22;


    @IntDef({TP_NOTIFICATION_ADD_FRIEND, TP_NOTIFICATION_REQUEST_FRIEND,
            TP_NOTIFICATION_VERIFY, TP_NOTIFICATION_REJECT,
            TP_NOTIFICATION_DELETE_FRIEND, TP_NOTIFICATION_VIDEO_CHAT,
            TP_NOTIFICATION_ROTATE_REMOTE_VIDEO, TP_NOTIFICATION_CHECK_REMOTE_CAMERA_FOR_ROTABLE,
            TP_NOTIFICATION_REMOTE_CAMERA_ROTABLE_STATUS, TP_NOTIFICATION_ROTATE_REMOTE_CAMERA,
            TP_NOTIFICATION_SET_REMOTE_MUTE, TP_NOTIFICATION_REQUEST_MONITOR_AUTHORIZATION,
            TP_NOTIFICATION_MONITOR_AUTHORIZATION_STATUS_DENIED, TP_NOTIFICATION_MONITOR_AUTHORIZATION_STATUS_AUTHORIZED,
            TP_NOTIFICATION_MEETING_REJECT_CALL,
            TP_NOTIFICATION_MEETING_INVITE,TP_NOTIFICATION_PULL_INVITERS,
            TP_NOTIFICATION_MEETING_INVITE_TIMEOUT,TP_NOTIFICATION_MEETING_HANGUP,
            TP_NOTIFICATION_USER_BUSY,TP_NOTIFICATION_MEETING_LOCK,
            TP_NOTIFICATION_MONITOR_LOCK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface NotificationType {
    }


    //消息类型
    public @NotificationType
    int notificationType;

    //用户id
    public String yxAccountId;

    //手机或者激活id
    public String yxRegisterCode;

    //云信id
    public String yxOpenId;

    //其他保留信息
    public String message;

    //---------------------------------多人通话新增----------------------------------
    public String roomName;
    public String teamId;
    public List<TeamChatTransInfo> accounts;
    public String teamName;

    private static final String KEY_ID = "id";
    private static final String KEY_MEMBER = "members";
    private static final String KEY_TID = "teamId";
    private static final String KEY_RID = "room";
    private static final String KEY_TNAME = "teamName";

    //------------------------------------------------------------------------

    public CustomNotificationContent(@NotificationType int notificationType, String yxAccountId,
                                     String yxRegisterCode, String yxOpenId, @Nullable String mesage) {
        this.notificationType = notificationType;
        this.yxAccountId = yxAccountId;
        this.yxRegisterCode = yxRegisterCode;
        this.yxOpenId = yxOpenId;
        this.message = mesage;
    }

    public CustomNotificationContent(@NotificationType int notificationType, String message) {
        this.notificationType = notificationType;
        this.message = message;
    }

    public CustomNotificationContent(int notificationType) {
        this.notificationType = notificationType;
    }

    @Override
    public String toString() {
        return "CustomNotificationContent{" +
                "notificationType=" + notificationType +
                ", yxAccountId=" + yxAccountId +
                ", yxRegisterCode=" + yxRegisterCode +
                ", yxOpenId='" + yxOpenId + '\'' +
                ", mesage='" + message + '\'' +
                '}';
    }
}
