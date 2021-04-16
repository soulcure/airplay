package com.coocaa.smartmall.data.mobile.data;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

public class BaseRequest {
    public HashMap<String,Object> toMap(){
        HashMap<String,Object> map=new HashMap<>();
        for(Field field:getClass().getDeclaredFields()){
            try {
                Log.i("luoxi",field.getName());
                field.setAccessible(true);
                Object value=field.get(this);
                if(value!=null){
                    map.put(field.getName(),field.get(this));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
