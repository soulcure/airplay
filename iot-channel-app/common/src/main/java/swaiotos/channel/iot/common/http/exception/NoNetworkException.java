package swaiotos.channel.iot.common.http.exception;

import java.io.IOException;

/**
 * Created by Wangzj on 2017/6/7.
 */

public class NoNetworkException extends IOException {
    public NoNetworkException() {
    }

    public NoNetworkException(Throwable cause) {
        super(cause);
    }
}
