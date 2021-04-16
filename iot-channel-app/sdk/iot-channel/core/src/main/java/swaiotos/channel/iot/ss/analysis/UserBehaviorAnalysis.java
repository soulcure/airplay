package swaiotos.channel.iot.ss.analysis;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.analysis.data.LocalConnectError;
import swaiotos.channel.iot.ss.analysis.data.LocalConnectSuccess;
import swaiotos.channel.iot.ss.analysis.data.SSConnect;
import swaiotos.channel.iot.ss.analysis.data.SSeInit;
import swaiotos.channel.iot.ss.analysis.data.SSeInitError;
import swaiotos.channel.iot.ss.analysis.data.SSeMsgError;
import swaiotos.channel.iot.ss.analysis.data.ServerInterfaceMsg;
import swaiotos.channel.iot.ss.analysis.data.ServerInterfaceMsgError;
import swaiotos.channel.iot.ss.server.data.log.ReportData;
import swaiotos.channel.iot.ss.server.data.log.ReportDataUtils;
import swaiotos.channel.iot.ss.server.http.SessionHttpService;
import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.utils.AndroidLog;

public class UserBehaviorAnalysis {
	private static ExecutorService singleThreadExecutor;
	private static LinkedBlockingQueue<ReportData> blockingQueue;
	private static boolean isRunning;
	private static Future<?> future;
	public static String deviceType = "";//上报终端类型，mobile(手机）、dongle、tv、panel；
	public static String userId = "";
	public static String wifiSSID = "";

