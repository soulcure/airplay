package swaiotos.channel.iot.ss.server.http;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
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
import swaiotos.channel.iot.ss.server.http.api.HttpResult;


public interface ISessionHttpMethod {

    @GET("/api/screen/query-bind-device")
    Call<HttpResult<BindLsidData>> getLsidList(@Query("accessToken") String accessToken,
                                               @Query("queryType") String queryType,
                                               @Query("sign") String sign);

    @GET("/api/screen/reportProperty")
    Call<HttpResult<String>> reportDeviceState(@Query("accessToken") String accessToken,
                                               @Query("currentTime") String currentTime,
                                               @Query("property") String property);

    @GET("/api/screen/submit")
    Call<HttpResult<DeviceData>> bindDevice(@Query("accessToken") String accessToken,
                                            @Query("bindCode") String bindCode);

    @GET("/api/screen/submit")
    Call<HttpResult<DeviceData>> bindDevice(@Query("accessToken") String accessToken,
                                            @Query("bindCode") String bindCode,
                                            @Query("tempBind") int tempBind);

    @GET("/api/screen/delete-bind")
    Call<HttpResult<String>> unbindDevice(@Query("accessToken") String accessToken,
                                          @Query("lsid") String lsid,
                                          @Query("deleteType") String deleteType);

    @POST("/api/screen/screens-online-status")
    Call<OnlineData> queryFlushDeviceStatus(@Body FlushDeviceStatus screen_ids);


    @GET("/appstore/iot/v1/ss-client-config")
    Call<HttpResult<ApkInfo>> reqVersionCode(@Query("ssClientID") String clientId,
                                         @Query("ssClientVersion") int protoCode);

    @GET("/appstore/appstorev3/appDetail")
    Call<AppStoreResult<AppItem>> checkAppStore(@Query("pkg") String pkg);

    @GET("/api/screen/join-room")
    Call<HttpResult<JoinToLeaveData>> joinRoom(@Query("accessToken") String accessToken,
                                               @Query("roomId") String roomId,
                                               @Query("session") String session);

    @GET("/api/screen/leave-room")
    Call<HttpResult<JoinToLeaveData>> leaveRoom(@Query("accessToken") String accessToken,
                                                @Query("userQuit") String userQuit);

    @POST("/datav/v1/app-event/report")
    Call<HttpResult<Void>> reportLog(@Query("appkey") String appkey,
                                          @Query("time") int time,
                                          @Query("sign") String sign,
                                          @Body ReportData reportLog);

    @POST("/api/screen/broadcast-room-message")
    Call<HttpResult<RoomHasOnline>> sendBroadCastRoomMessage(@Query("accessToken") String accessToken,
                                                             @Body MessageRoomData messageRoomData);

    @GET("/api/screen/room-devices")
    Call<HttpResult<RoomDevices>> getRoomDevices(@Query("accessToken") String accessToken);

    @GET("/api/screen/getQrCode")
    Call<HttpResult<TempQrcode>> getTempQrcode(@Query("accessToken") String accessToken,
                                               @Query("time") long time,
                                               @Query("tempBind") String tempBind,
                                               @Query("sign") String sign);

    @GET("/operation/v1/dongle/cast-list")
    Call<HttpResult<ScreenApps>> getScreenApps(@Query("appkey") String appkey,
                                                   @Query("time") int time,
                                                   @Query("sign") String sign);

    @GET("/api/screen/temp-bind-directly")
    Call<HttpResult<DeviceData>> tempBindDirectDevice(@Query("accessToken") String accessToken,
                                            @Query("lsid") String lsid,
                                            @Query("spaceId") String spaceId);
}
