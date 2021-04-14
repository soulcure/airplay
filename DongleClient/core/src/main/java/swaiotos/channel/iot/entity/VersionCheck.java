package swaiotos.channel.iot.entity;

import android.util.Log;

import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import swaiotos.channel.iot.SdkManager;
import swaiotos.channel.iot.utils.Constants;


public class VersionCheck {


    public VersionCheck() {
    }

    public void sendDialogToTarget(IMMessage message, String clientId) {
        IMMessage.Builder builder = new IMMessage.Builder();

        Session source = message.getTarget();
        Session target = message.getSource();
        builder.setTarget(target);
        builder.setSource(source);

        String sourceClient = message.getClientTarget(); //发送方设置为接收方
        String targetClient = message.getClientSource(); //接收方设置为发送方
        builder.setClientSource(sourceClient);
        builder.setClientTarget(targetClient);


        builder.setType(IMMessage.TYPE.DIALOG);
        builder.setContent(clientId);
        IMMessage msg = builder.build();
        if (Constants.isDongle()) {
            msg.putExtra("registerType", "dongle");
        } else {
            msg.putExtra("registerType", "tv");
        }

        try {
            Log.e("yao", "sendDialogToTarget--" + msg.toString());
            SdkManager.instance().sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendConfirmToTarget(IMMessage message, IMMessage.TYPE TYPE) {
        IMMessage.Builder builder = new IMMessage.Builder();

        Session source = message.getTarget();  //发送方设置为接收方
        Session target = message.getSource();  //接收方设置为发送方
        builder.setTarget(target);
        builder.setSource(source);

        String sourceClient = message.getClientTarget(); //发送方设置为接收方
        String targetClient = message.getClientSource(); //接收方设置为发送方
        builder.setClientSource(sourceClient);
        builder.setClientTarget(targetClient);

        builder.setType(TYPE);
        builder.setContent(message.getContent());
        IMMessage msg = builder.build();

        try {
            Log.e("yao", "sendConfirmToTarget--" + msg.toString());
            SdkManager.instance().sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
