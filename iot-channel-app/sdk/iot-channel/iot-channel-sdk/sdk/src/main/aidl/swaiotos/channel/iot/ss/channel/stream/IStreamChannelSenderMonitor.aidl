// IStreamChannelSender.aidl
package swaiotos.channel.iot.ss.channel.stream;

// Declare any non-default types here with import statements

interface IStreamChannelSenderMonitor {
    void onAvailableChanged(boolean available);
}
