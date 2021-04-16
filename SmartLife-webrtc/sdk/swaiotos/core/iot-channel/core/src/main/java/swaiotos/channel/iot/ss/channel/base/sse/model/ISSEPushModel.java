package swaiotos.channel.iot.ss.channel.base.sse.model;

import java.io.File;
import java.io.IOException;

import swaiotos.channel.iot.ss.channel.base.sse.SSEChannel;

/**
 * @ClassName: ISSEPushModel
 * @Author: AwenZeng
 * @CreateDate: 2020/3/9 17:47
 * @Description:
 */
public interface ISSEPushModel {
    interface SSEReceiver {
        void onReceive(String tag, String message);
    }

    boolean isSSEConnected();

    boolean isSSEStarted();

    boolean connectSSE(String deviceId);

    void uploadFile(String target, File file,String uid,SSEChannel.UploadCallback callback) throws IOException;

    void downloadFile(String fileKey, SSEChannel.DownloadCallback callback);

    void setReceiveListener(SSEReceiver receiver);

    void sendSSEMessage(String toDeviceId, String msgId, String msgName, String message,SSEChannel.SendMessageCallBack callBack) throws IOException;

}
