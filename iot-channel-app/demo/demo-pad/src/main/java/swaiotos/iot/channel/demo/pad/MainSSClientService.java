package swaiotos.iot.channel.demo.pad;

import android.util.Log;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class MainSSClientService extends SSChannelClient.SSChannelClientService {
    public static final String AUTH = "ss-clientID-mobile";

    public MainSSClientService() {
        super("MainSSClientService");
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.d("MSS", "handleIMMessage " + message);
        return true;
    }
}
