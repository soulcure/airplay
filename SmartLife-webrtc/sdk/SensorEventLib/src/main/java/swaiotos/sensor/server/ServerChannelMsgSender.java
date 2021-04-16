package swaiotos.sensor.server;

import android.content.Context;
import android.os.DeadObjectException;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.sensor.channel.ChannelMsgSender;
import swaiotos.sensor.channel.IMsgSender;

/**
 * @Author: yuzhan
 */
public class ServerChannelMsgSender extends ChannelMsgSender {

    public ServerChannelMsgSender(Context context, String clientId) {
        super(context, clientId);
        TAG = "SSCServer";
    }

    @Override
    public void sendMsg(String content, String targetId)  throws Exception {
        try {
            Log.d(TAG, "server sendBroadCast targetId : " + targetId + ", content : " + content);
            Session sourceSession = ssChannel.getSessionManager().getMySession();
//            Session targetSession = ssChannel.getSessionManager().getConnectedSession();
            Log.d(TAG, "sourceSession=" + (sourceSession == null ? null : sourceSession.toString()));
            IMMessage message = new IMMessage.Builder()
                    .setBroadcast(true)
                    .setSource(sourceSession)
//                    .setTarget(targetSession)
                    .setClientSource(clientId)
                    .setClientTarget(targetId)
                    .setContent(content)
                    .setType(IMMessage.TYPE.TEXT)
                    .build();
//            message.setReqProtoVersion(0);
            ssChannel.getIMChannel().send(message, callback);
//            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String iotPkgName() {
        return "swaiotos.channel.iot";
    }
}
