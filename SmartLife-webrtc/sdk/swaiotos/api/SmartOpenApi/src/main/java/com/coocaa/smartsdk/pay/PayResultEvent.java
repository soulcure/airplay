package com.coocaa.smartsdk.pay;

import java.io.Serializable;

public class PayResultEvent implements Serializable {
    public String status;

    public String id;

    public PayResultEvent() {

    }

    public PayResultEvent(String s, String id) {
        status = s;
        this.id = id;
    }

    @Override
    public String toString() {
        return "PayResultEvent{" +
                "status='" + status + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public final static String STATUS_SUCCESS = "success";
    public final static String STATUS_FAIL = "fail";
    public final static String STATUS_CANCEL = "cancel";
}
