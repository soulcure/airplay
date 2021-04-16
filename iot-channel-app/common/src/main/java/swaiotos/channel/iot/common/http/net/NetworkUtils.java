package swaiotos.channel.iot.common.http.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class NetworkUtils {

    /**
     * Get ConnectivityManager
     */
    public static ConnectivityManager getConnectivityManager(Context context) {
        // Marshmallow bug!
        // ConnectivityManager is singleton in Marshmallow, so it will reference the context!
        Context applicationContext = context.getApplicationContext();
        return (ConnectivityManager) applicationContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Check whether the network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = getConnectivityManager(context);
        if (null == connManager) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && (activeNetworkInfo.isAvailable()
                || activeNetworkInfo.isConnected());
    }

}
