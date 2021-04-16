package com.coocaa.movie.web.base;

/**
 * Created by luwei on 16-10-19.
 */

public class HttpExecption extends RuntimeException{
    private int code = 0;
    private String msg = "";

    public HttpExecption(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public HttpExecption(Throwable e){
        super(e);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return super.toString() + ", msg=" + msg + ", code=" + code;
    }
}
