package swaiotos.runtime.h5.common.event;

import com.coocaa.businessstate.object.BusinessState;

public class OnReportRC {
    private String id;
    private String state;

    public OnReportRC(String id, String state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
