package com.coocaa.smartsdk.pay;

import java.io.Serializable;

public class SubmitPayEvent implements Serializable {
    public int status;
    public String json;
    public String id;

    public SubmitPayEvent() {

    }

    public SubmitPayEvent(int s, String id, String json) {
        status = s;
        this.id = id;
        this.json = json;
    }

    @Override
    public String toString() {
        return "PayResultEvent{" +
                "status=" + status +
                ", json='" + json + '\'' +
                '}';
    }

    public final static int STATUS_SUCCESS = 0;
    public final static int STATUS_FAIL = 1;
    public final static int STATUS_CANCEL = 2;
}
