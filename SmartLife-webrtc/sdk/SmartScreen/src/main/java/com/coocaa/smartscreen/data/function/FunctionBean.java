package com.coocaa.smartscreen.data.function;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class FunctionBean implements Serializable, Parcelable {
    public String name;
    public String subtitle;
    public String icon;
    public String type;
    public String id;
    public String target;
    public String fragment;
    public int quantity;
    public Map<String, String> params;
    public Map<String, String> runtime;
//    public String params;

    private String uri;

    public FunctionBean() {

    }

    protected FunctionBean(Parcel in) {
        name = in.readString();
        subtitle = in.readString();
        icon = in.readString();
        type = in.readString();
        id = in.readString();
        target = in.readString();
        fragment = in.readString();
        quantity = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(subtitle);
        dest.writeString(icon);
        dest.writeString(type);
        dest.writeString(id);
        dest.writeString(target);
        dest.writeString(fragment);
        dest.writeInt(quantity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FunctionBean> CREATOR = new Creator<FunctionBean>() {
        @Override
        public FunctionBean createFromParcel(Parcel in) {
            return new FunctionBean(in);
        }

        @Override
        public FunctionBean[] newArray(int size) {
            return new FunctionBean[size];
        }
    };

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String uri() {
        if(uri != null)
            return uri;
        Uri.Builder builder = new Uri.Builder().scheme(type).encodedAuthority(id).path(target);
        if(params != null) {
            builder.appendQueryParameter("params", new Gson().toJson(params));
        }
        if(!TextUtils.isEmpty(icon)) {
            builder.appendQueryParameter("icon", icon);
        }
        builder.appendQueryParameter("name", name);
        if(runtime == null) {
            runtime = new HashMap<>();
        }
        if(!runtime.containsKey("RUNTIME_NETWORK_FORCE_KEY") && type != null && type.startsWith("np")) {
            runtime.put("RUNTIME_NETWORK_FORCE_KEY", SSConnectManager.FORCE_LAN);
        }
        if(runtime != null) {
            builder.appendQueryParameter("runtime", new Gson().toJson(runtime));
        }
        if(!TextUtils.isEmpty(fragment)) {
            builder.fragment(fragment);
        }
        uri = builder.build().toString();
        return uri;
    }
}