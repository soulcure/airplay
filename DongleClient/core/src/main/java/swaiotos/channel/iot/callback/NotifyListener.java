package swaiotos.channel.iot.callback;

import com.coocaa.sdk.entity.IMMessage;

public interface NotifyListener {
    void OnRec(String targetClient, IMMessage msg);
}
