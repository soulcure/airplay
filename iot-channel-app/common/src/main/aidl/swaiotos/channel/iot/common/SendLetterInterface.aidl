// SendLetterInterface.aidl
package swaiotos.channel.iot.common;

import swaiotos.channel.iot.common.BindCodeCallback;
import swaiotos.channel.iot.common.TypeInfoCallback;

// Declare any non-default types here with import statements

interface SendLetterInterface {

    void registerCallback(in BindCodeCallback bindCodeListener);

    void unregisterCallback(in BindCodeCallback bindCodeListener);

    void loadBindCodeStart();

    void loadBindCodeStop();

    //type:1 临时连接
    void registerTypeCallback(in TypeInfoCallback bindCodeListener,int type);

    //type:1 临时连接
    void unregisterTypeCallback(in TypeInfoCallback bindCodeListener,int type);

    //返回信息：type=1 为临时连接
    void loadInfo(int type);
}
