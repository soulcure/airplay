package swaiotos.channel.iot.common.http.exception;

import java.io.IOException;

/**
 * 带http响应码的异常，便于错误处理时，根据响应码执行操作
 */
public class ErrorCodeIOException extends IOException {
    private int responseCode;

    public ErrorCodeIOException(String msg, int responseCode) {
        super(msg);
        this.responseCode = responseCode;
    }

    public ErrorCodeIOException(String msg) {
        super(msg);
    }

    public int code() {
        return this.responseCode;
    }
}
