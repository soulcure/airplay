package com.coocaa.swaiotos.virtualinput.iot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: AbstractState
 * @Author: lu
 * @CreateDate: 2020/10/15 9:06 PM
 * @Description:
 */
public class State {
    public static final String KEY_TYPE = "type";
    public static final String KEY_VERSION = "version";
    public static final String KEY_VALUES = "values";

    public static State decode(String json) {
        try {
            JSONObject object = new JSONObject(json);
            String type = object.getString(State.KEY_TYPE);
            int version = Integer.valueOf(object.getString(State.KEY_VERSION));
            String jvalues = object.getString(State.KEY_VALUES);
            JSONObject _jvalues = new JSONObject(jvalues);
            Iterator<String> keys = _jvalues.keys();
            Map<String, String> values = new LinkedHashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                values.put(key, _jvalues.getString(key));
            }
            return new State(type, version, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String type;
    private int version;
    private Map<String, String> values = new LinkedHashMap<>();

   State(State state) {
        this.type = state.type;
        this.version = state.version;
        this.values.putAll(state.values);
    }

    State(String type, int version) {
        this.type = type;
        this.version = version;
    }

    State(String type, int version, Map<String, String> values) {
        this.type = type;
        this.version = version;
        this.values.putAll(values);
    }

    public String getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public synchronized void put(String key, String value) {
        values.put(key, value);
    }

    public synchronized String get(String key) {
        return values.get(key);
    }

    public synchronized void clear() {
        values.clear();
    }

    public synchronized String encode() {
        try {
            JSONObject _values = new JSONObject(values);
            JSONObject object = new JSONObject();
            object.put(KEY_TYPE, type);
            object.put(KEY_VERSION, String.valueOf(version));
            object.put(KEY_VALUES, _values.toString());
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
//
//
//    public static void main(String[] args) {
//        State state = new State("type", 1);
//        state.put("key-1", "value-1");
//        state.put("key-2", "value-2");
//        state.put("key-3", "value-3");
//        System.out.println(state.encode());
//    }
}
