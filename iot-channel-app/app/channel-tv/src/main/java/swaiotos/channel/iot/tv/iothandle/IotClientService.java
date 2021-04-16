package swaiotos.channel.iot.tv.iothandle;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.statemanager.common.bean.CmdData;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.tv.iothandle.handle.base.MessageHandle;

public class IotClientService extends SSChannelClient.SSChannelClientService {

    private static final String tag = "iotclient";

    public IotClientService() {
        super("MyClientService");
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        Log.d(tag, "handleIMMessage  getId:" + message.getId());
        Log.d(tag, "handleIMMessage  getSource:" + message.getSource());
        Log.d(tag, "handleIMMessage  getTarget:" + message.getTarget());
        Log.d(tag, "handleIMMessage  getClientSource:" + message.getClientSource());
        Log.d(tag, "handleIMMessage  getClientTarget:" + message.getClientTarget());
        Log.d(tag, "handleIMMessage  type:" + message.getType());
        Log.d(tag, "handleIMMessage  content:" + message.getContent());

        CmdData cmdData;
        MessageHandle handle;
        try {
            switch (message.getType()) {
                case IMAGE:
                case AUDIO:
                case VIDEO:
                case DOC:
                    cmdData = JSONObject.parseObject(message.getExtra("response"), CmdData.class);
                    handle = IotChannelHandleFactory.getHandle(CmdData.CMD_TYPE.valueOf(cmdData.type));
                    handle.onHandle(message, channel, cmdData);
                    break;
                case TEXT:
                    cmdData = JSONObject.parseObject(message.getContent(), CmdData.class);
                    handle = IotChannelHandleFactory.getHandle(CmdData.CMD_TYPE.valueOf(cmdData.type));
                    handle.onHandle(message, channel, cmdData);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
