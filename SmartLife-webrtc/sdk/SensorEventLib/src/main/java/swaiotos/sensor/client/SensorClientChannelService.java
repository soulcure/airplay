package swaiotos.sensor.client;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.sensor.data.ChannelEvent;

/**
 * @Author: yuzhan
 */
public class SensorClientChannelService extends SSChannelClient.SSChannelClientService {

    private static final String TAG = "SSCClient";

    public SensorClientChannelService(String name) {
        super(name);
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.i(TAG, "client handleIMMessage");
        Log.i(TAG, "handleIMMessage  type:" + message.getType());
        Log.i(TAG, "handleIMMessage  id: " + message.getId());
        Log.i(TAG, "handleIMMessage  content:" + message.getContent());
        Log.i(TAG, "handleIMMessage  source:" + message.getSource());
        Log.i(TAG, "handleIMMessage  target:" + message.getTarget());
        Log.i(TAG, "handleIMMessage  clientSource:" + message.getClientSource());
        Log.i(TAG, "handleIMMessage  clientTarget:" + message.getClientTarget());
        Log.i(TAG, "handleIMMessage  extra:" + message.encode());

        EventBus.getDefault().post(new ChannelEvent(message.getContent()));
        return false;
    }
}
