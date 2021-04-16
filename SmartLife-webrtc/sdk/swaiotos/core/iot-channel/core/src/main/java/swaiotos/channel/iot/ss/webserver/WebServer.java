package swaiotos.channel.iot.ss.webserver;

import java.io.File;
import java.io.IOException;

/**
 * @ClassName: IWebServer
 * @Author: lu
 * @CreateDate: 2020/3/23 10:30 AM
 * @Description:
 */
public interface WebServer {
    String open() throws IOException;

    String getAddress();

    boolean available();

    String uploadFile(File file) throws IOException;

    void close() throws IOException;
}
