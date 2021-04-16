package com.coocaa.tvpi.module.log;

import android.content.Context;

import java.util.Map;

/**
 * @Author: yuzhan
 */
class CcLogSubmit extends BaseLogSubmit{

    public CcLogSubmit(Context context) {
        super(context);
    }

    @Override
    public void event(String eventId, Map<String, String> params) {
        try {
            PayloadEvent.submit("smartscreen.event", eventId, params);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
