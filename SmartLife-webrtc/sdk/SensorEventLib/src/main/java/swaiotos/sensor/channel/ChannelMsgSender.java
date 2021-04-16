package swaiotos.sensor.channel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.sensor.data.AccountInfo;

/**
 * @Author: yuzhan
 */
public class ChannelMsgSender implements IMsgSender{
    protected Context context;
    protected SSChannel ssChannel;
    protected String clientId;

    private String stickyMsg;
    private String stickyTargetId;
    private AccountInfo accountInfo;

    private int protoVersion = -1;
    private boolean showTips;

    public static String TAG = "SSCClient";

    public ChannelMsgSender(Context context, String clientId) {
        this.context = context;
        this.clientId = clientId;
        Log.d(TAG, "ChannelMsgSender init channel clientId=" + clientId + ", pkg=" + iotPkgName());
        open();
    }

    public void open() {
        IOTChannel.mananger.open(context.getApplicationContext(), iotPkgName(), new IOTChannel.OpenCallback() {
            @Override
            public void onConntected(SSChannel channel) {
                Log.d(TAG, "ChannelMsgSender init channel onConnected");
                ssChannel = channel;
                if(!TextUtils.isEmpty(stickyMsg)) {
                    try {
                        sendMsg(stickyMsg, stickyTargetId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String s) {
                Log.d(TAG, "ChannelMsgSender init channel onError : " + s);
                ssChannel = null;
            }
        });
    }

    @Override
    public void setProtoVersion(int protoVersion) {
        this.protoVersion = protoVersion;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public void setShowTips(boolean showTips) {
        this.showTips = showTips;
    }

    public void setTag(String tag) {
        TAG = tag;
    }

    @Override
    public boolean isChannelReady() {
        return ssChannel != null;
    }

    @Override
    public void sendMsg(String content, String targetId) throws Exception {
        try {
            Session sourceSession = ssChannel.getSessionManager().getMySession();
            Session targetSession = ssChannel.getSessionManager().getConnectedSession();

            Log.d(TAG, "sourceSession=" + sourceSession + ", targetSession=" + targetSession);

            IMMessage message = new IMMessage.Builder()
                    .setSource(sourceSession)
                    .setTarget(targetSession)
                    .setClientSource(clientId)
                    .setClientTarget(targetId)
                    .setContent(content)
                    .setType(IMMessage.TYPE.TEXT)
                    .build();
            if(protoVersion >= 0)
                message.setReqProtoVersion(protoVersion);
            if(showTips) {
                message.putExtra("showtips", "true");
                if(accountInfo != null) {
                    JSONObject owner = new JSONObject();
                    owner.put("userID", accountInfo.open_id);
                    owner.put("token", accountInfo.accessToken);
                    owner.put("mobile", accountInfo.mobile);
                    owner.put("nickName", accountInfo.nickName);
                    owner.put("avatar", accountInfo.avatar);
                    message.putExtra("owner", owner.toJSONString());
                }
            }

            Log.d(TAG, "sendMsg : " + message);
            ssChannel.getIMChannel().send(message, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMsgSticky(String content, String targetId) throws Exception {
        if(isChannelReady()) {
            sendMsg(content, targetId);
        } else {
            stickyMsg = content;
            stickyTargetId = targetId;
        }
    }

    protected IMMessageCallback callback = new IMMessageCallback() {

        @Override
        public void onStart(IMMessage message) {

        }

        @Override
        public void onProgress(IMMessage message, int progress) {

        }

        @Override
        public void onEnd(IMMessage message, int code, String info) {
        }
    };

    protected String iotPkgName() {
        return context.getPackageName();
    }
}
