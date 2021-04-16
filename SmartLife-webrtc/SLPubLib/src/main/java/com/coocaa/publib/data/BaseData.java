package com.coocaa.publib.data;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * ClassName: BaseData
 * Author: wuhaiyuan
 * Date: 2017/2/29 20:42
 * Discription:
 */
public class BaseData {

    public int code;
    public String message;


    public static  <T> T load(String str, Class<T> cls) {
        /*GsonBuilder b = new GsonBuilder();
        BooleanSerializer serializer = new BooleanSerializer();
        b.registerTypeAdapter(Boolean.class, serializer);//boolean 类型支持数字。
        b.registerTypeAdapter(boolean.class, serializer);
        Gson gson = b.create();*/
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Boolean.class, booleanAsIntAdapter)
                .registerTypeAdapter(boolean.class, booleanAsIntAdapter)
                .create();
        try {
            return gson.fromJson(str, cls);
        } catch (Exception e) {
            Log.e("ERROR", "BaseResp.load.e: " + e.toString());
            return null;
        }
    }

    public static class BooleanSerializer implements JsonSerializer<Boolean>,
            JsonDeserializer<Boolean> {

        @Override
        public JsonElement serialize(Boolean src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src ? 1 : 0);
        }

        @Override
        public Boolean deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException {
            return json.getAsInt() != 0;
        }
    }

    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }
        @Override
        public Boolean read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            switch (peek) {
                case BOOLEAN:
                    return in.nextBoolean();
                case NULL:
                    in.nextNull();
                    return null;
                case NUMBER:
                    return in.nextInt() != 0;
                case STRING:
                    return Boolean.parseBoolean(in.nextString());
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };
}  