package swaiotos.channel.iot.im;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Message {
    public enum CMD {
        CONNECT,
        DISCONNECT,
        UPDATE,
        GET_CLIENT,
        REPLY,
        ONLINE,
        OFFLINE,
        BIND,
        UNBIND,
        UPDATE_DEVICE_INFO,
        JOIN,
        LEAVE
    }


    final String id;
    final long timestamp;
    final CMD cmd;
    final String source;
    final Map<String, String> payload;

    Message(String id, CMD cmd, String source, Map<String, String> payload) {
        this.id = id;
        this.cmd = cmd;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
        this.payload = payload != null ? payload : new LinkedHashMap<String, String>();
    }

    Message(String in) throws JSONException {
        JSONObject object = new JSONObject(in);
        id = object.getString("id");
        cmd = CMD.valueOf(object.getString("cmd"));
        source = object.getString("source");
        timestamp = object.getLong("timestamp");
        payload = new LinkedHashMap<>();
        JSONObject extra = object.getJSONObject("payload");
        Iterator<String> keys = extra.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            payload.put(key, extra.getString(key));
        }
    }

    public Message(Message.CMD cmd, String source) {
        this(cmd, source, null);
    }

    public Message(Message.CMD cmd, String source, Map<String, String> payload) {
        this(UUID.randomUUID().toString(), cmd, source, payload);
    }

    public Message reply(String source) {
        return new Message(id, Message.CMD.REPLY, source, null);
    }

    public String getPayload(String key) {
        return payload.get(key);
    }

    public void putPayload(String key, String value) {
        payload.put(key, value);
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            object.put("cmd", cmd.name());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            object.put("source", source);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            object.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            object.put("payload", new JSONObject(payload));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
