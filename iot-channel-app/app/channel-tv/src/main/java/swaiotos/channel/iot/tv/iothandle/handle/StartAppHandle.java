package swaiotos.channel.iot.tv.iothandle.handle;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.iothandle.data.CmdEnum;
import swaiotos.channel.iot.tv.iothandle.data.OnClickData;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;
import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

import static swaiotos.channel.iot.tv.iothandle.data.OnClickData.DOWHAT_SEND_BROADCAST;
import static swaiotos.channel.iot.tv.iothandle.data.OnClickData.DOWHAT_START_ACTIVITY;
import static swaiotos.channel.iot.tv.iothandle.data.OnClickData.DOWHAT_START_SERVICE;

/**
 * 电视一键清理、直播投屏 -- 应用启动
 */
public class StartAppHandle extends BaseChannelHandle {

    @Override
    protected void onHandle() {
        Log.i(TAG, "StartAppHandle onHandle: ");
        if (mCmdData != null && !TextUtils.isEmpty(mCmdData.param)) {
            switch (CmdEnum.START_APP_CMD.valueOf(mCmdData.cmd)) {
                case LIVE_VIDEO:
                    try {
                        ISystem iSystem = SAL.getModule(TVChannelApplication.getContext(), SalModule.SYSTEM);
                        iSystem.setAIScreenMode(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //直播
                    break;
                case ONE_KEY_CLEAR:
                    //一键清理
                    break;
                case PREVIEW_SCREENSAVER:
                    //预览屏保
                    break;
                case CUSTOM_SCREENSAVER:
                    //定制屏保
                    break;
            }
            jumpOperateData(TVChannelApplication.getContext(), JSONObject.parseObject(mCmdData.param, OnClickData.class));
        }
    }

    private void jumpOperateData(Context context, OnClickData data) {
        try {
            if (data == null) return;

            if (data.dowhat != null && !data.dowhat.equals("") && !data.dowhat.equals("null")) {
                Intent eIntent = data.buildIntent(context);
                boolean hasInstalled = isPackageInstalled(context, data.packagename);
                if (hasInstalled) {
                    if (eIntent != null) {
                        try {
                            switch (data.dowhat) {
                                case DOWHAT_START_ACTIVITY:
                                    context.startActivity(eIntent);
                                    break;
                                case DOWHAT_START_SERVICE:
                                    context.startService(eIntent);
                                    break;
                                case DOWHAT_SEND_BROADCAST:
                                    context.sendBroadcast(eIntent);
                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (data.exception != null) {
                                jumpOperateData(context, data.exception);
                            }
                        }
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setAction("coocaa.intent.action.SMART_DETAIL");
                    intent.putExtra("pkg", data.packagename);
                    if (eIntent != null) {
                        intent.putExtra("eIntent", eIntent);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isPackageInstalled(Context context, String pkg) {
        PackageManager mPackageManager = context.getApplicationContext().getPackageManager();
        PackageInfo intent;
        try {
            intent = mPackageManager.getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        if (intent == null)
            return false;
        else
            return true;
    }

}
