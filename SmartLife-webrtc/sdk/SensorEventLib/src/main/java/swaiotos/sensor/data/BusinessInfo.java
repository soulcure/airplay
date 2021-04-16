package swaiotos.sensor.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
public class BusinessInfo implements Serializable {

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

    public BusinessInfo() {

    }

    public BusinessInfo(String client, String target, String name, int w, int h) {
        this.clientSSId = client;
        this.targetSSId = target;
        this.businessName = name;
        this.width = w;
        this.height = h;
    }

    @NonNull
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
