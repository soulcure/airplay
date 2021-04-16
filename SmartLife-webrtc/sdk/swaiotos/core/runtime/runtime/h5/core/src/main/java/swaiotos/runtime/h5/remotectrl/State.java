package swaiotos.runtime.h5.remotectrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: AbstractState
 * @Author: lu
 * @CreateDate: 2020/10/15 9:06 PM
 * @Description:
 */
public class State implements Serializable {
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


    public String type;
    public int version;
    public final Map<String, String> values = new LinkedHashMap<>();

    public State() {
    }

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

    public final String getType() {
        return type;
    }

    public final int getVersion() {
        return version;
    }

    public final Map<String, String> getValues() {
        return values;
    }

    public final synchronized void put(String key, String value) {
        values.put(key, value);
    }

    public final synchronized String get(String key) {
        return values.get(key);
    }

    public final synchronized void clear() {
        values.clear();
    }

    public final synchronized String encode() {
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
}
