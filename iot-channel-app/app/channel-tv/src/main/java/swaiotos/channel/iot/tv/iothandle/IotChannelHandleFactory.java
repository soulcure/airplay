package swaiotos.channel.iot.tv.iothandle;


import com.coocaa.statemanager.common.bean.CmdData;

import swaiotos.channel.iot.tv.iothandle.handle.AppInfosHandle;
import swaiotos.channel.iot.tv.iothandle.handle.DefaultHandle;
import swaiotos.channel.iot.tv.iothandle.handle.KeyEventHandle;
import swaiotos.channel.iot.tv.iothandle.handle.StartAppHandle;
import swaiotos.channel.iot.tv.iothandle.handle.StateHandle;
import swaiotos.channel.iot.tv.iothandle.handle.TouchEventHandle;
import swaiotos.channel.iot.tv.iothandle.handle.base.MessageHandle;

public class IotChannelHandleFactory {

    public static MessageHandle getHandle(CmdData.CMD_TYPE type) {
        switch (type) {
            case KEY_EVENT:
                return new KeyEventHandle();
            case TOUCH_EVENT:
                return new TouchEventHandle();
//            case CUSTOM_EVENT:
//                return new CustomEventHandle();
//            case SCREEN_SHOT:
//                return ScreenShotHandle.getInstance();
            case START_APP:
                return new StartAppHandle();
//            case LOCAL_MEDIA:
//                return new LocalMediaHandle();
            case STATE:
                return new StateHandle();
            case APP_INFOS:
                return new AppInfosHandle();
            default:
                return new DefaultHandle();
        }
    }
}
