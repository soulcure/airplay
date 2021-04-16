package swaiotos.channel.iot.tv.iothandle.handle;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.iothandle.handle.base.BaseChannelHandle;
import swaiotos.channel.iot.utils.EmptyUtils;


public class StateHandle extends BaseChannelHandle {

    private static final String USER_DISCONNECT = "disconnect";
    private static final String GET_BUSINESS_STATE = "getBusinessState";//获取当前业务状态
    private static final String SHOW_BIG_QRCODE_WINDOW = "showBigQRCodeWindow";//展示大的共享二维码
    private static final String CMD_TO_DONGLE = "cmdToDongle";//向dongle发指令
    @Override
    protected void onHandle() {
        String cmd = mCmdData.cmd;
        Log.d(TAG, "StateHandle  cmd:" + cmd);
        Intent intent = null;
        switch (cmd){
            case GET_BUSINESS_STATE:
                intent = new Intent();
                intent.setAction("coocaa.intent.action.BusinessStateService");
                intent.setPackage("swaiotos.channel.iot");
                break;
            case SHOW_BIG_QRCODE_WINDOW:
                intent = new Intent();
                intent.setAction("coocaa.intent.action.BigQRCodeService");
                intent.setPackage("swaiotos.channel.iot");
                break;
            case USER_DISCONNECT:
                intent = new Intent();
                intent.setAction("coocaa.intent.action.DisconnectService");
                intent.setPackage("swaiotos.channel.iot");
                intent.putExtra("mMessage",mIMMessage);
                break;
            case CMD_TO_DONGLE:
                intent = new Intent();
                intent.setAction("coocaa.intent.action.DongleCMDService");
                intent.setPackage("swaiotos.channel.iot");
                intent.putExtra("mMessage",mIMMessage);
                break;
        }
        if(EmptyUtils.isNotEmpty(intent)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TVChannelApplication.getContext().startForegroundService(intent);
            } else {
                TVChannelApplication.getContext().startService(intent);
            }
        }
    }
}
