package com.coocaa.smartmall.data.api;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartmall.data.tv.data.DetailResult;
import com.coocaa.smartmall.data.tv.data.RecommandResult;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.coocaa.smartmall.data.tv.http.SmartMallRequestService;

/**
 * Description: 公共网络请求接口
 * Create by wzh on 2019-11-13
 */
public class HttpApi {

    private HttpApi() {

    }

    private static class SingletonHolder {
        private static final HttpApi INSTANCE = new HttpApi();
    }

    public synchronized static HttpApi getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 异步请求方法 --- 不需要开子线程
     * @param call
     * @param subscribe
     * @param <T>
     */
    public <T> void request(final Call<T> call, final HttpSubscribe<T> subscribe) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.i("OKHTTP-LOG","线程名："+Thread.currentThread().getName());
                int code = response.raw().code();
                if (code == 200) {
                    Log.i("OKHTTP-LOG","code == 200");
                    T result = response.body();
                    if (result != null) {
                        Log.i("OKHTTP-LOG","subscribe.onSuccess()--->result = "+result.toString());
                        subscribe.onSuccess(result);
                    } else {
                        Log.i("OKHTTP-LOG","subscribe.onError");
                        subscribe.onError(new HttpThrowable(HttpThrowable.ERROR.ServerRetunNullException));
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
                            Log.i("OKHTTP-LOG","errorBody.string() = "+errorBody.string());
                            JSONObject jsonObject = JSONObject.parseObject(errorBody.string());
                            int errCode = Integer.parseInt(jsonObject.get("code").toString());
                            String errMsg = jsonObject.get("msg").toString();
                            subscribe.onError(new HttpThrowable(errCode, errMsg));
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(call, new RuntimeException("parse errorBody exception:" + response.raw().toString()));
                        }
                    } else {
                        onFailure(call, new RuntimeException("response error:" + response.raw().toString()));
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                t.printStackTrace();
                subscribe.onError(new HttpThrowable(t));
            }
        });
    }

    /**
     * 同步请求方法 --- 需要在子线程调用
     *
     * @param call
     * @param <T>
     * @return
     */
    public <T> T requestSync(final Call<T> call) {
        T result = null;
        try {
            result = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
