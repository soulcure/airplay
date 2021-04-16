package swaiotos.channel.iot.ss.channel.im;

/**
 * @ClassName: IIMChannel
 * @Author: lu
 * @CreateDate: 2020/4/1 8:34 PM
 * @Description:
 */
public interface IIMChannel extends IIMChannelCore {
    IMMessage sendSync(IMMessage message, IMMessageCallback callback, long timeout) throws Exception;

    IMMessage sendSync(IMMessage message, long timeout) throws Exception;

    void reset(String sid, String token) throws Exception;

    void reset(String sid, String token, String userId) throws Exception;

    void sendBroadCast(IMMessage message, IMMessageCallback callback) throws Exception;

    String fileService(String path) throws Exception;
}
