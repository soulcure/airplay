package swaiotos.channel.iot.ss.server.http;


import android.content.Context;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.server.data.ApkInfo;
import swaiotos.channel.iot.ss.server.data.AppItem;
import swaiotos.channel.iot.ss.server.data.BindLsidData;
import swaiotos.channel.iot.ss.server.data.DeviceData;
import swaiotos.channel.iot.ss.server.data.FlushDeviceStatus;
import swaiotos.channel.iot.ss.server.data.JoinToLeaveData;
import swaiotos.channel.iot.ss.server.data.MessageRoomData;
import swaiotos.channel.iot.ss.server.data.OnlineData;
import swaiotos.channel.iot.ss.server.data.ReportLog;
import swaiotos.channel.iot.ss.server.data.RoomDevices;
import swaiotos.channel.iot.ss.server.data.ScreenApps;
import swaiotos.channel.iot.ss.server.data.RoomHasOnline;
import swaiotos.channel.iot.ss.server.data.TempQrcode;
import swaiotos.channel.iot.ss.server.data.log.ReportData;
import swaiotos.channel.iot.ss.server.http.api.AppStoreResult;
import swaiotos.channel.iot.ss.server.http.api.HttpManager;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.server.utils.MD5;
import swaiotos.channel.iot.ss.server.utils.SignCore;
import swaiotos.channel.iot.utils.AndroidLog;


/**
 *
 */
public class SessionHttpService extends HttpManager<ISessionHttpMethod> {

    public static final SessionHttpService SERVICE = new SessionHttpService();

    @Override
    protected Class<ISessionHttpMethod> getServiceClass() {
        return ISessionHttpMethod.class;
    }

    @Override
    protected Map<String, String> getHeaders() {
        return HttpServiceConfig.HEADER_LOADER.getHeader();
    }

    @Override
    protected String getBaseUrl() {
        return Constants.getIOTServer(SSChannelService.getContext());
    }

    public Call<HttpResult<BindLsidData>> getLsidList(String accessToken, String queryType) {
        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("queryType", queryType);
        return getHttpService().getLsidList(accessToken, queryType, getSign(map));
    }

    public Call<HttpResult<String>> reportDeviceState(String accessToken, String property) {
        long time = System.currentTimeMillis();
        return getHttpService().reportDeviceState(accessToken, String.valueOf(time), property);
    }

    public Call<HttpResult<DeviceData>> bindDevice(String accessToken, String bindCode) {
        return getHttpService().bindDevice(accessToken, bindCode);
    }

    public Call<HttpResult<DeviceData>> tempBindDirectDevice(String accessToken, String uniQueId,int type) {
        return getHttpService().tempBindDirectDevice(accessToken, type == 0 ? uniQueId : "", type == 0 ?  "" : uniQueId);
    }


    public Call<HttpResult<String>> unbindDevice(String accessToken, String lsid, String deleteType) {
        return getHttpService().unbindDevice(accessToken, lsid, deleteType);
    }

    public Call<HttpResult<JoinToLeaveData>> leaveRoom(String accessToken,String userQuit) {
        return getHttpService().leaveRoom(accessToken,userQuit);
    }

    public Call<HttpResult<JoinToLeaveData>> joinRoom(String accessToken, String roomId,String session) {
        return getHttpService().joinRoom(accessToken, roomId,session);
    }

    public Call<HttpResult<RoomHasOnline>> sendBroadCastRoomMessage(String accessToken, MessageRoomData roomData) {
        return getHttpService().sendBroadCastRoomMessage(accessToken, roomData);
    }

    public Call<HttpResult<RoomDevices>> getRoomDevices(String accessToken) {
        AndroidLog.androidLog("room-devices","----getRoomDevices:"+this);
        return getHttpService().getRoomDevices(accessToken);
    }

    public Call<HttpResult<TempQrcode>> getTempQrcode(String accessToken) {
        long time = System.currentTimeMillis();

        Map<String, String> qrCodeMap = new HashMap<>();
        qrCodeMap.put(Constants.COOCAA_ACCESSTOKEN,accessToken);
        qrCodeMap.put(Constants.COOCAA_TEMPBIND,Constants.COOCAA_DANGLE);
        qrCodeMap.put(Constants.COOCAA_TIME,""+time);

        String sign = SignCore.buildRequestMysign(qrCodeMap, Constants.getAppKey(SSChannelService.getContext()));

        return getHttpService().getTempQrcode(accessToken,time,Constants.COOCAA_DANGLE,sign);
    }

    public Call<HttpResult<Void>> reportLog(ReportData reportLog) {
        String appKey = Constants.getLogAppKey(SSChannelService.getContext());
        int time = (int) (System.currentTimeMillis()/1000);
        String prestr = "appkey"+ appKey + "time"+time +Constants.LOG_SECRET;
        String sign = null;
        try {
            sign = MD5.getMd5(prestr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        AndroidLog.androidLog("sign:"+sign);
        return httpLog(SSChannelService.getContext()).reportLog(appKey, time,sign,reportLog);
    }

    public Call<OnlineData> queryFlushDeviceStatus(FlushDeviceStatus flushDeviceStatus) {
        return getHttpService().queryFlushDeviceStatus(flushDeviceStatus);
    }


    public Call<HttpResult<ApkInfo>> reqVersionCode(String clientId, int protoCode) {
        return httpAppStore(SSChannelService.getContext()).reqVersionCode(clientId, protoCode);
//        return httpAppStoreTest().reqVersionCode(clientId, protoCode);
    }


    public Call<AppStoreResult<AppItem>> checkAppStore(String packageName) {
        return httpAppStore(SSChannelService.getContext()).checkAppStore(packageName);
//        return httpAppStoreTest().checkAppStore(packageName);
    }

    public Call<HttpResult<ScreenApps>> getScreenApps(Context c) {
        String appKey = Constants.getLogAppKey(c);
        int time = (int) (System.currentTimeMillis()/1000);
        String prestr = "appkey"+ appKey + "time"+time +Constants.LOG_SECRET;
        String sign = null;
        try {
            sign = MD5.getMd5(prestr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return httpLog(c).getScreenApps(appKey,time,sign);
    }


    private String getSign(Map<String, String> map) {
        return SignCore.buildRequestMysign(map, Constants.getAppKey(SSChannelService.getContext()));
    }

    private String getLogSign(Map<String, String> map) {
        return SignCore.buildRequestMysign(map, Constants.getAppKey(SSChannelService.getContext()));
    }
}
