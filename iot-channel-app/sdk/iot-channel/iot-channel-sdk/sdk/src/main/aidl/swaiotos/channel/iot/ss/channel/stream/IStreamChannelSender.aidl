// IStreamChannelSender.aidl
package swaiotos.channel.iot.ss.channel.stream;

// Declare any non-default types here with import statements
import swaiotos.channel.iot.ss.channel.stream.IStreamChannelSenderMonitor;

interface IStreamChannelSender {
    void send(in byte[] data);
    boolean available();
    void setSenderMonitor(in IStreamChannelSenderMonitor monitor);
}
