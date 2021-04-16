package swaiotos.channel.iot.ss.server;

import swaiotos.channel.iot.ss.SSContext;
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
import swaiotos.channel.iot.ss.server.http.SessionHttpService;
import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;

public class ServerInterfaceImpl implements ServerInterface {
    private SSContext mSSContext;

    public ServerInterfaceImpl(SSContext ssContext) {
        mSSContext = ssContext;
    }

    @Override
    public void submitDeviceState(DeviceState state) throws Exception {
        String accessToken = mSSContext.getSmartScreenManager().getLSIDManager().getLSIDInfo().accessToken;
        HttpApi.getInstance().requestSync(SessionHttpService.SERVICE.reportDeviceState(accessToken,state.encode()),"reportProperty", mSSContext.getLSID());
    }

    @Override
    public void submitStartBind(String accessToken, String bindCode, HttpSubscribe<HttpResult<DeviceData>> bindHttpSubcribe) throws Exception {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.bindDevice(accessToken,bindCode),bindHttpSubcribe,"submit", mSSContext.getLSID());
    }

    @Override
    public void unBindDevices(String accessToken, String lsid, String deleteType, HttpSubscribe<HttpResult<String>> httpSubcribe) throws Exception {
       HttpApi.getInstance().request(SessionHttpService.SERVICE.unbindDevice(accessToken,lsid,deleteType),httpSubcribe,"delete-bind", mSSContext.getLSID());
    }

    @Override
    public void queryFlushDeviceStatus(FlushDeviceStatus flushDeviceStatus, HttpSubscribe<OnlineData> httpSubcribe) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.queryFlushDeviceStatus(flushDeviceStatus),httpSubcribe,"screens-online-status", mSSContext.getLSID());
    }

    @Override
    public void leaveRoom(String accessToken,String userQuit, HttpSubscribe<HttpResult<JoinToLeaveData>> httpSubcribe) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.leaveRoom(accessToken,userQuit),httpSubcribe,"leave-room", mSSContext.getLSID());
    }

    @Override
    public void joinRoom(String accessToken, String roomId,String session, HttpSubscribe<HttpResult<JoinToLeaveData>> httpSubcribe) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.joinRoom(accessToken,roomId,session),httpSubcribe,"join-room", mSSContext.getLSID());
    }

    @Override
    public HttpResult<JoinToLeaveData> leaveRoom(String accessToken,String userQuit) {
        return HttpApi.getInstance().requestSync(SessionHttpService.SERVICE.leaveRoom(accessToken,userQuit),"leave-room", mSSContext.getLSID());
    }

    @Override
    public HttpResult<JoinToLeaveData> joinRoom(String accessToken, String roomId,String session) {
        return HttpApi.getInstance().requestSync(SessionHttpService.SERVICE.joinRoom(accessToken,roomId,session),"join-room", mSSContext.getLSID());
    }

    @Override
    public void reportLog(ReportData reportLog, HttpSubscribe<HttpResult<Void>> reportResponce) throws Exception {
        //上报日志不上报行为分析
        HttpApi.getInstance().request(SessionHttpService.SERVICE.reportLog(reportLog),reportResponce);
    }

    @Override
    public void sendBroadCastRoomMessage(MessageRoomData messageRoomData, HttpSubscribe<HttpResult<RoomHasOnline>> reportResponce) throws Exception {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.sendBroadCastRoomMessage(mSSContext.getAccessToken(),messageRoomData),reportResponce,"broadcast-room-message", mSSContext.getLSID());
    }

    @Override
    public void getRoomDevices(String accessToken, HttpSubscribe<HttpResult<RoomDevices>> roomDevicesResponse) throws Exception {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.getRoomDevices(mSSContext.getAccessToken()),roomDevicesResponse,"room-devices", mSSContext.getLSID());
    }

    @Override
    public void getTempQrcodeInCore(String accessToken, HttpSubscribe<HttpResult<TempQrcode>> tempResponse) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.getTempQrcode(mSSContext.getAccessToken()),tempResponse,"getQrCode", mSSContext.getLSID());
    }

    @Override
    public void submitTempStartBindDirect(String accessToken, String uniQueId, int type, HttpSubscribe<HttpResult<DeviceData>> bindHttpSubcribe) throws Exception {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.tempBindDirectDevice(accessToken,uniQueId,type),bindHttpSubcribe,"submit", mSSContext.getLSID());
    }

}
