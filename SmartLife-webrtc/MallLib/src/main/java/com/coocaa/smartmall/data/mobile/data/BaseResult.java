package com.coocaa.smartmall.data.mobile.data;

import java.io.Serializable;

public class BaseResult implements Serializable {

    /**
     * state : true
     * code : 200
     * msg : 成功
     */

    private boolean state;
    private int code;
    private String msg;

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
