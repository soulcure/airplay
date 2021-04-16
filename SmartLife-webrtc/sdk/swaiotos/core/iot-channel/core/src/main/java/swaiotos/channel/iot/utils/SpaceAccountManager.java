package swaiotos.channel.iot.utils;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.channel.iot.ss.SSContext;

/**
 * bind_status:number:必须:绑定状态 0:未绑定 1:绑定
 * screen_id:string:非必须:智屏id
 * screen_status:number:非必须:智屏本身状态 1:正常 11:被禁用
 * activation_id:string:非必须:智屏激活id
 * space_id:string:非必须:空间id
 * space_name:string:非必须:空间名称
 * merchant_id:string:非必须:商家id
 * merchant_name:string:非必须:商家名称
 * merchant_location:object:非必须:商家位置:备注: 商家位置
 * latitude:number:非必须:纬度
 * longitude:number:非必须:经度
 * merchant_address:string:非必须:商家地址
 * merchant_tel:string:非必须:商家电话
 * scene_type:string:非必须:场景类型
 */
//private int bind_status;
//private String screen_id;
//private int screen_status;
//private String activation_id;
//private String space_id;
//private String space_name;
//private String merchant_id;
//private String merchant_name;
//private double latitude;
//private double longitude;
//private String merchant_address;
//private String merchant_tel;
//private String scene_type;

public class SpaceAccountManager {
    private BroadcastReceiver broadcastReceiver;
    private SSContext ssContext;

    public SpaceAccountManager(SSContext ssContext) {
        this.ssContext = ssContext;
    }

    public SpaceAccountManager() {
    }

    /**
     * 结果为json字符串，目前字段参考注释
     *
     * @param context
     * @return
     */
    public String getSpaceAccount(Context context) {
        String data = null;
        Cursor cursor = null;
        try {
            Uri uri = Uri.parse("content://com.coocaa.provider.SpaceAccountProvider/data");
            ContentProviderClient client;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                client = context.getContentResolver().acquireUnstableContentProviderClient(uri);
            } else {
                client = context.getContentResolver().acquireContentProviderClient(uri);
            }

            cursor = client.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                data = cursor.getString(cursor.getColumnIndex("data"));
                Log.i("SpaceAccountTest", "getAccountSpace: " + data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        AndroidLog.androidLog("spaceAccount----getSpaceAccount:"+data);
        return data;
    }

    public void register(Context context) {
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO
                    String data = intent.getStringExtra("data");
                    // 结果为json字符串，目前字段参考注释
                    AndroidLog.androidLog("spaceAccount---onReceive: " + data);
                    if (!TextUtils.isEmpty(data)) {
                        ssContext.getController().onSpaceAccount(data);
                    }
                }
            };
        }

        IntentFilter intentFilter = new IntentFilter("space_account_state");
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregister(Context context) {
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }
}
