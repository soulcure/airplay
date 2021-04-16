package swaiotos.channel.iot.ss.channel.stream;

import swaiotos.channel.iot.ss.channel.stream.IStreamChannelReceiver;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannelSender;
interface IStreamChannelService {
   int open(in IStreamChannelReceiver stream);
   void close(int channelId);
   IStreamChannelSender openSender(in Session session ,int channelId);
   void closeSender(IStreamChannelSender sender);
}
