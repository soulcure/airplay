package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

public class BindCodeMsgResp implements Serializable {

    private String code;
    private String msg;
    private BindCodeMsg data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public BindCodeMsg getData() {
        return data;
    }

    public void setData(BindCodeMsg data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BindCodeMsgResp{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
