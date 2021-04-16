package com.coocaa.tvpi.module.pay.http;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class PayData implements Serializable {

    private String sign;
    private String sign_type;
    private String random_str;
    private String js_api_param;




    public void setSign(String sign) {
        this.sign = sign;
    }
    public String getSign() {
        return sign;
    }

    public void setSign_type(String sign_type) {
        this.sign_type = sign_type;
    }
    public String getSign_type() {
        return sign_type;
    }

    public void setRandom_str(String random_str) {
        this.random_str = random_str;
    }
    public String getRandom_str() {
        return random_str;
    }

    public void setJs_api_param(String js_api_param) {
        this.js_api_param = js_api_param;
    }
    public String getJs_api_param() {
        return js_api_param;
    }

    @Override
    public String toString() {
        return "PayData{" +
                "sign='" + sign + '\'' +
                ", sign_type='" + sign_type + '\'' +
                ", random_str='" + random_str + '\'' +
                ", js_api_param='" + js_api_param + '\'' +
                '}';
    }
}