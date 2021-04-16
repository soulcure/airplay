package swaiotos.channel.iot.ss.channel.base.sse;

import com.skyworthiot.iotssemsg.IotSSEMsgLib;

import java.io.File;
import java.io.IOException;

import swaiotos.channel.iot.ss.channel.base.BaseChannel;

/**
 * @ClassName: SSEChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:59 PM
 * @Description:
 */
public interface SSEChannel extends BaseChannel {
    interface Receiver {
        void onReceive(String message);
    }

    interface SendMessageCallBack {
        void onSendErro(IotSSEMsgLib.SSESendResultEnum sseSendResultEnum,String message);
    }

    interface UploadCallback {
        void onFileUploaded(String fileKey);
    }

    interface DownloadCallback {
        void onFileDownloaded(String fileKey, File file);
    }

    String open(String lsid) throws IOException;

    void send(String target, String msgId, String tag, String message, SendMessageCallBack callBack) throws IOException;

    void uploadFile(String target, File file, String uid, UploadCallback callback) throws IOException;

    void downloadFile(String fileKey, DownloadCallback callback);

    void addReceiver(String tag, Receiver receiver);

    void removeReceiver(String tag);

    void reOpen(String lsid) throws IOException;
}
