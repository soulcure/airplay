package swaiotos.channel.iot.ss.channel.stream;

interface IStreamChannelReceiver{
 void onReceive(in byte[] data);
}