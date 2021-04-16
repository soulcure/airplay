package swaiotos.sensor.server;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.greenrobot.eventbus.EventBus;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.sensor.data.ClientCmdInfo;

public abstract class SensorServerChannelService extends SSChannelClient.SSChannelClientService {
    private static final String TAG = "SSCServer";

    public SensorServerChannelService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.i(TAG, "server handleIMMessage  type:" + message.getType());
        Log.i(TAG, "server handleIMMessage  id: " + message.getId());
        Log.i(TAG, "server handleIMMessage  content:" + message.getContent());
        Log.i(TAG, "server handleIMMessage  source:" + message.getSource());
        Log.i(TAG, "server handleIMMessage  target:" + message.getTarget());
        Log.i(TAG, "server handleIMMessage  clientSource:" + message.getClientSource());
        Log.i(TAG, "server handleIMMessage  clientTarget:" + message.getClientTarget());
        Log.i(TAG, "server handleIMMessage  extra:" + message.encode());

        ClientCmdInfo info = JSON.parseObject(message.getContent(), ClientCmdInfo.class);
        if(ClientCmdInfo.CMD_CLIENT_START.equals(info.cmd)) {
            MessageEventData eventData = new MessageEventData();
            eventData.message = message;
            EventBus.getDefault().post(eventData);
        } else if(ClientCmdInfo.CMD_CLIENT_STOP.equals(info.cmd)) {
            MessageEventData eventData = new MessageEventData();
            eventData.message = message;
            EventBus.getDefault().post(eventData);
        }

        return true;
    }

}
