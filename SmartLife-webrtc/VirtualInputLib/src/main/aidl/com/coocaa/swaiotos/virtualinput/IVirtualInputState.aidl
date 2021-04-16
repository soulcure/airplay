// IVirtualInputState.aidl
package com.coocaa.swaiotos.virtualinput;
import com.coocaa.swaiotos.virtualinput.IVirtualInputStateListener;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;

// Declare any non-default types here with import statements

interface IVirtualInputState {
    String getCurState();
    List<SceneConfigBean> getConfigList();
    void addListener(in IVirtualInputStateListener listener);
    void removeListener(in IVirtualInputStateListener listener);
    void startConnectDevice();
    boolean hasHistoryDevice();
    void refreshCurState();
}