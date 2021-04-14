package swaiotos.channel.iot.http;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;
import swaiotos.channel.iot.response.BaseResp;
import swaiotos.channel.iot.response.BindDeviceResp;
import swaiotos.channel.iot.response.DeviceDataResp;
import swaiotos.channel.iot.response.DeviceStatusResp;
import swaiotos.channel.iot.request.ReqDevices;
import swaiotos.channel.iot.request.ResRoomMsg;
import swaiotos.channel.iot.response.RoomOnlineResp;
import swaiotos.channel.iot.response.UnbindResp;

public interface HttpMethod {
    //获取绑定设备列表
    //accessToken
    //queryType
    //sign
    @GET("/api/screen/query-bind-device")
    Observable<BindDeviceResp> getBindDevice(@QueryMap Map<String, String> map);


    @GET("/api/screen/submit")
    Observable<DeviceDataResp> bindDevice(@Query("accessToken") String accessToken,
                                          @Query("bindCode") String bindCode);

    @GET("/api/screen/delete-bind")
    Observable<UnbindResp> unbindDevice(@Query("accessToken") String accessToken,
                                        @Query("lsid") String lsid,
                                        @Query("deleteType") int deleteType);

    @POST("/api/screen/screens-online-status")
    Observable<DeviceStatusResp> refreshOnlineStatus(@Body ReqDevices sids);


    @GET("/api/screen/join-room")
    Observable<BaseResp> joinRoom(@Query("accessToken") String accessToken,
                                  @Query("roomId") String roomId,
                                  @Query("session") String session);

    @GET("/api/screen/leave-room")
    Observable<BaseResp> leaveRoom(@Query("accessToken") String accessToken,
                                   @Query("userQuit") String userQuit);


    @POST("/api/screen/broadcast-room-message")
    Observable<RoomOnlineResp> sendBroadCastRoomMessage(@Query("accessToken") String accessToken,
                                                                    @Body ResRoomMsg resRoomMsg);

}