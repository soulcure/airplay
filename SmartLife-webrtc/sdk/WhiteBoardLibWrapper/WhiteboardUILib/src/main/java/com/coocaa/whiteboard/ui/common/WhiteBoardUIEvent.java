package com.coocaa.whiteboard.ui.common;

import java.io.Serializable;

public class WhiteBoardUIEvent implements Serializable {
    public String doWhat;

    public WhiteBoardUIEvent(String doWhat) {
        this.doWhat = doWhat;
    }

    public final static String DO_WHAT_EXIT = "DO_WHAT_EXIT";

    @Override
    public String toString() {
        return "WhiteBoardUIEvent{" +
                "doWhat='" + doWhat + '\'' +
                '}';
    }
}
