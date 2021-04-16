// IIMChannelService.aidl
package swaiotos.channel.iot.ss.channel.im;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.utils.ipc.ParcelableObject;

interface IIMChannelService {
    void send(in IMMessage message,in Messenger callback);
    ParcelableObject sendSync(in IMMessage message,in Messenger callback,long timeout);

    void reset(in String sid, in String token);

    void sendBroadCast(in IMMessage message,in Messenger callback);

    String fileService(in String path);

    void resetSidAndUserId(in String sid, in String token, in String userId);

}
