package com.coocaa.movie.web.base;

public class HttpResult<T> {
    public int code = 0;
    public String msg = "";
    public T data;
}