package com.coocaa.sdk.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Session implements Parcelable {
    private static final String BROADCAST_SID = "sid-broadcast";
    public static final Session BROADCAST = new Session(BROADCAST_SID);

    private String mSid;
    private Map<String, String> mExtra = new HashMap<>();


    public static final class Builder {
        public static Session create(String id) {
            return new Session(id);
        }

        public static Session decode(String in) throws JSONException {
            JSONObject object = new JSONObject(in);
            Session session = new Session();
            session.mSid = object.getString("id");
            JSONObject extra = object.getJSONObject("extra");
            Iterator<String> keys = extra.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                session.mExtra.put(key, extra.getString(key));
            }
            return session;
        }
    }


    public Session(String sid) {
        this.mSid = sid;
    }


    public boolean isBroadcast() {
        return mSid.equals(BROADCAST_SID);
    }

    public void setId(String mId) {
        this.mSid = mId;
    }

    public final String getId() {
        return mSid;
    }

    public final String getExtra(String key) {
        return mExtra.get(key);
    }

    public final Map<String, String> getExtras() {
        return new HashMap<>(mExtra);
    }

    public final void putExtra(String key, String value) {
        mExtra.put(key, value);
    }

    public final String encode() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", mSid);
            object.put("extra", new JSONObject(mExtra));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Session session = (Session) o;
        return mSid.equals(session.mSid);
    }

    @Override
    public int hashCode() {
        return mSid.hashCode();
    }

    @Override
    public String toString() {
        return encode();
    }

    public Session() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSid);
        dest.writeInt(this.mExtra.size());
        for (Map.Entry<String, String> entry : this.mExtra.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    protected Session(Parcel in) {
        this.mSid = in.readString();
        int mExtraSize = in.readInt();
        this.mExtra = new HashMap<String, String>(mExtraSize);
        for (int i = 0; i < mExtraSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.mExtra.put(key, value);
        }
    }

    public static final Creator<Session> CREATOR = new Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel source) {
            return new Session(source);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
}
