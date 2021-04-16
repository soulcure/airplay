/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.coocaa.smartscreen.network.api;

import com.coocaa.smartscreen.data.BaseResp;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.AccountLoginInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.device.BindCodeMsgResp;
import com.coocaa.smartscreen.data.device.PadUserInfo;
import com.coocaa.smartscreen.data.device.Register3rdDeviceData;
import com.coocaa.smartscreen.data.device.RegisterLoginResp;
import com.coocaa.smartscreen.data.device.TvProperty;
import com.coocaa.smartscreen.data.device.UpdateDeviceInfoResp;
import com.coocaa.smartscreen.data.device.ValidCode;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import static com.coocaa.smartscreen.network.api.Api.COOCAA_ACCOUNT_DOMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * ================================================
 * Created by wuhaiyuan on 28/11/2019 11:49
 * ================================================
 * 登录相关接口
 */
public interface CoocaaAccountApiService {

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/common/getValidCode")
    @FormUrlEncoded
    Observable<ResponseBody> getImageCaptcha(@QueryMap Map<String, Object> queryMap,
                                             @FieldMap Map<String, Object> fieldMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/common/captcha")
    @FormUrlEncoded
    Observable<BaseResp<Void>> getSmsCaptcha(@QueryMap Map<String, Object> queryMap,
                                                             @FieldMap Map<String, Object> fieldMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/common/captcha-new")
    @FormUrlEncoded
    Observable<BaseResp<Void>> getSmsCaptchaWithImageCaptcha(@QueryMap Map<String, Object> queryMap,
                                                             @FieldMap Map<String, Object> fieldMap);



    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/user/login/mobile")
    @FormUrlEncoded
    Observable<AccountLoginInfo> smsLoginServer(@QueryMap Map<String, Object> queryMap,
                                                @FieldMap Map<String, Object> fieldMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/skyapi/user/sy/login")
    Observable<BaseResp<AccountLoginInfo>> oneClickLogin(@QueryMap Map<String, String> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/v2/user-info")
    @FormUrlEncoded
    Observable<BaseResp<YunXinUserInfo>> getYXUserInfo(@FieldMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/api/user/info")
    @FormUrlEncoded
    Observable<CoocaaUserInfo> getCoocaaUserInfo(@QueryMap Map<String, Object> queryMap,
                                                           @FieldMap Map<String, Object> fieldMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/api/user/update")
    @FormUrlEncoded
    Observable<String> updateCoocaaUserInfo(@QueryMap Map<String, Object> queryMap,
                                                           @FieldMap Map<String, Object> fieldMap);
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/api/user/avatar-base64")
    @FormUrlEncoded
    Observable<ResponseBody> updateCoocaaAvatar(@QueryMap Map<String, Object> queryMap,
                                            @FieldMap Map<String, Object> fieldMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/user/exchange-tp-token")
    Observable<TpTokenInfo> getTpToken(@QueryMap Map<String, Object> queryMap);


    //智慧屏业务
    //ozh-设备注册接口
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/register-login")
    Observable<RegisterLoginResp> registerDevice(@QueryMap Map<String, String> queryMap);

    //ozh-根据token修改设备硬件信息
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/update-deviceInfo")
    Observable<UpdateDeviceInfoResp> updateDeviceInfo(@QueryMap Map<String, String> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/getTvProperty")
//    @GET("http://172.20.151.162:8087/api/screen/getTvProperty")
    Observable<BaseResp<TvProperty>> getTvProperty(@QueryMap Map<String, String> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/get-temp-bind-code")
    Observable<BindCodeMsgResp> getBindCode(@QueryMap Map<String, String> queryMap);

    //ozh-设备注册接口 pad 使用
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME,
                    "Content-Type: application/json"})
    @POST("/api/screen/register-thirdparty-device")
//    @FormUrlEncoded
    Observable<BaseResp<Register3rdDeviceData>> register3rdDevice(@QueryMap Map<String, String> queryMap, @Body Map<String, String> bodyMap);

    //ozh-获取验证码接口 pad 使用
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/valid-code")
    Observable<BaseResp<ValidCode>> getValideCode(@Header("cudid") String activeId, @QueryMap Map<String, String> queryMap);

    //ozh-设备注册接口 pad 使用
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/api/screen/register-login")
    Observable<RegisterLoginResp> registerDevice(@Header("cudid") String activeId, @QueryMap Map<String, String> queryMap);

    //ozh-获取验证码接口 pad 使用
    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME,})
    @GET("/api/screen/userinfo")
    Observable<BaseResp<PadUserInfo>> getUserInfo(@QueryMap Map<String, String> queryMap);
}
