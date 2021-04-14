package com.swaiot.webrtc.http;

import com.swaiot.webrtc.request.ResRoomMsg;
import com.swaiot.webrtc.response.BindDeviceResp;
import com.swaiot.webrtc.response.LinkCodeResp;
import com.swaiot.webrtc.response.RoomOnlineResp;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;
import rx.Observable;

public interface HttpMethod {
    @GET
    Observable<LinkCodeResp> getLinkCode(@Url String url,@QueryMap Map<String, String> map);

}