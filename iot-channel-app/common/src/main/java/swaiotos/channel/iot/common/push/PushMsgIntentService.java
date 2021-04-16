package swaiotos.channel.iot.common.push;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import swaiotos.channel.iot.common.utils.Constants;


/**
 * @author fc
 */
public class PushMsgIntentService extends IntentService {

    private String msgId, pushId, msg;
    private static String TAG = PushMsgIntentService.class.getSimpleName();

    public PushMsgIntentService() {
        super("PushMsgIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.d(TAG,"---PushMsgIntentService start-");
            if (intent == null) return;
            parseMsg(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从intent中解析消息 @param intent
     * 收到消息后把msgId，pushId,pkg,currentTime 回传给push APK做数据统计使用
     */
    private void parseMsg(Intent intent) {
        msgId = intent.getStringExtra(CCPushConst.MSG_ID_KEY);
        pushId = intent.getStringExtra(CCPushConst.REGID_RESULT_KEY);
        HashMap<String, String> map = new HashMap<>();
        map.put(CCPushConst.MSG_ID_KEY, msgId);
        map.put(CCPushConst.REGID_RESULT_KEY, pushId);
        map.put(CCPushConst.THIRD_PKGNAME, getApplicationContext().getPackageName());
        map.put(CCPushConst.CURRENT_TIME, String.valueOf(System.currentTimeMillis()));
        PushMsgFeedBack.msgFeedBack(getApplicationContext(),CCPushConst.THRID_APP_RECEIVE, map);

        msg = intent.getStringExtra(CCPushConst.MSG_RESULT_KEY);

        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
        Log.d(TAG,"---PushMsgIntentService msg:"+msg);
        Intent msgIntent = new Intent();
        msgIntent.setAction(Constants.COOCAA_PUSH_ACTION);
        msgIntent.putExtra(Constants.COOCAA_PUSH_MSG,msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(msgIntent);

        handleReceiveMsg();
    }

    /**
     * 处理消息后把msgId，pushId,pkg,currentTime，是否处理成功 回传给push APK做数据统计使用
     */
    private void handleReceiveMsg() {
        // TODO: 2020/4/11  第三方应用自己去处理msg消息{}

        HashMap<String, String> map = new HashMap<>();
        map.put(CCPushConst.THRID_APP_HANDLE_RESULT, "true");
        map.put(CCPushConst.MSG_ID_KEY, msgId);
        map.put(CCPushConst.REGID_RESULT_KEY, pushId);
        map.put(CCPushConst.THIRD_PKGNAME, getApplicationContext().getPackageName());
        map.put(CCPushConst.CURRENT_TIME, String.valueOf(System.currentTimeMillis()));
        PushMsgFeedBack.msgFeedBack(getApplicationContext(),CCPushConst.THRID_APP_HANDLE, map);
    }
}
