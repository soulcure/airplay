package swaiotos.runtime.h5.common.event;

import swaiotos.runtime.h5.common.base.BaseEvent;

public class SetLeftBtnEvent extends BaseEvent {
    public SetLeftBtnEvent(String typeString) {
        super(typeString);
    }

    private String id;
    public SetLeftBtnEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }
}
