package swaiotos.runtime.h5.common.event;

import java.io.Serializable;

public class OnJsCallbackData implements Serializable {
    public String id;
    public String method;
    public String data;

    public OnJsCallbackData() {
    }

    public OnJsCallbackData(String id, String method, String data) {
        this.id = id;
        this.method = method;
        this.data = data;
    }

    @Override
    public String toString() {
        return "OnJsCallbackData{" +
                "id='" + id + '\'' +
                ", method='" + method + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
