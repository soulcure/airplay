// IMainService.aidl
package swaiotos.channel.iot.ss;

// Declare any non-default types here with import statements

import swaiotos.channel.iot.utils.ipc.ParcelableBinder;

interface IMainService {
    ParcelableBinder open(String packageName);
}
