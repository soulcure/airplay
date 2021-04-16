package com.coocaa.movie.web.base;

import java.util.List;

public class HttpListResult<T> {
    public int code = 0;
    public String msg = "";
    public List<T> data;
}