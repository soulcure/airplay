// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

interface IConnectResult {

    void onProgress(String lsid, int code, String info);

    void onFail(String lsid, int code, String info);
}
