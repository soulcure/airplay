package swaiotos.channel.iot.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: JSON
 * @Author: lu
 * @CreateDate: 2020/4/23 11:41 AM
 * @Description:
 */
public class JSON {
    public static Map<String, String> parse(String in, String key) throws JSONException {
        JSONObject object = new JSONObject(in);
        JSONObject m = object.getJSONObject(key);
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = m.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            map.put(k, m.getString(k));
        }
        return map;
    }
}
