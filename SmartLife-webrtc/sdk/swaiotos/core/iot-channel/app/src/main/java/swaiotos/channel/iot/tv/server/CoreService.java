package swaiotos.channel.iot.tv.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDInfo;
import swaiotos.channel.iot.ss.manager.lsid.LSIDManager;

public class CoreService extends SSChannelService {

    private static final String TAG = "core";

    private static class LSIDManagerImpl implements LSIDManager {
        private LSIDInfo mLSIDInfo;

        public LSIDManagerImpl(Context context) {
            SharedPreferences sp = context.getSharedPreferences("screenId", Context.MODE_PRIVATE);
            String sid = sp.getString("sid", "");
            if (TextUtils.isEmpty(sid)) {
                sid = "SID" + (int) (Math.random() * 10000000);
                sp.edit().putString("sid", sid).apply();
            }
            mLSIDInfo = new LSIDInfo(sid, "token-" + sid);
        }

        @Override
        public void addCallback(Callback callback) {

        }

        @Override
        public void removeCallback(Callback callback) {

        }

        @Override
        public LSIDInfo refreshLSIDInfo() {
            return mLSIDInfo;
        }

        @Override
        public LSIDInfo getLSIDInfo() {
            return mLSIDInfo;
        }

        @Override
        public LSIDInfo reset() {
            return null;
        }

        @Override
        public void setSid(String sid, String token) {

        }
    }

    private static final SSChannelServiceManager manager =
            new SSChannelServiceManager<TVDeviceInfo>() {
                private LSIDManager mLSIDManager;

                @Override
                public void performCreate(Context context) {
                    Log.d(TAG, "SSChannelServiceManager performCreate---");
                    mLSIDManager = new LSIDManagerImpl(context);
                }

                @Override
                public LSIDManager getLSIDManager() {
                    Log.d(TAG, "SSChannelServiceManager getLSIDManager---");
                    return mLSIDManager;
                }

                @Override
                public TVDeviceInfo getDeviceInfo(Context context) {
                    TVDeviceInfo tvDeviceInfo = new TVDeviceInfo();
                    tvDeviceInfo.activeId = "sdfdsfsdfdsf";
                    tvDeviceInfo.deviceName = "device1211";
                    tvDeviceInfo.mModel = "E8";
                    return tvDeviceInfo;
                }

                @Override
                public boolean performClientVerify(Context context, ComponentName cn, String id, String key) {
                    return true;
                }

                @Override
                public void onSSChannelServiceStarted(Context context) {

                }

                @Override
                public Intent getClientServiceIntent(Context context) {
                    return new Intent(SSChannelClient.DEFAULT_ACTION);
                }
            };

    @Override
    protected SSChannelServiceManager getManager() {
        return manager;
    }
}
