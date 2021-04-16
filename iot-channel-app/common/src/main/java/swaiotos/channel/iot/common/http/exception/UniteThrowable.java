package swaiotos.channel.iot.common.http.exception;

import android.net.ParseException;

import org.json.JSONException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import swaiotos.channel.iot.utils.AndroidLog;


public class UniteThrowable extends Exception {

    public final int errorType;
    public final int errorCode;
    public final String errorMsg;

    private UniteThrowable(Throwable throwable, int errorType, String errorMsg) {
        this(throwable, errorType, -1,errorMsg);
    }


    private UniteThrowable(Throwable throwable, int errorType, int errorCode, String errorMsg) {
        super(throwable);
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static UniteThrowable handleException(Throwable e) {
        UniteThrowable ex;
       if (e instanceof JSONException
               ||e instanceof com.alibaba.fastjson.JSONException
                || e instanceof ParseException) {  // 均视为解析错误
            ex = new UniteThrowable(e, ErrorType.PARSE_ERROR, ErrorType.PARSE_ERROR_MESSAGE);
        } else if (e instanceof NoNetworkException) { // 无网络连接
            ex = new UniteThrowable(e, ErrorType.NETWORK_ERROR, ErrorType.NETWORK_ERROR_MESSAGE);
        } else if (e instanceof UnknownHostException || e instanceof ConnectException) { //网络连接异常
            ex = new UniteThrowable(e, ErrorType.UNKNOWN_HOST_ERROR_CONNECT, ErrorType.UNKNOWN_HOST_ERROR_CONNECT_MESSAGE);
        } else if (e instanceof SocketTimeoutException) { // 超时
           ex = new UniteThrowable(e, ErrorType.NETWORK_TIME_OUT, ErrorType.NETWORK_TIME_OUT_MESSAGE);
       } else if (e instanceof ErrorCodeIOException) { // HTTP错误
           ErrorCodeIOException httpException = (ErrorCodeIOException) e;
            ex = new UniteThrowable(e, ErrorType.HTTP_ERROR, httpException.code(), ErrorType.HTTP_ERROR_MESSAGE);
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new UniteThrowable(e, ErrorType.SSL_ERROR, ErrorType.SSL_ERROR_MESSAGE);
        }  else { // 未知错误
            ex = new UniteThrowable(e, ErrorType.UNKNOWN, ErrorType.UNKNOWN_ERROR_MESSAGE);
        }
        AndroidLog.androidLog("---e:"+e.getMessage());
        return ex;
    }

    /**
     * 约定异常
     */
    public static final class ErrorType {

        /**
         * 字符串解析错误
         */
        public static final int PARSE_ERROR = 1000000;
        public static final String PARSE_ERROR_MESSAGE = "数据解析错误";
        /**
         * 无网络
         */
        public static final int NETWORK_ERROR = 1000001;
        public static final String NETWORK_ERROR_MESSAGE = "网络连接异常，请检查当前网络状态";

        /**
         * 网络连接异常
         */
        public static final int UNKNOWN_HOST_ERROR_CONNECT = 1000002;
        public static final String UNKNOWN_HOST_ERROR_CONNECT_MESSAGE = "网络连接异常,请稍后";
        /**
         * 连接超时
         */
        public static final int NETWORK_TIME_OUT = 1000003;
        public static final String NETWORK_TIME_OUT_MESSAGE = "连接超时,请稍后再试";
        /**
         * HTTP协议错误
         */
        public static final int HTTP_ERROR = 1000004;
        public static final String HTTP_ERROR_MESSAGE = "服务器异常,请稍后再试";
        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1000005;
        public static final String SSL_ERROR_MESSAGE = "服务器证书异常";

        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000006;
        public static final String UNKNOWN_ERROR_MESSAGE = "未知错误";

        public static final int RESPONSE_ERROR = 1000007;
        public static final String RESPONSE_ERROR_MESSAGE = "response data error";
    }
}