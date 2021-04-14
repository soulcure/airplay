package swaiotos.channel.iot.im;

import com.coocaa.sdk.entity.Session;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class DeviceState {
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


    public static DeviceState parse(String in) {
        try {
            JSONObject object = new JSONObject(in);
            String lsid = object.getString("lsid");
            Map<String, String> connectiveInfo = parse(in, "connective");
            Map<String, String> clientInfo = parse(in, "client");
            return new DeviceState(lsid, connectiveInfo, clientInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private DeviceState(String lsid, Map<String, String> connectiveInfo, Map<String, String> clientInfo) {
        mLsid = lsid;
        mConnectiveInfo = new HashMap<>(connectiveInfo);
        mClientInfo = new HashMap<>(clientInfo);
    }


    private static Map<String, String> parse(String in, String key) throws JSONException {
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
