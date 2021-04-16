package swaiotos.channel.iot.ss.client.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName CmdData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-27
 * @Version TODO (write something)
 */
public class CmdData implements Parcelable {
    public String cmd;//具体指令
    public String param;//指令内部参数,用指定的bean转成json
    public String session;//de 开放给外部用于验证的 针对应用圈 小维ai session不同
    public String mode;//"sync":同步回消息 "async":异步回消息
    public String type;//指令的种类

    public enum CMD_TYPE {
        /**
         * 设备相关
         */
        DEVICE,
        /**
         * event
         */
        KEY_EVENT,
        TOUCH_EVENT,
        CUSTOM_EVENT,
        /**
         * 应用圈
         */
        APP_STORE,
        /**
         * 启动应用(直播投屏、一键清理)-- 解析 param，用OnClickData
         */
        START_APP,
        /**
         * 多媒体推送，在线，直播，本地（video，music，picture）
         */
        MEDIA,
        /**
         * 语音
         */
        VOICE,
        /**
         * 截屏
         */
        SCREEN_SHOT,
        /**
         * 本地（video，music，picture）
         */
        LOCAL_MEDIA,
        /**
         * 账号相关
         */
        ACCOUNT,
        /**
         * 设备状态
         */
        STATE,
        /**
         * 设备属性：设备名称
         * */
        DEVICE_INFO
    }

    public CmdData() {
    }

    public CmdData(Parcel in) {
        this.cmd = in.readString();
        this.param = in.readString();
        this.session = in.readString();
        this.mode = in.readString();
        this.type = in.readString();
    }

    public static final Creator<CmdData> CREATOR = new Creator<CmdData>() {
        @Override
        public CmdData createFromParcel(Parcel source) {
            return new CmdData(source);
        }

        @Override
        public CmdData[] newArray(int size) {
            return new CmdData[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cmd);
        dest.writeString(param);
        dest.writeString(session);
        dest.writeString(mode);
        dest.writeString(type);
    }
}
