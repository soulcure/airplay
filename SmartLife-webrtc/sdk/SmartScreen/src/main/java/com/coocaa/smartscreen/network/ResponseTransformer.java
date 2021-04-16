package com.coocaa.smartscreen.network;


import android.util.Log;

import com.coocaa.smartscreen.data.BaseResp;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

import static com.coocaa.smartscreen.network.exception.ApiException.NETWORK_ERROR;
import static com.coocaa.smartscreen.network.exception.ApiException.PARSE_ERROR;
import static com.coocaa.smartscreen.network.exception.ApiException.UNKNOWN;

/**
 * Response转换类
 * Created by song on 2020/3/21
 */
public class ResponseTransformer {
    private static final String TAG = "ResponseTransformer";
    public static final int SUCCESS_CODE_0 = 0;    //普通接口正确状态码是0
    public static final int SUCCESS_CODE_1 = 1;    //登录模块，视频通话模块接口正确状态码是1

    /**
     * 处理公共异常（网络，Json解析等）{@link ErrorFunction}，切换线程
     * 接口调用正确时去掉BaseResp包装返回T,接口调用错误时返回带有后台错误信息msg的ApiException {@link ResponseFunction}
     *
     * @param successCode 成功状态码
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<BaseResp<T>, T> handleResult(final int successCode) {
        return new ObservableTransformer<BaseResp<T>, T>() {
            @Override
            public ObservableSource<T> apply(Observable<BaseResp<T>> upstream) {
                return upstream.onErrorResumeNext(new ErrorFunction<T>())
                        .flatMap(new ResponseFunction<T>(successCode))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private static class ErrorFunction<T> implements Function<Throwable, ObservableSource<? extends BaseResp<T>>> {

        @Override
        public ObservableSource<? extends BaseResp<T>> apply(Throwable throwable) throws Exception {
            return Observable.error(handlePublicException(throwable));
        }
    }

    private static class ResponseFunction<T> implements Function<BaseResp<T>, ObservableSource<T>> {

        private int successCode;

        public ResponseFunction(int successCode) {
            this.successCode = successCode;
        }

        @Override
        public ObservableSource<T> apply(BaseResp<T> baseResp) {
            int code = baseResp.code;
            if (code == successCode) {
                T data = baseResp.data;
                return Observable.just(data);
            } else {
                String message = baseResp.msg;
                return Observable.error(new ApiException(code, message));
            }
        }
    }


    /**
     * 处理公共异常（网络，Json解析等），切换线程{@link ErrorFunction}，
     *
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> handException() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.onErrorResumeNext(new ErrorFunctionNoBaseResp<T>())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private static class ErrorFunctionNoBaseResp<T> implements Function<Throwable, ObservableSource<? extends T>> {

        @Override
        public ObservableSource<? extends T> apply(Throwable throwable) throws Exception {
            return Observable.error(handlePublicException(throwable));
        }
    }


    public static ApiException handlePublicException(Throwable e) {
        Log.d(TAG, "handlePublicException: " + e.toString());
        if (e instanceof JsonParseException
                || e instanceof JSONException
                || e instanceof ParseException) {
            //解析错误
            return new ApiException(PARSE_ERROR, e.toString());
        } else if (e instanceof ConnectException) {
            //网络错误
           return new ApiException(NETWORK_ERROR, "网络连接异常");
        } else if (e instanceof UnknownHostException || e instanceof SocketTimeoutException ) {
            //连接错误
           return new ApiException(NETWORK_ERROR,"网络异常");
        } else if (e instanceof HttpException) {
            //兼容某些接口错误和异常请求状态放在400中返回
            //retrofit2.adapter.rxjava2.HttpException: HTTP 400 Bad Request https://passport.coocaa.com/skyapi/user/login/mobile?sign=de9f0aee4d4e3e3ef550109f7ea9d928&time=1595325721&client_id=7050748df941410d8ed46172fb72eefe (166ms)
            //OkHttpClient: Date: Tue, 21 Jul 2020 10:02:58 GMT
            //OkHttpClient: Content-Type: application/json;charset=UTF-8
            //OkHttpClient: Content-Length: 61
            //OkHttpClient: Connection: keep-alive
            //OkHttpClient: Server: openresty
            //OkHttpClient: {"code":20204,"msg":"验证码错误,还有9次验证机会"}
            if(((HttpException) e).code() == 500){
                return new ApiException(UNKNOWN, "服务器异常");
            }else {
                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                try {
                    String res = responseBody.string();
                    JSONObject jsonObject = new JSONObject(res);
                    int code = jsonObject.optInt("code");
                    String msg = jsonObject.optString("msg");
                    return new ApiException(code, msg);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    return new ApiException(PARSE_ERROR, "数据读取异常");
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                    return new ApiException(PARSE_ERROR, "数据解析异常");
                }
            }
        } else {
            //未知错误
            return new ApiException(UNKNOWN, "服务器异常");
        }
    }
}
