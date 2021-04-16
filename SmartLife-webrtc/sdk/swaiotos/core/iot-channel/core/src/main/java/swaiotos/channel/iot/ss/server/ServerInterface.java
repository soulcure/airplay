package swaiotos.channel.iot.ss.server;

import swaiotos.channel.iot.ss.controller.DeviceState;
import swaiotos.channel.iot.ss.server.data.DeviceData;
import swaiotos.channel.iot.ss.server.data.FlushDeviceStatus;
import swaiotos.channel.iot.ss.server.data.JoinToLeaveData;
import swaiotos.channel.iot.ss.server.data.MessageRoomData;
import swaiotos.channel.iot.ss.server.data.OnlineData;
import swaiotos.channel.iot.ss.server.data.ReportLog;
import swaiotos.channel.iot.ss.server.data.RoomDevices;
import swaiotos.channel.iot.ss.server.data.RoomHasOnline;
import swaiotos.channel.iot.ss.server.data.TempQrcode;
import swaiotos.channel.iot.ss.server.data.log.ReportData;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;

/**
 * @ClassName: ServerIInterface
 * @Author: lu
 * @CreateDate: 2020/4/23 10:51 AM
 * @Description:
 */
public interface ServerInterface {
    void submitDeviceState(DeviceState state) throws Exception;

    void submitStartBind(String accessToken,String bindCode, HttpSubscribe<HttpResult<DeviceData>> bindHttpSubcribe) throws  Exception;

    void unBindDevices(String accessToken,String lsid, String deleteType,HttpSubscribe<HttpResult<String>> httpSubcribe) throws  Exception;

    void queryFlushDeviceStatus(FlushDeviceStatus flushDeviceStatus, HttpSubscribe<OnlineData> httpSubcribe);

    /**
     *
     * 异步请求
     *
     * */
    void leaveRoom(String accessToken,String userQuit,HttpSubscribe<HttpResult<JoinToLeaveData>> httpSubcribe);
    void joinRoom(String accessToken,String roomId,String sessionStr,HttpSubscribe<HttpResult<JoinToLeaveData>> httpSubcribe);

    /**
     *
     * 同步请求
     * */
    HttpResult<JoinToLeaveData> leaveRoom(String accessToken,String userQuit);
    HttpResult<JoinToLeaveData> joinRoom(String accessToken,String roomId,String sessionStr);

    void reportLog(ReportData reportLog, HttpSubscribe<HttpResult<Void>> reportResponce) throws  Exception;

    void sendBroadCastRoomMessage(MessageRoomData roomData,HttpSubscribe<HttpResult<RoomHasOnline>> reportResponce) throws  Exception;

    void getRoomDevices(String accessToken, HttpSubscribe<HttpResult<RoomDevices>> roomDevicesResponse) throws Exception;

    void getTempQrcodeInCore(String accessToken, HttpSubscribe<HttpResult<TempQrcode>> tempResponse);

    void submitTempStartBindDirect(String accessToken,String uniQueId,int type, HttpSubscribe<HttpResult<DeviceData>> bindHttpSubcribe) throws  Exception;
}
