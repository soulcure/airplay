// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface IUnBindResult {
    void onSuccess(String lsid);

    void onFail(String lsid, String errorType, String msg);
}
