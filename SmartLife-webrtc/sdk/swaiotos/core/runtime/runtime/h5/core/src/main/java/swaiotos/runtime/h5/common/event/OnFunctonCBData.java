package swaiotos.runtime.h5.common.event;


import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class OnFunctonCBData implements Serializable {
//    public String event;
//
//    public String functionId;
//    public int code;
    public com.alibaba.fastjson.JSONObject data;

    public OnFunctonCBData() {
    }

    public OnFunctonCBData(JSONObject data) {
//        this.event = event;
//        this.functionId = functionId;
//        this.code = code;
        this.data = data;
    }

    private String id;
    public OnFunctonCBData setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "OnFunctonCBData{" +
                "data=" + data +
                '}';
    }
}
