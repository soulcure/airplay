package com.swaiotos.testdemo_pad.message;

import android.content.Context;

import com.swaiotos.testdemo_pad.FileUtils;

import java.io.File;
import java.util.UUID;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.session.Session;

import static com.swaiotos.testdemo_pad.MainActivity.isCloud;

public class MessageManager implements IMessage {
    private Context context;


    public MessageManager(){
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public IMMessage getTestMessage(SSChannel channel) {
        try {
            Session session = channel.getSessionManager().getMySession();
            Session target = channel.getSessionManager().getConnectedSession();
            IMMessage message = IMMessage.Builder.createTextMessage(session, target,
                    "ss-clientID-testdemo-pad9527", "ss-clientID-testdemo-TV9527", UUID.randomUUID().toString());
//            File file = new File("/storage/emulated/0/tmp","aaa.jpg");
//            IMMessage message = IMMessage.Builder.createImageMessage(session, target,
//                    "ss-clientID-testdemo-pad9527", "ss-clientID-testdemo-TV9527", file);
            if (isCloud)
                message.putExtra(SSChannel.FORCE_SSE, "true");
            return message;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
//        try {
//            IOTChannel.mananger.getSSChannel().getIMChannel().sendSync(msg, 5000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return false;
    }

    @Override
    public Device getCurrentDevice() {
//        try {
//            currentDevice = IOTChannel.mananger.getSSChannel().getDeviceManager().getCurrentDevice();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }

}
