package swaiotos.channel.iot.ss.controller;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.JSON;

/**
 * @ClassName: DeviceState
 * @Author: lu
 * @CreateDate: 2020/4/22 5:44 PM
 * @Description:
 */
public class DeviceState implements Parcelable {
    private String mLsid;
    private Map<String, String> mConnectiveInfo;
    private Map<String, String> mClientInfo;

    public Session toSession() {
        Session session = new Session();
        session.setId(mLsid);

        Set<String> keys = mConnectiveInfo.keySet();
        for (String key : keys) {
            session.putExtra(key, mConnectiveInfo.get(key));
        }

        return session;
    }

    public DeviceState() {
    }

    public static DeviceState parse(String in) {
//        return com.alibaba.fastjson.JSONObject.parseObject(in, DeviceState.class);
        try {
            JSONObject object = new JSONObject(in);
            String lsid = object.getString("lsid");
            Map<String, String> connectiveInfo = JSON.parse(in, "connective");
            Map<String, String> clientInfo = JSON.parse(in, "client");
            return new DeviceState(lsid, connectiveInfo, clientInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DeviceState(String lsid) {
        this(lsid, new HashMap<String, String>(), new HashMap<String, String>());
    }

    DeviceState(String lsid, Map<String, String> connectiveInfo, Map<String, String> clientInfo) {
        mLsid = lsid;
        mConnectiveInfo = new HashMap<>(connectiveInfo);
        mClientInfo = new HashMap<>(clientInfo);
    }

    DeviceState(Parcel source) {
        this.mLsid = source.readString();
        this.mConnectiveInfo = source.readHashMap(Map.class.getClassLoader());
        this.mClientInfo = source.readHashMap(Map.class.getClassLoader());
    }

    public void setLsid(String lsid) {
        this.mLsid = lsid;
    }

    public String getLsid() {
        return mLsid;
    }

    public final boolean putConnectiveInfo(String key, String value) {
        synchronized (mConnectiveInfo) {
            mConnectiveInfo.put(key, value);
            return true;
        }
    }

    public final Set<String> getConnectivies() {
        synchronized (mConnectiveInfo) {
            return mConnectiveInfo.keySet();
        }
    }

    public final boolean putClientInfo(String key, String value) {
        synchronized (mClientInfo) {
            boolean r = false;
            String v = mClientInfo.get(key);
            if (v == null || !v.equals(value)) {
                r = true;
                mClientInfo.put(key, value);
            }
            return r;
        }
    }

    public final boolean removeClientInfo(String key) {
        synchronized (mClientInfo) {
            if (mClientInfo.containsKey(key)) {
                mClientInfo.remove(key);
                return true;
            }
            return false;
        }
    }

    public final Set<String> getClients() {
        synchronized (mClientInfo) {
            return mClientInfo.keySet();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLsid);
        dest.writeMap(mConnectiveInfo);
        dest.writeMap(mClientInfo);
    }

    public String encode() {
//        return JSONObject.toJSONString(this);
        JSONObject object = new JSONObject();
        try {
            object.put("lsid", mLsid);
            object.put("connective", new JSONObject(mConnectiveInfo));
            object.put("client", new JSONObject(mClientInfo));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public static final Creator<DeviceState> CREATOR = new Creator<DeviceState>() {
        @Override
        public DeviceState createFromParcel(Parcel source) {
            return new DeviceState(source);
        }

        @Override
        public DeviceState[] newArray(int size) {
            return new DeviceState[size];
        }
    };
}
