package swaiotos.runtime.h5.core.os.model;

import android.content.Context;
import android.net.Uri;

import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.runtime.h5.common.bean.H5ContentBean;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;

/**
 * @ClassName: ISendMessageManager
 * @Author: AwenZeng
 * @CreateDate: 2020/10/26 20:08
 * @Description:
 */
public interface ISendMessageManager {

    boolean sendTvDongleMessage(H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom);

    Context getContext();

    void getRemoteAppVersion(String clientTarget);

    boolean sendDeviceMessage(H5ContentBean data, Object remoteVersion, IMMessageCallback callback, Boolean isShowCastFrom);
    void gotoApplet(Uri uri);
    void setCastFromShow(boolean isShow);

    boolean getUserInfo(Object id);

    boolean setRemoteCtrlState(String pause,String title);

    boolean sendGameEvent(OnGameEngineInfo gameInfo, Object remoteVersion, IMMessageCallback callback);

}
