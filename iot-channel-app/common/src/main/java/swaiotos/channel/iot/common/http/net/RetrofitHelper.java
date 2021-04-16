package swaiotos.channel.iot.common.http.net;

import android.content.Context;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import swaiotos.channel.iot.common.http.exception.NoNetworkException;
import swaiotos.channel.iot.common.utils.Constants;

public class RetrofitHelper {
    public static final String API_BASE_URL = "https://api.github.com";
    private static RetrofitHelper retrofitHelper;
    private static RetrofitHelper retrofitHelperWithLog;

    private Retrofit mRetrofit;

    public static synchronized RetrofitHelper getInstance(boolean log, Context context) {
        RetrofitHelper helper;
        if (log) {
            if (null == retrofitHelperWithLog) {
                retrofitHelperWithLog = new RetrofitHelper(log, context);
            }
            helper = retrofitHelperWithLog;
        } else {
            if (null == retrofitHelper) {
                retrofitHelper = new RetrofitHelper(log, context);
            }
            helper = retrofitHelper;
        }

        return helper;
    }

    private RetrofitHelper(boolean log, Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (log) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(
                    new HttpLoggingInterceptor.Logger() {
                        private static final String TAG = "OkHttp";
                        private final Logger mLogger = Logger.getLogger(TAG);
//                        private final org.slf4j.Logger mLog = LoggerFactory.getLogger(TAG);

                        @Override
                        public void log(String message) {
                            //                    Log.d(TAG, message);
                            mLogger.log(java.util.logging.Level.INFO, message);
//                            mLog.debug(message);
                        }
                    });
            httpLoggingInterceptor.setLevel(Level.BODY);
            // add interceptor
            builder.addInterceptor(httpLoggingInterceptor);
        }

        if (context != null) {
            // use application context
            final Context appContext = context.getApplicationContext();
            // add interceptor
            builder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {

                    // no network
                    if (!NetworkUtils.isNetworkAvailable(appContext)) {
                        // throw no network exception
                        throw new NoNetworkException();
                    }

                    try {
                        return chain.proceed(chain.request());
                    } catch (SocketTimeoutException | UnknownHostException e) {

                        // no network
                        if (!NetworkUtils.isNetworkAvailable(appContext)) {
                            // throw no network exception
                            throw new NoNetworkException();
                        }

                        // re-throw original exception
                        throw e;
                    }
                }
            });
        }

        // build OkHttpClient
        OkHttpClient httpClient = customHttpClient(builder)
                // set timeout...
                .connectTimeout(Constants.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(Constants.SOCKET_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(Constants.CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .build();

        Builder gsonBuild = getBaseRetrofitBuilder(httpClient);

        mRetrofit = gsonBuild.build();
    }

    private Builder getBaseRetrofitBuilder(OkHttpClient httpClient) {
        return new Builder()
                .client(httpClient)
                .baseUrl(API_BASE_URL);
    }

    /**
     * custom the httpclient
     *
     * @param builder
     *         httpclient builder
     */
    protected OkHttpClient.Builder customHttpClient(OkHttpClient.Builder builder) {
        return builder;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public void setRetrofit(Retrofit pRetrofit) {
        mRetrofit = pRetrofit;
    }
}
