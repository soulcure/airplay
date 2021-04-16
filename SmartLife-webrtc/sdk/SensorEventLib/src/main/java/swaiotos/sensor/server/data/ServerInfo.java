package swaiotos.sensor.server.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class ServerInfo implements Serializable {
    public String url;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
