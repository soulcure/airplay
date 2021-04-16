package swaiotos.runtime.h5.common.event;

import swaiotos.runtime.h5.common.base.BaseEvent;

public class SetNativeUI extends BaseEvent {
    public SetNativeUI(Object data) {
        super(data);
    }

    private String id;
    public SetNativeUI setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