	public static void init() {

		blockingQueue = new LinkedBlockingQueue<>();
		singleThreadExecutor = Executors.newSingleThreadExecutor();
		future = singleThreadExecutor.submit(new Runnable() {

			@Override
			public void run() {

				Thread.currentThread().setName("behavior-thread");

				isRunning = true;
				final AtomicBoolean hasGet = new AtomicBoolean(true);
				final HttpSubscribe<HttpResult<Void>> callback = new HttpSubscribe<HttpResult<Void>>() {
					@Override
					public void onSuccess(HttpResult<Void> result) {
						synchronized (hasGet) {
							hasGet.set(true);
							hasGet.notifyAll();
						}
					}

					@Override
					public void onError(HttpThrowable error) {
						synchronized (hasGet) {
							hasGet.set(true);
							hasGet.notifyAll();
						}
					}
				};

				try {
					while (isRunning) {

						// get a request
						ReportData reportData = blockingQueue.take();

						if (null == reportData) {
							continue;
						}

						AndroidLog.androidLog("blockingQueue, take a request, command---");

						while (!hasGet.get()) {
							synchronized (hasGet) {
								try {
									hasGet.wait(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						synchronized (hasGet) {
							hasGet.set(false);
						}
						HttpApi.getInstance().request(SessionHttpService.SERVICE.reportLog(reportData),callback,"","");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
				}
			}
		});
	}

	public static void unInit() {

		isRunning = false;
		if (future != null) {
			future.cancel(true);
		}
		if (singleThreadExecutor != null) {
			singleThreadExecutor.shutdown();
		}

	}

	/**
	 *
	 * 本地连接建立耗时	localConnectTime
	 *
	 * */
	public static <T> void appEventBehavior (String eventName, T data) {

		ReportData.PayLoadData<T> pData = new ReportData.PayLoadData<T>();
		ReportData.EventData<T> eData = new ReportData.EventData<T>();
		eData.data = data;
		eData.eventName = eventName;
		eData.eventTime = System.currentTimeMillis();
		pData.events = new ArrayList<>();
		pData.events.add(eData);
		if (SSChannelService.getContext() != null) {
			ReportData reportData = ReportDataUtils.getReportData(SSChannelService.getContext(), "iotchannel.link_events", pData);
			blockingQueue.offer(reportData);
		}
	}

	public static void reportServerInterfaceSuccess(String sourceLSID, long time,String interfaceName) {
		ServerInterfaceMsg serverInterfaceMsg = new ServerInterfaceMsg();
		serverInterfaceMsg.setSourceLsid(sourceLSID);
		serverInterfaceMsg.setTime(time);
		serverInterfaceMsg.setMethod(interfaceName);
		serverInterfaceMsg.setDeviceType(deviceType);
		serverInterfaceMsg.setWifiSSID(wifiSSID);

		appEventBehavior(ServerInterfaceMsg.EVENT_NAME,serverInterfaceMsg);
	}

	public static void reportServerInterfaceError(String sourceLSID,String method,String errorCode,String errorDsc) {

		ServerInterfaceMsgError serverInterfaceMsgError = new ServerInterfaceMsgError();
		serverInterfaceMsgError.setSourceLsid(sourceLSID);
		serverInterfaceMsgError.setMethod(method);
		serverInterfaceMsgError.setErrorCode(errorCode);
		serverInterfaceMsgError.setErrorDsc(errorDsc);
		serverInterfaceMsgError.setDeviceType(deviceType);
		serverInterfaceMsgError.setWifiSSID(wifiSSID);

//		appEventBehavior(ServerInterfaceMsgError.EVENT_NAME,serverInterfaceMsgError);  //去除接口请求
	}


	public static void reportSSConnect(String sourceLSID,String targetLSID,long time) {
		SSConnect ssConnect = new SSConnect();
		ssConnect.setSourceLsid(sourceLSID);
		ssConnect.setTargetLsid(targetLSID);
		ssConnect.setTime(time);
		ssConnect.setDeviceType(deviceType);
		ssConnect.setWifiSSID(wifiSSID);

		appEventBehavior(SSConnect.EVENT_NAME,ssConnect);
	}

	public static void reportLocalConnect(String sourceLSID,long time) {
		LocalConnectSuccess localConnectSuccessData = new LocalConnectSuccess();
		localConnectSuccessData.setSourceLsid(sourceLSID);
		localConnectSuccessData.setTime(time);
		localConnectSuccessData.setDeviceType(deviceType);
		localConnectSuccessData.setWifiSSID(wifiSSID);

		appEventBehavior(LocalConnectSuccess.EVENT_NAME,localConnectSuccessData);
	}

	public static void reportLocalConnectError(String sourceLSID,String targetLSID) {
		LocalConnectError localConnectError = new LocalConnectError();
		localConnectError.setSourceLsid(sourceLSID);
		localConnectError.setTargetLsid(targetLSID);
		localConnectError.setErrorCode("-1");
		localConnectError.setErrorDsc("send failure");
		localConnectError.setDeviceType(deviceType);
		localConnectError.setWifiSSID(wifiSSID);

		appEventBehavior(LocalConnectError.EVENT_NAME,localConnectError);
	}

	public static void reportSSeMsgError(String sourceLSID,String targetLSID, String msgId,String type,String dsc,String cmdType,String content) {
		SSeMsgError sSeMsgError = new SSeMsgError();
		sSeMsgError.setSourceLsid(sourceLSID);
		sSeMsgError.setTargetLsid(targetLSID);
		sSeMsgError.setMsgID(msgId);
		sSeMsgError.setErrorCode("-1");
		sSeMsgError.setMsgType(type);
		sSeMsgError.setErrorDsc(dsc);
		sSeMsgError.setCmdType(cmdType);
		sSeMsgError.setDeviceType(deviceType);
		sSeMsgError.setWifiSSID(wifiSSID);
		sSeMsgError.setContent(content);

		appEventBehavior(SSeMsgError.EVENT_NAME,sSeMsgError);
	}

	public static void reportSSeInitTime(String sourceLSID, long time) {
		SSeInit sSeInit = new SSeInit();
		sSeInit.setSourceLsid(sourceLSID);
		sSeInit.setDeviceType(deviceType);
		sSeInit.setWifiSSID(wifiSSID);
		sSeInit.setTime(time);

		appEventBehavior(SSeInit.EVENT_NAME,sSeInit);
	}

	public static void reportSSeInitError(String sourceLSID, String errorCode,String errorDsc) {
		SSeInitError sSeInitError = new SSeInitError();
		sSeInitError.setSourceLsid(sourceLSID);
		sSeInitError.setDeviceType(deviceType);
		sSeInitError.setWifiSSID(wifiSSID);
		sSeInitError.setErrorCode(errorCode);
		sSeInitError.setErrorDsc(errorDsc);

		appEventBehavior(SSeInitError.EVENT_NAME,sSeInitError);
	}

}
