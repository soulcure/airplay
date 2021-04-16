package com.coocaa.publib.data.def;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Huang Haifeng 2016-9-1
 *
 * Des:
 */
public interface IBaseMobileLafites {

	
	public static final class MSG {
		public static final int BASE_MSG = 0x00001;

		public static final int MSG_ON_DEVICES_SEARCH_FINISHED = BASE_MSG + 1;
		public static final int MSG_ON_DEVICE_CONNECT_RESULT = MSG_ON_DEVICES_SEARCH_FINISHED + 1;
		public static final int MSG_ON_DEVICE_ACTIVE = MSG_ON_DEVICE_CONNECT_RESULT + 1;
		public static final int MSG_ON_DEVICE_INACTIVE = MSG_ON_DEVICE_ACTIVE + 1;

		public static final int MSG_SOGOU_START_RUN = BASE_MSG + 0x1000;
		public static final int MSG_SOGOU_STOP_RUN = MSG_SOGOU_START_RUN + 1;
		public static final int MSG_SOGOU_ON_RESULT = MSG_SOGOU_STOP_RUN + 1;
		public static final int MSG_SOGOU_ON_ERROR = MSG_SOGOU_ON_RESULT + 1;
		public static final int MSG_SOGOU_ON_RECORD_DB = MSG_SOGOU_ON_ERROR + 1;
		public static final int MSG_SOGOU_ON_PART_RESULT = MSG_SOGOU_ON_RECORD_DB + 1;
		public static final int MSG_SOGOU_CANCEL_RUN = MSG_SOGOU_ON_PART_RESULT + 1;

	}
	
	public enum Intention {
		CHAT,//聊天，百科
		JOKE,//讲笑话
		WEATHER,//天气
		STOCK,//股票
		QUERY_ROUTE,//火车
		FLIGHT,//航班
		CUSTOM,//系统版本号,本机ip地址
		VIDEO,//电影搜索
		SETTING,//打开xx设置,系统控制
		INSTRUCTION,//系统控制
		PLAY_MUSIC,//播放控制
		
		SINASTOCK//新浪股票
	}
	
	/**
	 * 
	 * @author Huang Haifeng 2016-9-20
	 *
	 * Des:聊天列表展示类型，根据类型加载不同控件
	 */
	public enum ChatType {
		TEXT,//纯文本
		STOCKTEXT,//股票图片
		WEATHERTEXT,//天气图片
		BOTTOM//底部测试
	}
	
	/**
	 * 
	 * @author Huang Haifeng 2016-9-26
	 *
	 * Des:手机端提供数据类型
	 */
	public enum PhoneDataType {
		AUDIO_RATE,//音频脉冲
		SOUGOU_PART_RESULT, //逐字上屏
		SOUGOU_START,//点击开始
		SOUGOU_RESULT,//搜狗语义
		STOP_RECORD,//手机停止
		SOUGOU_ERROR,//ERROR
		SCREENSHOT_START,//截屏

		//百度方案更新
		DUEROS_REQUEST_START_RAWDATA, //开始采集音频
		DUEROS_REQUEST_STOP_RAWDATA,  //结束采集音频
		DUEROS_REQUEST_CANCEL_RAWDATA,  //取消本次识别
		DUEROS_REQUEST_RAWDATA,		//音频数据
		DUEROS_REQUEST_START_SCREENSHOT, //向电视端发送截屏请求
	}

	/*电视端酷开精灵安装状态*/
	public enum LafiteAppStatus {
		UNINSTALL,		//未安装
		INSTALLED,		//已安装
		DOWNLOADING	//下载中
	}

	public enum LafiteAppDownloadStatus {
		DOWNLOAD_ON_READY(1000),
		DOWNLOAD_ON_PREPARE(1001),
		DOWNLOAD_ON_START(1002),
		DOWNLOAD_ON_STOP(1003),
		DOWNLOAD_ON_FINISH(1004),
		DOWNLOAD_ON_DELETE(1005),
		DOWNLOAD_ON_ERROR(1006);

		// 定义私有变量
		private int value ;

		// 构造函数，枚举类型只能为私有
		private LafiteAppDownloadStatus( int value) {
			this.value = value;
		}

		//从int到enum的转换
		private static final Map<Integer, LafiteAppDownloadStatus> intToEnum = new HashMap<Integer, LafiteAppDownloadStatus>();
		static {
			for (LafiteAppDownloadStatus status : values()) {
				intToEnum.put(status.value(), status);
			}
		}

		public static LafiteAppDownloadStatus valueOf(int value) {
			return intToEnum.get(value);
		}

		public int value() {
			return this.value;
		}

		@Override
		public String toString() {
			return String.valueOf (this.value);
		}
	}

}
