package swaiotos.channel.iot.common.http.net;

import java.util.Map;

import swaiotos.channel.iot.common.response.CooCaaResponse;


public interface CooCaaRestApi {

    void reflush();

    <T extends CooCaaResponse> void queryAIotCooCaaResponse(String url, Map<String, String> querys, Class<T> cls, RequestListener<T> requestListener);

    <T extends CooCaaResponse> void querySynAIotCooCaaResponse(String url, Map<String, String> querys, Class<T> cls, RequestListener<T> requestListener);

     interface RequestListener<T> {
        /**
         * 请求成功回调
         * @param t 返回结果
         */
        void requestSuccess(T t);

        /**
         *请求失败回调
         * @param errorType :自定义错误码类型
         * @param errorCode
         * -1：自定义错误码   其他错误码服务器请求返回  比如 400  500 502等
         * @param errorMessage 错误消息
         */
        void requestError(int errorType, int errorCode, String errorMessage);
    }
}