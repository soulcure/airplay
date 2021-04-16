package swaiotos.channel.iot.common.http.net;
import android.content.Context;


public class RestApiFactory {

    private final Context mContext;

    public RestApiFactory(Context context) {
        mContext = context != null ? context.getApplicationContext() : null;
    }

    /**
     * Create the RestApi.
     *
     * @param log If set <code>true<code/>, printing log while retrieving data from the network;
     *            Otherwise, no log.
     */
    public RestApi create(boolean log) {
        RetrofitHelper instance = RetrofitHelper.getInstance(log, mContext);
        return instance.getRetrofit().create(RestApi.class);
    }
}
