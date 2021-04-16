package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;

import java.util.List;

/**
 * @ClassName: Devices
 * @Author: colin
 * @CreateDate: 2020/4/15 19:48 PM
 * @Description:
 */
public interface DeviceAdminManager extends DeviceManager {
    interface OnBindResultListener {
        /**
         * 绑定过程成功回调
         */
        void onSuccess(String bindCode, Device device);

        /**
         * 绑定过程失败回调
         */
        void onFail(String bindCode, String errorType, String msg);
    }

    interface unBindResultListener {
        /**
         * 解绑成功回调
         */
        void onSuccess(String lsid);

        /**
         * 解绑失败回调
         */
        void onFail(String lsid, String errorType, String msg);
    }

    void startBind(String accessToken, String bindCode, OnBindResultListener listener, long time) throws Exception;
    void unBindDevice(String accessToken, String lsid, int type, unBindResultListener listener) throws Exception;

    /**
     * type: 0：lsId（设备sid） 1：spaceId(空间id)
     * accessToken: token的唯一标识
     * uniQueId：空间id or 设备sid
     * OnBindResultListener:临时绑定callback
     * time:超时时间
     * */
    void startTempBindDirect(String accessToken, String uniQueId, int type,OnBindResultListener listener,long time) throws Exception;

}
