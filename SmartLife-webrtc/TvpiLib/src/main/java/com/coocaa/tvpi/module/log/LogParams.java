package com.coocaa.tvpi.module.log;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class LogParams {

    private Map<String, String> params = new HashMap<>();

    private LogParams() {

    }

    public static LogParams newParams() {
        return new LogParams();
    }

    public LogParams(String key, String value) {
        append(key, value);
    }

    public LogParams(Map<String, String> params) {
        append(params);
    }

    public LogParams append(String key, String value) {
        if(!TextUtils.isEmpty(key) && value != null) {
            params.put(key, value);
        }
        return this;
    }

    public LogParams append(Map<String, String> params) {
        if(params != null) {
            this.params.putAll(params);
        }
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
