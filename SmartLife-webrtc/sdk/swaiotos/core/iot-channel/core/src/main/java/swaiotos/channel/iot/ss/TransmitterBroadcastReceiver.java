package swaiotos.channel.iot.ss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common
 * @ClassName: MonitorStartUpBroadcastReceiver
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/30 11:13
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/30 11:13
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TransmitterBroadcastReceiver  implements TransmitterCallBack{
    private final String TAG = TransmitterBroadcastReceiver.class.getSimpleName();
    private SSContext mSSContext;

    public TransmitterBroadcastReceiver(SSContext ssContext) {
        AndroidLog.androidLog("TransmitterBroadcastReceiver control");
        this.mSSContext = ssContext;
    }

    private void sendCoreData(Context context) {
        try {
            String devicesJson = ShareUtls.getInstance(SSChannelService.getContext()).getString(Constants.COOCAA_PREF_DEVICEs_LIST, "");
            AndroidLog.androidLog("sendCoreData:"+devicesJson);
            Intent intent = new Intent();
            intent.setPackage("com.ccos.tvlauncher");
            intent.setAction("ccos.action.HOME.SERVICE");
            intent.putExtra("doWhat", "deliver_plugin_msg");//固定格式
            Bundle bundle = new Bundle();
            bundle.putString("pkg", "com.skyworth.smarthome_tv");//插件包名，必须，通过包名传给对应插件
            bundle.putString("PUSH_TYPE", "swaiotos.channel.iot");//省略，添加自己需要的其他数据
            bundle.putString(Constants.COOCAA_ACCESSTOKEN,mSSContext.getAccessToken());
            bundle.putString(Constants.COOCAA_PREF_DEVICEs_LIST,devicesJson);
            intent.putExtra("msg", bundle);//key固定msg，bundle把数据携带过去
            context.startService(intent);
            AndroidLog.androidLog("sendCoreData:"+mSSContext.getAccessToken() + " %%%%");
        } catch (Exception e) {
            e.printStackTrace();
            AndroidLog.androidLog("sendCoreData"+e.getLocalizedMessage());
        }
    }

    @Override
    public void sendCoreData() {
        try {
            if (mSSContext != null) {
                sendCoreData(mSSContext.getContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
