package com.coocaa.smartsdk.pay;

import java.io.Serializable;

public class PayRequestEvent implements Serializable {
    public String id;
    public String json;
    public int status;

    public PayRequestEvent() {

    }

    public PayRequestEvent(String id, String json) {
        this.id = id;

        this.json = json;
    }

    public final static int STATUS_SUCCESS = 0;
    public final static int STATUS_FAIL = 1;
    public final static int STATUS_CANCEL = 2;
}
