package com.coocaa.smartscreen.data.action;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class NewAction implements Serializable, Parcelable {

    //跳转参数
    public String type;
    public String id;
    public String target;
    public Map<String, String> params;

    private String uri;

    public NewAction() {
    }

    protected NewAction(Parcel in) {
        type = in.readString();
        id = in.readString();
        target = in.readString();
        uri = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(id);
        dest.writeString(target);
        dest.writeString(uri);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NewAction> CREATOR = new Creator<NewAction>() {
        @Override
        public NewAction createFromParcel(Parcel in) {
            return new NewAction(in);
        }

        @Override
        public NewAction[] newArray(int size) {
            return new NewAction[size];
        }
    };

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("NewAction{");
        sb.append("type='").append(type).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", target='").append(target).append('\'');
        sb.append(", params=").append(params);
        sb.append('}');
        return sb.toString();
    }

    public String uri() {
        if(uri != null)
            return uri;

        Uri.Builder builder = new Uri.Builder().scheme(type).encodedAuthority(id).path(target);
        if(params != null) {
            builder.appendQueryParameter("params", new Gson().toJson(params));
        }
        if(params != null) {
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        uri = builder.build().toString();
        return uri;
    }
}
