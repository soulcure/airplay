package swaiotos.channel.iot.tv.server;

import android.text.TextUtils;
import android.util.Log;


import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.tv.DemoApplication;

public class MainSSClientService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = MainSSClientService.class.getSimpleName();


    private DemoApplication app;

    public MainSSClientService() {
        super("MainSSClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        app = (DemoApplication) getApplication();
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.d(TAG, "handleIMMessage " + message);
        if (null == message) {
            Log.d(TAG, "message is null !!!");
            return false;
        }

        String content = message.getExtra("content");  //解析消息json
        if (!TextUtils.isEmpty(content)) {
            Log.d(TAG, "handleIMMessage content:" + content);
//            app.getSsChannel().onReceive(content);
            //处理逻辑
        }
        return true;
    }

}
