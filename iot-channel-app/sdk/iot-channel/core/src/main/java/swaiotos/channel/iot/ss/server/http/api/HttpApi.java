package swaiotos.channel.iot.ss.server.http.api;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import swaiotos.channel.iot.ss.analysis.UserBehaviorAnalysis;
import swaiotos.channel.iot.utils.ThreadManager;

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
                    T result = response.body();
                    if (result != null) {
                        subscribe.onSuccess(result);
                    } else {
                        subscribe.onError(new HttpThrowable(HttpThrowable.ERROR.ServerRetunNullException));
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
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
                HttpThrowable httpThrowable = new HttpThrowable(t);
                subscribe.onError(httpThrowable);
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
        long requestStartTime = System.currentTimeMillis();
        T result = null;
        try {
            result = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 异步请求方法 --- 不需要开子线程
     * @param call
     * @param subscribe
     * @param <T>
     */
    public <T> void request(final Call<T> call, final HttpSubscribe<T> subscribe, final String interfaceName,final String sid) {
        final long requestStartTime = System.currentTimeMillis();
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.i("OKHTTP-LOG","线程名："+Thread.currentThread().getName());
                int code = response.raw().code();
                if (code == 200) {
                    T result = response.body();
                    if (result != null) {
                        subscribe.onSuccess(result);
                    } else {
                        subscribe.onError(new HttpThrowable(HttpThrowable.ERROR.ServerRetunNullException));
                    }
                    if (!TextUtils.isEmpty(interfaceName)) {
                        UserBehaviorAnalysis.reportServerInterfaceSuccess(sid, System.currentTimeMillis() - requestStartTime,interfaceName);
                    }

                } else {
                    ResponseBody errorBody = response.errorBody();
                    if (errorBody != null) {
                        try {
                            JSONObject jsonObject = JSONObject.parseObject(errorBody.string());
                            int errCode = Integer.parseInt(jsonObject.get("code").toString());
                            String errMsg = jsonObject.get("msg").toString();
                            subscribe.onError(new HttpThrowable(errCode, errMsg));
                            if (!TextUtils.isEmpty(interfaceName)) {
                                UserBehaviorAnalysis.reportServerInterfaceError(sid,interfaceName,errCode+"",errMsg);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            onFailure(call, new RuntimeException("parse errorBody exception:" + response.raw().toString()));
                            if (!TextUtils.isEmpty(interfaceName)) {
                                UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, "-100", "parse errorBody exception:" + response.raw().toString());
                            }
                        }
                    } else {
                        onFailure(call, new RuntimeException("response error:" + response.raw().toString()));
                        if (!TextUtils.isEmpty(interfaceName)) {
                            UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, "-100", "response error:" + response.raw().toString());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                t.printStackTrace();
                HttpThrowable httpThrowable = new HttpThrowable(t);
                subscribe.onError(httpThrowable);
                if (!TextUtils.isEmpty(interfaceName)) {
                    UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, httpThrowable.getErrCode() + "", httpThrowable.getErrMsg());
                }
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
    public <T> T requestSync(final Call<T> call,final String interfaceName,final String sid) {
        long requestStartTime = System.currentTimeMillis();
        T result = null;
        try {
            final Response<T> response = call.execute();
            int code = response.raw().code();
            if (code == 200) {
                result = response.body();
                if (!TextUtils.isEmpty(interfaceName)) {
                    UserBehaviorAnalysis.reportServerInterfaceSuccess(sid, System.currentTimeMillis() - requestStartTime,interfaceName);
                }
            } else {
                ResponseBody errorBody = response.errorBody();
                if (errorBody != null) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(errorBody.string());
                        int errCode = Integer.parseInt(jsonObject.get("code").toString());
                        String errMsg = jsonObject.get("msg").toString();
                        if (!TextUtils.isEmpty(interfaceName)) {
                            UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, errCode + "", errMsg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (!TextUtils.isEmpty(interfaceName)) {
                            UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, "-100", "parse errorBody exception:" + response.raw().toString());
                        }
                    }
                } else {
                    if (!TextUtils.isEmpty(interfaceName)) {
                        UserBehaviorAnalysis.reportServerInterfaceError(sid, interfaceName, "-100", "response error:" + response.raw().toString());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}
