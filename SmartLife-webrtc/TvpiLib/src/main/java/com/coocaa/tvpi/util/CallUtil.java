package com.coocaa.tvpi.util;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.tvpi.module.login.UserInfoCenter;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author kangwen
 * @date 2020/8/17.
 */
public class CallUtil {

    public static final String TAG = CallUtil.class.getSimpleName();

    /**
     * 上传未接来电次数
     *
     * @param accessToken
     * @param friendId
     */
    public synchronized static void uploadMissCallNum(String accessToken, String friendId) {
        Log.d(TAG, "uploadMissCallNum");
        HashMap<String, String> hashMap = new HashMap<>();
        if (TextUtils.isEmpty(accessToken)) {
            accessToken = UserInfoCenter.getInstance().getCoocaaUserInfo().getAccessToken();
        }
        hashMap.put("accessToken", accessToken);
        hashMap.put("yxFriendId", friendId);
        hashMap.put("isConnect", "0");
        Call<ResponseBody> call = NetWorkManager.getInstance().getApiService()
                .addCallLog(hashMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG,"CODE:"+response.code() + "  message:"+response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "uploadMissCallNum:Failure");
                t.getStackTrace();
            }
        });
    }
}
