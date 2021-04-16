package com.coocaa.svg.render;

/**
 * @Author: yuzhan
 */
public class RenderException extends RuntimeException {

    public RenderException() {
        super();
    }

    public RenderException(String msg) {
        super(msg);
    }

    public RenderException(Exception e) {
        super(e);
    }
}
