package com.coocaa.publib.data.def;

public enum SkyMediaType {
	// 以后加类别请注意加在后面，因为IOS开发判断类别是通过枚举值的。
	/**
	 * @Fields MEDIA_VIDEO TODO(本地电影)
	 */
	MEDIA_VIDEO,
	/**
	 * @Fields MEDIA_AUDIO TODO(本地音乐)
	 */
	MEDIA_AUDIO,
	/**
	 * @Fields MEDIA_PICTURE TODO(本地图片)
	 */
	MEDIA_PICTURE,
	/**
	 * @Fields MEDIA_ONLINE_MOVIE TODO(在线电影)
	 */
	MEDIA_ONLINE_MOVIE,
	/**
	 * @Fields MEDIA_ONLINE_MUSIC TODO(在线音乐)
	 */
	MEDIA_ONLINE_MUSIC,
	/**
	 * @Fields MEDIA_ONLINE_PICTURE TODO(在线图片)
	 */
	MEDIA_ONLINE_PICTURE,
	/**
	 * @Fields MEDIA_FM_MUSIC TODO(电台音乐)
	 */
	MEDIA_FM_MUSIC,
	/**
	 * @Fields MEDIA_ONLINE_NEWS TODO(在线资讯)
	 */
	MEDIA_ONLINE_NEWS,
	/**
	 * @Fields MEDIA_DLNA_VIDEO TODO(DLNA目录电影)
	 */
	MEDIA_DLNA_DIR_VIDEO,
	/**
	 * @Fields MEDIA_DLNA_AUDIO TODO(DLNA目录音乐)
	 */
	MEDIA_DLNA_DIR_AUDIO,
	/**
	 * @Fields MEDIA_DLNA_PICTURE TODO(DLNA目录图片)
	 */
	MEDIA_DLNA_DIR_PICTURE,
	/**
	 * @Fields MEDIA_SAMBA_VIDEO TODO(SAMBA目录电影)
	 */
	MEDIA_SAMBA_DIR_VIDEO,
	/**
	 * @Fields MEDIA_SAMBA_AUDIO TODO(SAMBA目录音乐)
	 */
	MEDIA_SAMBA_DIR_AUDIO,
	/**
	 * @Fields MEDIA_SAMBA_PICTURE TODO(SAMBA目录图片)
	 */
	MEDIA_SAMBA_DIR_PICTURE,
	/**
	 * @Fields MEDIA_DLNA_RENDER_VIDEO TODO(DLNA推送电影)
	 */
	MEDIA_DLNA_RENDER_VIDEO,
	/**
	 * @Fields MEDIA_DLNA_RENDER_AUDIO TODO(DLNA推送音乐)
	 */
	MEDIA_DLNA_RENDER_AUDIO,
	/**
	 * @Fields MEDIA_DLNA_RENDER_PICTURE TODO(DLNA推送图片)
	 */
	MEDIA_DLNA_RENDER_PICTURE,
	/**
	 * @Fields MEDIA_AIR_PLAYER_VIDEO TODO(AirPlayer推送电影)
	 */
	MEDIA_AIR_PLAYER_VIDEO,
	/**
	 * @Fields MEDIA_AIR_PLAYER_AUDIO TODO(AirPlayer推送音乐)
	 */
	MEDIA_AIR_PLAYER_AUDIO,
	/**
	 * @Fields MEDIA_AIR_PLAYER_PICTURE TODO(AirPlayer推送图片)
	 */
	MEDIA_AIR_PLAYER_PICTURE,
	/**
	 * @Fields MEDIA_RTSP_VIDEO TODO(RTSP视频)
	 */
	MEDIA_RTSP_VIDEO,

	/**
	 * @Fields MEDIA_QIYI_VIDEO TODO(奇艺视频)
	 */
	MEDIA_QIYI_VIDEO,

	/**
	 * @Fields MEDIA_PHONE_VIDEO TODO(手机推送视频)
	 */
	MEDIA_PHONE_RENDER_VIDEO,
	/**
	 * @Fields MEDIA_PHONE_RENDER_AUDIO TODO(手机推送音频)
	 */
	MEDIA_PHONE_RENDER_AUDIO,
	/**
	 * @Fields MEDIA_OTHER_VIDEO TODO(其他情况视频)
	 */
	MEDIA_OTHER_VIDEO,
	/**
	 * @Fields MEDIA_OTHER_AUDIO TODO(其他情况音频)
	 */
	MEDIA_OTHER_AUDIO,
	/**
	 * @Fields MEDIA_OTHER_PICTURE TODO(其他情况图片)
	 */
	MEDIA_OTHER_PICTURE,
	/**
	 * @Fields MEDIA_MYAPP TODO(我的应用)
	 */
	MEDIA_MYAPP, MEDIA_CUSTOM, MEDIA_TEXT,
	/**
	 * @Fields MEDIA_WEBVIEW TODO(web view 展示)
	 */
	MEDIA_WEBVIEW, MEDIA_BROWSER, MEDIA_ERROR,

	/**
	 * @Fields MEDIA_DTV guiqingwen add for dtv
	 */
	MEDIA_DTV,

	/**
	 * @Fields MEDIA_RADIO_STATION TODO(广播电台)
	 */
	MEDIA_RADIO_STATION,

	/**
	 * @Fields MEDIA_ONLINE_MOVIE TODO(网址导航)
	 */
	MEDIA_ONLINE_WEBSITE,

	/**
	 * @Fields MEDIA_ONLINE_APP TODO(网址搜索到的应用)
	 */
	MEDIA_ONLINE_APP, MEDIA_4K_PICTURE, MEDIA_ACTION,
	/**
	 * @Fields MEDIA_PHONE_RENDER_AUDIO TODO(手机推送直播)
	 */
	MEDIA_PHONE_RENDER_TV,
	/**
	 * Description:(浏览器视频)
	 */
	MEDIA_BROWSER_VIDEO,
	/**
	 * @Fields MEDIA_VIDEO TODO(本地直播)
	 */
	MEDIA_LIVE
}
