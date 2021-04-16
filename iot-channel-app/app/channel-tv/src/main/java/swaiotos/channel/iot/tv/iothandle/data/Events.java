package swaiotos.channel.iot.tv.iothandle.data;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Events implements Serializable {
    public String event_type;
    public int version;
    public String value;

    public Events(String type, int v, String values) {
        event_type = type;
        version = v;
        value = values;
    }

    public enum Event_Type {
        KEY_EVENT,
        TOUCH_EVENT,
        CUSTOM_EVENT
    }

    public static Events decode(String json) {
        try {
            JSONObject object = new JSONObject(json);
            String event_type = object.getString("event_type");
            int version = Integer.valueOf(object.getString("version"));
            String values = object.getString("value");

            return new Events(event_type, version, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final synchronized String encode() {
        try {
            JSONObject object = new JSONObject();
            object.put("event_type", event_type);
            object.put("version", String.valueOf(version));
            object.put("value", value.toString());
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
