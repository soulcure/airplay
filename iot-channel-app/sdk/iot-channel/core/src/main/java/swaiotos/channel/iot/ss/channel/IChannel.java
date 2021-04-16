package swaiotos.channel.iot.ss.channel;

import java.io.IOException;

/**
 * @ClassName: IChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:42 PM
 * @Description:
 */
public interface IChannel {

    String open() throws IOException;

    String getAddress();

    boolean available();

    String type();

    void close() throws IOException;
}
