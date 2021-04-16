package swaiotos.sensor.client.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
public class ClientBusinessInfo implements Serializable {

    /**
     * IOT-Channel对应的ID
     */
    public String clientSSId;

    /**
     * dongle端接收方ID
     */
    public String targetSSId;

    /**
     * 业务名称
     */
    public String businessName;

    /**
     * View的宽度
     */
    public int width;

    /**
     * View的高度
     */
    public int height;

    public int offsetX;
    public int offsetY;
    
    public int protoVersion = -1;

    public ClientBusinessInfo() {

    }

    public ClientBusinessInfo(String client, String target, String name, int w, int h) {
        this.clientSSId = client;
        this.targetSSId = target;
        this.businessName = name;
        this.width = w;
        this.height = h;
    }

    public ClientBusinessInfo(String client, String target, String name, int w, int h, int oX, int oY) {
        this.clientSSId = client;
        this.targetSSId = target;
        this.businessName = name;
        this.width = w;
        this.height = h;
        this.offsetX = oX;
        this.offsetY = oY;
    }

    @NonNull
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
