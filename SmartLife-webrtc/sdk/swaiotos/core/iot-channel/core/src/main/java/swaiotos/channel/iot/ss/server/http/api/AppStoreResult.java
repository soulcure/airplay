package swaiotos.channel.iot.ss.server.http.api;

import java.io.Serializable;

/**
 * Description: 网络接口实体基类
 * Create by wzh on 2019-11-13
 */
public class AppStoreResult<T> implements Serializable {
    public int ret;
    public String msg;
    public T data;
}
