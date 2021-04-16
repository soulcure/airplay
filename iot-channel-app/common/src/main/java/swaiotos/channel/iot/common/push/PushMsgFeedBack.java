package swaiotos.channel.iot.common.push;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import java.util.HashMap;

import swaiotos.channel.iot.common.utils.Constants;

/**
 * Created by fc on 2020/4/9
 * Describe: 第三方应用收到消息和处理完消息，调用此类给apk一个回馈用于链路分析
 *
 * @author fc
 */
public class PushMsgFeedBack {

    public static void msgFeedBack(Context context, String msgType, HashMap<String, String> map) {
        Intent intent = new Intent(CCPushConst.ACTION_PUSH);
        //传递数据
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.COOCAA_PUSH_MAP, map);
        intent.putExtra(CCPushConst.THRID_APP_MSG_TYPE, msgType);
        intent.putExtras(bundle);
        intent.setPackage(CCPushConst.PUSH_APP_PKG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}
