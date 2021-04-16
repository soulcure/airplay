package com.coocaa.smartscreen.utils;

import android.util.Log;
import android.view.MotionEvent;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.service.MainSSClientService;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.DeviceParams;
import com.coocaa.smartscreen.data.channel.PlayParams;
import com.coocaa.smartscreen.data.channel.ReverseScreenParams;
import com.coocaa.smartscreen.data.channel.SkySourceAccountData;
import com.coocaa.smartscreen.data.channel.StartAppParams;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.google.gson.Gson;

import java.util.HashMap;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CAPTURE_APP;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_APP_STORE;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_MOVIE;

/**
 * @ClassName CmdUtil
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 3/18/21
 * @Version TODO (write something)
 */
public class CmdUtil {

    public static final String TAG = CmdUtil.class.getSimpleName();

    public static void sendTextMessage(String cmd, String targetClient) {
        SSConnectManager.getInstance().sendTextMessage(cmd, targetClient);
    }

    /**
     * public static final int KEYCODE_DPAD_UP = 19;
     * public static final int KEYCODE_DPAD_DOWN = 20;
     * public static final int KEYCODE_DPAD_LEFT = 21;
     * public static final int KEYCODE_DPAD_RIGHT = 22;
     * public static final int KEYCODE_DPAD_CENTER = 23;
     * <p>
     * public static final int KEYCODE_HOME = 3;
     * public static final int KEYCODE_BACK = 4;
     * public static final int KEYCODE_MENU = 82; 信号源
     * public static final int KEYCODE_SETTINGS = 176;
     * <p>
     * public static final int KEYCODE_VOLUME_UP = 24;
     * public static final int KEYCODE_VOLUME_DOWN = 25;
     * public static final int KEYCODE_VOLUME_MUTE = 164;
     * <p>
     * public static final int KEYCODE_POWER = 26;
     */
    public static void sendKey(int code) {
        CmdData data = new CmdData(code + "", CmdData.CMD_TYPE.KEY_EVENT.toString(), "");
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    //截图
    public static void sendScreenshot() {
        CmdData data = new CmdData("", CmdData.CMD_TYPE.SCREEN_SHOT.toString(), "");
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    /**
     * 打开系统设置app
     */
    public static void startSettingApp() {
        StartAppParams startAppParams = new StartAppParams();
        startAppParams.packagename = "com.tianci.setting";
        startAppParams.dowhat = StartAppParams.DOWHAT_START_ACTIVITY;
        startAppParams.bywhat = StartAppParams.BYWHAT_ACTION;
        startAppParams.byvalue = "android.settings.SETTINGS";
        String params = startAppParams.toJson();

        CmdData data = new CmdData(StartAppParams.CMD.LIVE_VIDEO.toString(),
                CmdData.CMD_TYPE.START_APP.toString(), params);
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_APPSTATE);
    }

    public static void sendVideoCmd(String cmdString, String third_album_id, String index) {
        Log.d(TAG, "sendVideoCmd: ");
        PlayParams params = new PlayParams();
        params.id = third_album_id;
        params.child_id = index;
        params.simple_detail = "1";
        try {
            SkySourceAccountData skySourceAccountData = new SkySourceAccountData();
            CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
            Log.d(TAG, "sendVideoCmd: CoocaaUserInfo = " + coocaaUserInfo.toString());
            skySourceAccountData.open_id = coocaaUserInfo.open_id;
            TpTokenInfo tpTokenInfo = Repository.get(LoginRepository.class).queryTpTokenInfo();
            skySourceAccountData.tp_token = tpTokenInfo.tp_token;
            params.user_info = new Gson().toJson(skySourceAccountData);
            params.account_source = "1";
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        CmdData data = new CmdData(cmdString, CmdData.CMD_TYPE.MEDIA.toString(), params.toJson());
        String cmd = data.toJson();
        IMMessage message = IMMessage.Builder.createTextMessage(
                SSConnectManager.getInstance().getMy(),
                SSConnectManager.getInstance().getTarget(),
                MainSSClientService.AUTH,
                TARGET_CLIENT_MOVIE,
                cmd);
        message.putExtra("showtips", "true");
        sendMessage(message);
    }

    //同屏
    public static void sendReveseScreenCmd(String cmdString) {
        Log.d(TAG, "sendReveseScreenCmd: ");
        ReverseScreenParams reverseScreenParams = new ReverseScreenParams();
        try {
            Session mySession = SSConnectManager.getInstance().getMy();
            reverseScreenParams.ip = mySession.getExtra(SSChannel.STREAM_LOCAL);
            CmdData data = new CmdData(cmdString, CmdData.CMD_TYPE.REVERSE_SCREEN.toString(), reverseScreenParams.toJson());
            String cmd = data.toJson();
            Log.d(TAG, "sendReveseScreenCmd: " + cmd);
            sendTextMessage(cmd, TARGET_CAPTURE_APP);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    //镜像
    public static void sendMirrorScreenCmd(String cmdString) {
        Log.d(TAG, "sendMirrorCmd: ");
        CmdData data = new CmdData(cmdString, CmdData.CMD_TYPE.MIRROR_SCREEN.toString(), "");
        String cmd = data.toJson();
        Log.d(TAG, "sendMirrorCmd: " + cmd);
        sendTextMessage(cmd, TARGET_CAPTURE_APP);
    }

    //应用
    public static void sendAppCmd(String cmdString, String param) {
        Log.d(TAG, "sendAppCmd: ");
        CmdData data = new CmdData(cmdString, CmdData.CMD_TYPE.APP_STORE.toString(), param);
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    //发送Touch事件
    public static void sendTouchEvent(MotionEvent event) {
        CmdData data = new CmdData("", CmdData.CMD_TYPE.TOUCH_EVENT.toString(), TouchEventUtil.formatTouchEvent(event, 1));
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    /**
     * @param cmdString {@link DeviceParams}
     */
    public static void sendDeviceCmd(String cmdString) {
        Log.d(TAG, "sendDeviceCmd: ");
        DeviceParams params = new DeviceParams();
        CmdData data = new CmdData(cmdString, CmdData.CMD_TYPE.DEVICE.toString(), params.toJson());
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    //一键清理
    public static void onKeyClear() {
        Log.d(TAG, "onKeyClear: ");
        StartAppParams startAppParams = new StartAppParams();
        startAppParams.packagename = "com.coocaa.tvmanager";
        startAppParams.dowhat = StartAppParams.DOWHAT_START_ACTIVITY;
        startAppParams.bywhat = StartAppParams.BYWHAT_ACTION;
        startAppParams.byvalue = "coocaa.intent.action.TVMANAGER_ONEKEY_SPEEDUP";
        String param = startAppParams.toJson();

        CmdData data = new CmdData(StartAppParams.CMD.LIVE_VIDEO.toString(),
                CmdData.CMD_TYPE.START_APP.toString(),
                param);
        String cmd = data.toJson();
        sendTextMessage(cmd, TARGET_CLIENT_APP_STORE);
    }

    public static void sendMessage(IMMessage message) {
        SSConnectManager.getInstance().sendMessage(message);
    }

    //直播
    public static void pushLiveVideo(String category_id, String channel_id) {
        Log.d(TAG, "pushLiveVideo: category_id = " + category_id + "channel_id" + channel_id);
        StartAppParams startAppParams = new StartAppParams();
        startAppParams.packagename = "com.fengmizhibo.live";
        startAppParams.dowhat = StartAppParams.DOWHAT_START_ACTIVITY;
        startAppParams.bywhat = StartAppParams.BYWHAT_ACTION;
        startAppParams.byvalue = "cn.beelive.intent.action.PLAY_LIVE_CHANNEL";
        startAppParams.params = new HashMap<>();
//        startAppParams.params.put("category_id", "62");
//        startAppParams.params.put("channel_id", "1786");
        startAppParams.params.put("category_id", category_id);
        startAppParams.params.put("channel_id", channel_id);
        startAppParams.params.put("from_coocaa_panel", "0");
        String param = startAppParams.toJson();

        CmdData data = new CmdData(StartAppParams.CMD.LIVE_VIDEO.toString(),
                CmdData.CMD_TYPE.START_APP.toString(),
                param);
        String cmd = data.toJson();
        IMMessage message = IMMessage.Builder.createTextMessage(
                SSConnectManager.getInstance().getMy(),
                SSConnectManager.getInstance().getTarget(),
                MainSSClientService.AUTH, TARGET_CLIENT_APP_STORE,
                cmd);
        message.putExtra("showtips", "true");
        sendMessage(message);
    }
}
