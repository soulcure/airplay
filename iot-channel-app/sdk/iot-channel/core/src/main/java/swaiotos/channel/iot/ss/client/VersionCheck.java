package swaiotos.channel.iot.ss.client;

import android.util.Log;

import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.server.data.ApkInfo;
import swaiotos.channel.iot.ss.server.data.AppItem;
import swaiotos.channel.iot.ss.server.http.SessionHttpService;
import swaiotos.channel.iot.ss.server.http.api.AppStoreResult;
import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.ss.session.Session;

public class VersionCheck {

    private SSContext ssContext;

    public VersionCheck(SSContext ssContext) {
        this.ssContext = ssContext;
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
        if (Constants.isDangle()) {
            msg.putExtra("registerType","dongle");
        } else {
            msg.putExtra("registerType","tv");
        }

        try {
            Log.e("yao", "sendDialogToTarget--" + msg.toString());
            ssContext.getIMChannel().send(msg);
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


//        builder.setType(IMMessage.TYPE.CONFIRM);
        builder.setType(TYPE);
        builder.setContent(message.getContent());
        IMMessage msg = builder.build();

        try {
            Log.e("soulcure", "sendConfirmToTarget--" + msg.toString());
            ssContext.getIMChannel().send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * check form appStore http api
     *
     * @param packageName   包名
     * @param httpSubscribe callback
     */
    public static void checkAppStore(String packageName, HttpSubscribe<AppStoreResult<AppItem>> httpSubscribe,String sid) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.checkAppStore(packageName), httpSubscribe,"appDetail",sid);
    }


    /**
     * 使用clientID,protoVersion,从版本配置平台获取支持此协议APK的包名和versionCode
     *
     * @param clientId     接收消息客户端ID
     * @param protoVersion 协议版本号
     */
    public static void reqVersionCode(String clientId, int protoVersion,
                                      HttpSubscribe<HttpResult<ApkInfo>> httpSubscribe,String sid) {
        HttpApi.getInstance().request(SessionHttpService.SERVICE.reqVersionCode(clientId, protoVersion), httpSubscribe,"ss-client-config",sid);
    }
}
