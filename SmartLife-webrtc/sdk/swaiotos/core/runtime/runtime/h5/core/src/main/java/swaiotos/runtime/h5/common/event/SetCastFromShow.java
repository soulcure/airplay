package swaiotos.runtime.h5.common.event;

import swaiotos.runtime.h5.common.base.BaseEvent;

public class SetCastFromShow extends BaseEvent {
    public SetCastFromShow(Integer showFlag) {
        super(showFlag);
    }

    private String id;
    public SetCastFromShow setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
