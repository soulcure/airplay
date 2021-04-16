package swaiotos.runtime.h5.remotectrl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: StatusData
 * @Author: XuZeXiao
 * @CreateDate: 2020/10/17 13:35
 * @Description:
 */
public class StateData implements Serializable {
    public String type;
    public String uri;
    private final Map<String, Object> extras = new HashMap<>();

    public Map<String, String> toMap() {
        Map<String, String> state = new HashMap<>();
        for (Field field : getClass().getFields()) {
            String name = field.getName();
            String value = null;
            try {
                value = (String) field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value != null) {
                state.put(name, value);
            }
        }
        return state;
    }

    public void putExtra(String key, String value) {
        extras.put(key, value);
    }

    public Object getExtra(String key) {
        return extras.get(key);
    }

    public Map<String, Object> getExtras() {
        return extras;
    }
}
