package swaiotos.sensor.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * @Author: yuzhan
 */
public class DeviceInfo implements Serializable {

    @NonNull
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
