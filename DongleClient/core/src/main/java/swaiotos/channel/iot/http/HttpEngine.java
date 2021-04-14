package swaiotos.channel.iot.http;

import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import swaiotos.channel.iot.response.BindDeviceResp;
import swaiotos.channel.iot.response.DeviceDataResp;
import swaiotos.channel.iot.response.DeviceStatusResp;
import swaiotos.channel.iot.request.ReqDevices;
import swaiotos.channel.iot.response.BaseResp;
import swaiotos.channel.iot.request.ResRoomMsg;
import swaiotos.channel.iot.response.RoomOnlineResp;
import swaiotos.channel.iot.response.UnbindResp;

public class HttpEngine {

    private static final HttpMethod deviceService = HttpManager.instance().create(HttpMethod.class);

    /**
     * 获取绑定设备列表
     */
    public static void getBindDevice(Map<String, String> map,
                                     Observer<BindDeviceResp> observer) {
        setSubscribe(deviceService.getBindDevice(map), observer);
    }


    public static void bindDevice(String accessToken, String bindCode,
                                  Observer<DeviceDataResp> observer) {
        setSubscribe(deviceService.bindDevice(accessToken, bindCode), observer);
    }

    public static void unbindDevice(String accessToken, String lsid, int deleteType,
                                    Observer<UnbindResp> observer) {
        setSubscribe(deviceService.unbindDevice(accessToken, lsid, deleteType), observer);
    }


    public static void refreshOnlineStatus(ReqDevices reqDevices,
                                           Observer<DeviceStatusResp> observer) {
        setSubscribe(deviceService.refreshOnlineStatus(reqDevices), observer);
    }

    public static void joinRoom(String accessToken, String roomId, String session,
                                Observer<BaseResp> observer) {
        setSubscribe(deviceService.joinRoom(accessToken, roomId, session), observer);
    }

    public static void leaveRoom(String accessToken, String userQuit,
                                 Observer<BaseResp> observer) {
        setSubscribe(deviceService.leaveRoom(accessToken, userQuit), observer);
    }

    public static void sendBroadCastRoomMessage(String accessToken, ResRoomMsg resRoomMsg,
                                                Observer<RoomOnlineResp> observer) {
        setSubscribe(deviceService.sendBroadCastRoomMessage(accessToken, resRoomMsg), observer);
    }


    private static <T> void setSubscribe(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())//子线程访问网络
                .observeOn(AndroidSchedulers.mainThread())//回调到主线程
                .subscribe(observer);
    }
}