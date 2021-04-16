package swaiotos.sensor.data;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yuzhan
 */
public class ServerCmdInfo implements Serializable {
    public String cId;
    public String sId;
    public String content;
    public String cmd;

    public Map<String, String> extra;

    public void addExtra(String key, String value) {
        if(extra == null) {
            extra = new HashMap<>();
        }
        extra.put(key ,value);
    }

    public String getExtra(String key) {
        return extra == null ? null : extra.get(key);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public final static String CMD_SERVER_RECEIVE_CONNECT = "server-receive-client-connect";
}
