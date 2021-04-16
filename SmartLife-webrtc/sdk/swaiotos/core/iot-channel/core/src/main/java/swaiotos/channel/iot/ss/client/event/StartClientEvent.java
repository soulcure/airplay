package swaiotos.channel.iot.ss.client.event;

import java.io.Serializable;

/**
 * @author eric
 */
public class StartClientEvent implements Serializable {
    public String clientID;
    public String pkgName;
    public String className;
    public String message;

    public StartClientEvent(String id,String pkg,String cName,String msg){
        clientID = id;
        pkgName = pkg;
        className = cName;
        message = msg;
    }

}
