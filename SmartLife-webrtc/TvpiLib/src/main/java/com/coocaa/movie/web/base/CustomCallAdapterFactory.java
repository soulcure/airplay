package com.coocaa.movie.web.base;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * Created by luwei on 17-7-17.
 */

public class CustomCallAdapterFactory extends CallAdapter.Factory{
    private static CustomCallAdapterFactory mInstance = null;
    public synchronized static CallAdapter.Factory create(){
        if (mInstance == null){
            mInstance = new CustomCallAdapterFactory();
        }
        return mInstance;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new CustomCallAdapter(returnType);
    }

    public static class CustomCallAdapter implements CallAdapter{
        private Type returnType = null;
        public CustomCallAdapter(Type returnType){
            this.returnType = returnType;
        }
        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public Object adapt(Call call) {
            try {
                return call.execute().body();
            } catch (Exception e) {
                throw new HttpExecption(e);
            }
        }
    }
}
