// IBindResult.aidl
package swaiotos.channel.iot.ss.device;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.ss.device.Device;

interface IBindResult {
    void onSuccess(String bindCode,in Device device);

    void onFail(String bindCode, String errorType, String msg);
}
