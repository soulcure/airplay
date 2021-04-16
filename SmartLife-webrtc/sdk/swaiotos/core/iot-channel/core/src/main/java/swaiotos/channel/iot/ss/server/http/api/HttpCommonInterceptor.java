package swaiotos.channel.iot.ss.server.http.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Description: header公共参数设置
 * Create by wzh on 2019-11-13
 */
public class HttpCommonInterceptor implements Interceptor {

    private Map<String, String> mHeaderMap = new HashMap<>();

    public HttpCommonInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request old = chain.request();
        Request.Builder requestBuilder = old.newBuilder();
        requestBuilder.method(old.method(), old.body());
        if (mHeaderMap.size() > 0) {
            for (Map.Entry<String, String> params : mHeaderMap.entrySet()) {
                requestBuilder.header(params.getKey(), params.getValue());
            }
        }
        return chain.proceed(requestBuilder.build());
    }

    public static class Builder {
        HttpCommonInterceptor mHttpCommonInterceptor;

        public Builder() {
            mHttpCommonInterceptor = new HttpCommonInterceptor();
        }

        public Builder addHeaderParams(Map<String, String> headers) {
            mHttpCommonInterceptor.mHeaderMap.putAll(headers);
            return this;
        }

        public Builder addHeaderParams(String key, String value) {
            mHttpCommonInterceptor.mHeaderMap.put(key, value);
            return this;
        }

        public Builder addHeaderParams(String key, int value) {
            return addHeaderParams(key, String.valueOf(value));
        }

        public Builder addHeaderParams(String key, float value) {
            return addHeaderParams(key, String.valueOf(value));
        }

        public Builder addHeaderParams(String key, long value) {
            return addHeaderParams(key, String.valueOf(value));
        }

        public Builder addHeaderParams(String key, double value) {
            return addHeaderParams(key, String.valueOf(value));
        }

        public HttpCommonInterceptor builder() {
            return mHttpCommonInterceptor;
        }
    }

}
