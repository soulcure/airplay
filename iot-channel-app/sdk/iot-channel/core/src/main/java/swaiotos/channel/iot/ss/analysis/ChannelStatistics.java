package swaiotos.channel.iot.ss.analysis;

import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import swaiotos.channel.iot.ss.SSContext;
import swaiotos.channel.iot.ss.analysis.data.LocalSseMsg;
import swaiotos.channel.iot.ss.analysis.data.SSeMsgError;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ss.analysis
 * @ClassName: ChannelStatistics
 * @Description: 统计100次
 * @Author: wangyuehui
 * @CreateDate: 2020/12/23 14:21
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/23 14:21
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ChannelStatistics {
    //消息生产者：key:事件ID value:当前事件
    private final ConcurrentHashMap<String, Long> mProducer = new ConcurrentHashMap<>();
    //消息消费者：key:事件ID value:消息发送成功时间差
//    private final ConcurrentHashMap<String,Long> mConsumer = new ConcurrentHashMap<>();
    //消息时间超过2s的个数
//    private final LinkedBlockingQueue<String> mFailureR = new LinkedBlockingQueue<>();

    private final Object lock = new Object();

    private SSContext ssContext;
    private CHANNEL type;
    private int countByType;

    public enum CHANNEL {
        LOCAL,
        SSE
    }

    public ChannelStatistics(SSContext ssContext, CHANNEL type) {
        this.type = type;
        this.ssContext = ssContext;
        switch (type) {
            case LOCAL:
            case SSE:
                countByType = 1;
                break;
        }
    }

    public void sendMessage(IMMessage imMessage) {
        try {
            synchronized (lock) {
                if (mProducer.size() > 100) {
                    mProducer.clear();
                }
                mProducer.put(imMessage.getId(), System.currentTimeMillis());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiverMessage(IMMessage imMessage, int timeout) {
        synchronized (lock) {

            long currentTime = System.currentTimeMillis();
            String msgId = imMessage.getId();
            if (!TextUtils.isEmpty(msgId) && mProducer.containsKey(msgId)) {
                long startTime = mProducer.get(msgId);

                LocalSseMsg localSseMsg = new LocalSseMsg();
                localSseMsg.setSourceLsid(ssContext.getLSID());
                localSseMsg.setTime(currentTime - startTime);
                localSseMsg.setLost(0);
                localSseMsg.setDeviceType(UserBehaviorAnalysis.deviceType);
                localSseMsg.setWifiSSID(UserBehaviorAnalysis.wifiSSID);
                localSseMsg.setCount(1);
                switch (type) {
                    case LOCAL:
                        UserBehaviorAnalysis.appEventBehavior(LocalSseMsg.EVENT_NAME_LOCAL, localSseMsg);
                        break;
                    case SSE:
                        UserBehaviorAnalysis.appEventBehavior(LocalSseMsg.EVENT_NAME_SSE, localSseMsg);
                        break;
                }
                mProducer.remove(msgId);
            }

            if (mProducer.size() > 0) {
                for (Map.Entry<String, Long> entry : mProducer.entrySet()) {
                    String entryMsgId = entry.getKey();
                    long timeDiff = currentTime - entry.getValue();
                    if (timeDiff > timeout) {
                        mProducer.remove(entryMsgId);

                        switch (type) {
                            case LOCAL:
                                UserBehaviorAnalysis.reportLocalConnectError(ssContext.getLSID(), imMessage.getTarget().getId());
                                break;
                            case SSE:

                                UserBehaviorAnalysis.reportSSeMsgError(ssContext.getLSID(), imMessage.getTarget().getId(),
                                        imMessage.getId(), imMessage.getType().toString(), "send time out " + timeout, SSeMsgError.SENDMSG,imMessage.encode());
                                break;
                        }
                    }
                }
            }
        }

    }

//    public void sendMessage(IMMessage imMessage) {
//        try {
//            synchronized (lock) {
//                if (mProducer.size() > countByType || mConsumer.size() > countByType || mFailureR.size() > countByType) {
//                    mProducer.clear();
//                    mConsumer.clear();
//                    mFailureR.clear();
//                }
//                mProducer.put(imMessage.getId(),System.currentTimeMillis());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void receiverMessage(IMMessage imMessage) {
//        synchronized (lock) {
//            //本地通道消息事件耗时
//            if (mConsumer.size() + mFailureR.size() >= countByType) {
//                Iterator<Map.Entry<String,Long>> iterator = mConsumer.entrySet().iterator();
//                long totalTime = 0L;
//                while (iterator.hasNext()) {
//                    Map.Entry<String,Long> entry = iterator.next();
//                    totalTime += entry.getValue();
//                }
//
//                LocalSseMsg localSseMsg = new LocalSseMsg();
//                localSseMsg.setSourceLsid(ssContext.getLSID());
//                localSseMsg.setTime((float) totalTime/mConsumer.size());
//                localSseMsg.setLost((float)mFailureR.size()/(mConsumer.size() + mFailureR.size()));
//                localSseMsg.setDeviceType(UserBehaviorAnalysis.deviceType);
//                localSseMsg.setWifiSSID(UserBehaviorAnalysis.wifiSSID);
//                localSseMsg.setCount(countByType);
//                switch (type) {
//                    case LOCAL:
//                        UserBehaviorAnalysis.appEventBehavior(LocalSseMsg.EVENT_NAME_LOCAL,localSseMsg);
//                        break;
//                    case SSE:
//                        UserBehaviorAnalysis.appEventBehavior(LocalSseMsg.EVENT_NAME_SSE,localSseMsg);
//                        break;
//                }
//
//                mConsumer.clear();
//                mFailureR.clear();
//                mProducer.clear();
//            }
//
//            long currentTime = System.currentTimeMillis();
//
//            String msgId = imMessage.getId();
//
//            if (!TextUtils.isEmpty(msgId) && mProducer.containsKey(msgId)) {
//                long startTime = mProducer.get(msgId);
//                mConsumer.put(msgId,currentTime-startTime);
//                mProducer.remove(msgId);
//            }
//
//            if (mProducer.size() > 0) {
//                for (Map.Entry<String, Long> entry : mProducer.entrySet()) {
//                    String entryMsgId = entry.getKey();
//                    long timeDiff = currentTime - entry.getValue();
//                    if (timeDiff > 5000) {
//                        mFailureR.offer(entryMsgId);
//                        mProducer.remove(entryMsgId);
//
//                        switch (type) {
//                            case LOCAL:
//                                UserBehaviorAnalysis.reportLocalConnectError(ssContext.getLSID(),imMessage.getTarget().getId());
//                                break;
//                            case SSE:
//                                UserBehaviorAnalysis.reportSSeMsgError(ssContext.getLSID(),imMessage.getTarget().getId(),
//                                        imMessage.getId(),imMessage.getType().toString(),"send time out 5000", SSeMsgError.SENDMSG);
//                                break;
//                        }
//                    }
//                }
//            }
//        }
//
//    }
//
//    public void sseSendMessageError(IMMessage imMessage) {
//        try {
//            synchronized (lock) {
//                String msgId = imMessage.getId();
//                if (!TextUtils.isEmpty(msgId) && mProducer.contains(msgId)) {
//                    mProducer.remove(msgId);
//                    mFailureR.put(msgId);
//                }
//            }
//            receiverMessage(imMessage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
