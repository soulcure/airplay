package com.coocaa.smartscreen.repository.http;


        import android.util.Log;

        import java.lang.annotation.Annotation;
        import java.lang.reflect.Type;

        import retrofit2.Call;
        import retrofit2.CallAdapter;
        import retrofit2.Retrofit;

public class CustomCallAdapterFactory extends CallAdapter.Factory {
    private static CustomCallAdapterFactory mInstance = null;

    public synchronized static CallAdapter.Factory create() {
        if (mInstance == null) {
            mInstance = new CustomCallAdapterFactory();
        }
        return mInstance;
    }

    @Override
    public CallAdapter<?,?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        return new CustomCallAdapter(returnType);
    }

    public static class CustomCallAdapter implements CallAdapter {
        private Type returnType = null;

        public CustomCallAdapter(Type returnType) {
            this.returnType = returnType;
        }

        @Override
        public Type responseType() {
            return returnType;
        }

        @Override
        public Object adapt(Call call) {
            try {
//                Log.d("TvpiHttp", "adapt 11 = " + call.execute().toString());
//                Log.d("TvpiHttp", "adapt 11 = " + call.execute().raw().toString());
//                Log.d("TvpiHttp", "adapt 11 = " + call.execute().code());
//                Log.d("TvpiHttp", "adapt 22 = " + call.execute().message());
//                Log.d("TvpiHttp", "adapt 33 = " + call.execute().raw().code());
                return call.execute().body();
            } catch (Exception e) {
                throw new HttpExecption(e);
            }
        }
    }


}
