package swaiotos.sensor.channel;

import android.os.DeadObjectException;

/**
 * @Author: yuzhan
 */
public interface IMsgSender {
    boolean isChannelReady();
    void sendMsg(String content, String targetId) throws Exception;
    void sendMsgSticky(String content, String targetId) throws Exception;
    void setProtoVersion(int protoVersion);
}
