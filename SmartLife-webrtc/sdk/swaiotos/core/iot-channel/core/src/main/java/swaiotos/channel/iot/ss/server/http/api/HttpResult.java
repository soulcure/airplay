package swaiotos.channel.iot.ss.server.http.api;

import java.io.Serializable;

/**
 * Description: 网络接口实体基类
 * Create by wzh on 2019-11-13
 */
public class HttpResult<T> implements Serializable {
    public String code;
    public String msg;
    public T data;
}
