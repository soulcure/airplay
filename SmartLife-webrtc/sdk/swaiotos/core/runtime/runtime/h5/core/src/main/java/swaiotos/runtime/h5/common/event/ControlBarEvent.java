package swaiotos.runtime.h5.common.event;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class ControlBarEvent implements Serializable {
    public boolean visible;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
