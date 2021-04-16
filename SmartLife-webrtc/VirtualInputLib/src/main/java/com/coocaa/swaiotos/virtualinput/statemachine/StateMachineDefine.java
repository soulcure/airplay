package com.coocaa.swaiotos.virtualinput.statemachine;

import android.text.TextUtils;

/**
 * @Author: yuzhan
 */
public class StateMachineDefine {

    private final static String SCHEMA_APP = "app://";
    private final static String SCHEMA_H5 = "h5://";

    public static String fromApp(String key) {
        return addPrefix(key, SCHEMA_APP);
    }

    public static String fromH5(String key) {
        return addPrefix(key, SCHEMA_H5);
    }

    private static String addPrefix(String key, String prefix) {
        if(!TextUtils.isEmpty(key) && !key.startsWith(prefix)) {
            return prefix + key;
        } else {
            return key;
        }
    }
}
