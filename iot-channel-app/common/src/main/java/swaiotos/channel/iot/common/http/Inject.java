package swaiotos.channel.iot.common.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import swaiotos.channel.iot.common.http.net.CooCaaRestApi;
import swaiotos.channel.iot.common.http.net.CooCaaRestApiFactory;
import swaiotos.channel.iot.common.utils.Singleton;

import static swaiotos.channel.iot.common.utils.Preconditions.checkNotNull;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.net
 * @ClassName: Inject
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 0:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 0:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class Inject {
    private static Singleton<CooCaaRestApi> singleton;

    public static CooCaaRestApi getGoLiveRestApi(final Context context) {
        checkNotNull(context);
        if(singleton == null) {
            synchronized (Inject.class) {
                if(singleton == null) {
                    singleton = new Singleton<CooCaaRestApi>() {
                        @Override
                        protected CooCaaRestApi create() {
                            Log.d(CooCaaRestApi.class.getName(),"-----getGoLiveRestApi---");
                            PackageManager pm = context.getPackageManager();
                            PackageInfo pi;
                            int versionCode = 0;
                            String versionName = "";
                            try {
                                pi = pm.getPackageInfo(context.getPackageName(), 0);
                                versionCode = pi.versionCode;
                                versionName = pi.versionName;
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            CooCaaRestApiFactory goLiveRestApiFactory =
                                    new CooCaaRestApiFactory(context, versionCode, versionName);

                            return goLiveRestApiFactory.createGoLiveRestApi();
                        }
                    };
                }
            }
        }
        return singleton.get();
    }

}
