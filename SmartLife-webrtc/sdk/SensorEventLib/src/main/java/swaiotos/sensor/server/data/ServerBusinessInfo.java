package swaiotos.sensor.server.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
public class ServerBusinessInfo implements Serializable {

    /**
     * IOT-Channel对应的ID
     */
    public String clientSSId;


    /**
     * 业务名称
     */
    public String businessName;

    public ServerBusinessInfo() {

    }

    public ServerBusinessInfo(String client, String name) {
        this.clientSSId = client;
        this.businessName = name;
    }

    @NonNull
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
