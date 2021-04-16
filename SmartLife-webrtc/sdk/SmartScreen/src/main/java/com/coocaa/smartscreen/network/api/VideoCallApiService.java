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
import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.videocall.ContactsResp;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

import static com.coocaa.smartscreen.network.api.Api.COOCAA_ACCOUNT_DOMAIN_NAME;
import static me.jessyan.retrofiturlmanager.RetrofitUrlManager.DOMAIN_NAME_HEADER;

/**
 * 视频通话相关后台接口
 * Created by songxing on 2020/7/1
 */
public interface VideoCallApiService {

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/friend-list")
    @FormUrlEncoded
    Observable<BaseResp<List<ContactsResp>>> getFriendList(@FieldMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/add-friend")
    Observable<BaseResp<YunXinUserInfo>> addFriends(@QueryMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/update-nickname")
    Observable<BaseResp<Void>> updateFriendsNickname(@QueryMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/delete-friend")
    Observable<BaseResp<Void>> deleteFriends(@QueryMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/is-friend")
    Observable<BaseResp<Void>> isFriends(@QueryMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @POST("/videocall/yxLogin/agree-refund-add-friend")
    @FormUrlEncoded
    Observable<BaseResp<Void>> agreeRefundAddFriends(@FieldMap Map<String, Object> queryMap);

    @Headers({DOMAIN_NAME_HEADER + COOCAA_ACCOUNT_DOMAIN_NAME})
    @GET("/videocall/yxLogin/push-to-tv")
    Observable<BaseResp<Void>> pushToTv(@QueryMap Map<String, String> queryMap);
}
