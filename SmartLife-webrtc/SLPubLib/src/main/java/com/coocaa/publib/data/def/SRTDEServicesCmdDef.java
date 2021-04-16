package com.coocaa.publib.data.def;

public class SRTDEServicesCmdDef
{

    public enum SkyDEServiceCommandEnum
    {
        /**
         * 新电视派推送视屏到电视
         */
        SKY_COMMAND_IPTV_PLAYLIVE,
        /**
         * 新旧推送视频
         */
        SKY_COMMAND_PLAYER_PLAY,
        SKY_COMMAND_CALL_PLAYER,
        /**
         * 新旧播放暂停
         */
        SKY_COMMAND_PLAYER_PAUSE_OR_RESUME,
        SKY_COMMAND_PLAYER_PLAY_OR_PAUSE,

        SKY_COMMAND_PLAYER_SEEK,
        SKY_COMMAND_PLAYER_STOP,
        /**
         * 停止App模式
         */
        SKY_COMMAND_APP_STOP,
        /**
         * 设置音量
         */
        SKY_COMMAND_SET_VOLUME,
        /**
         * 设置音量，旧
         */
        SKY_COMMAND_PLAYER_VOLUME_SEEK,

        /**
         * 获取dongle info
         */
        SKY_COMMAND_GET_DONGLEINFO,

        /**
         * 系统类 获取wifi列表
         */
        SKY_COMMAND_GET_APPINFO,

        SKY_COMMAND_SET_MUTE,

        /**
         * 新旧系统升级
         */
        SKY_COMMAND_SET_UPGRADE,
        SKY_COMMAND_EXEC_UPGRADE,

        SKY_COMMAND_SET_MIRACAST,
        SKY_COMMAND_DONGLE_SWITCH_SOURCE,
        SKY_COMMAND_CONNECT_AP,
        SKY_COMMAND_DISCONNECT_AP,
        SKY_COMMAND_FORGET_AP,
        SKY_COMMAND_MAX_NUM,
        SKY_COMMAND_SET_UNMUTE,
        SKY_COMMAND_STOP_APP,
        SKY_COMMAND_CHANG_SETTING,
        SKY_COMMAND_RESET,
        SKY_COMMAND_CLEAR_TV_DATA,
        SKY_COMMAND_SENSOR_CHANGED,
        SKY_COMMAND_UPDATE_APP,
        SKY_COMMAND_UNINSTALL_APP,
        SKY_COMMAND_HEADSET_AUDIO_CTRL,
        /**
         * 关屏待机
         */
        SKY_CFG_SYSTEM_CTRL_SUSPEND,
        SKY_CFG_SYSTEM_CTRL_WAKE,
        SKY_COMMAND_TV_SYSTEM_CTRL,
        /**
         * 关闭电源 a55 a43
         */
        SKY_COMMAND_POWER_OFF,

        /**
         * 发送文字
         */
        SKY_COMMAND_VOICE_TEXT,
        /**
         * 启动应用
         */
        SKY_COMMAND_START_APP,
        /**
         * dongle系统控制
         */
        SKY_COMMAND_DONGLE_SYSTEM_CTRL,

        /**
         * Player Control
         */
        SKY_COMMAND_PLAYER_PAUSE,
        SKY_COMMAND_PLAYER_PREV,
        SKY_COMMAND_PLAYER_NEXT,
        SKY_COMMAND_PLAYER_FFW,
        SKY_COMMAND_PLAYER_REW,
        SKY_COMMAND_PLAYER_RESUME,
        SKY_COMMAND_PLAYER_ROTATE,
        SKY_COMMAND_PLAYER_SEEKMODEL,
        SKY_COMMAND_PLAYER_UNSEEKMODEL,

        /**
         * 
         */
        SKY_COMMAND_SET_DISPLAY_MODE,

        /** 用来a55恢复出厂设置 **/
        SKY_COMMAND_SET_SYSTEM_RESTORE,
        SKY_COMMAND_TV_RESET,

        SKY_COMMAND_DGTV_SETTING,
        SKY_COMMAND_BLUETOOTH_SETTING,
        SKY_SETTING_BLUETOOTH_RESULT,

        /** 搜素蓝牙 **/
        SKY_SETTING_BLUETOOTH_START_DISCOVERY,
        
        /**重启电视**/
        SKY_COMMAND_REBOOT,
        
        /**设置A系列的城市信息**/
        SKY_COMMAND_SET_CITYINFO,

        /** 截屏 **/
        SKY_COMMAND_SCREENSHOT,

        /** 电视是否支持截屏 **/
        SKY_COMMAND_IS_SUPPORT_SCREENSHOT,

        /** 播放直播频道 **/
        SKY_COMMAND_PLAY_LIVE_CHANNEL,
    }

    public enum SkyDEServiceInputEnum
    {
        /**
         * 遥控按键
         */
        SKY_COMMAND_INPUT_KEY_PRESS,
        /**
         * 鼠标移动
         */
        SKY_COMMAND_INPUT_MOVE,
        /**
         * 触屏事件
         */
        SKY_COMMAND_INPUT_TOUCH,
        /**
         * 鼠标点击
         */
        SKY_COMMAND_INPUT_MOUSE_CLICK
    }

    /**
     * @author langge
     *
     */
    public enum SkyDEServiceInfoEnum
    {

        /**
         * Notify:通知手机，电视端资源退出
         */
        SKY_INFO_MEDIA_EXIT,
        /**
         * Notify:通知手机，电视端播放器退出
         */
        SKY_INFO_PLAYER_EXIT,
        /**
         * Query: 获取电视当前正在运行的APP信息
         */
        SKY_INFO_CURRENT_APP,
        /**
         * Notify：通知手机，电视播放器数据
         */
        SKY_INFO_MEDIA_DATA,
        // SKY_INFO_NOTIFY_MEDIA_DATA,
        /**
         * Query:手机查询：获取电视正在播放的媒体信息
         */
        // SKY_INFO_QUERY_MEDIA_DATA,
        /**
         * Query:手机查询：获取电视端的服务器入口
         */
        SKY_INFO_ENTRYPOINT,
        /**
         * Query:手机查询：获取电视端的设置数值、状态等
         */
        SKY_SYSTEM_SETTING,
        /**
         * Notify: 通知手机，电视用户帐号登录
         */
        SKY_INFO_USER_LOGIN,
        /**
         * Notify: 通知手机，电视用户帐号注销
         */
        SKY_INFO_USER_LOGOUT,
        /**
         * Notify: 通知手机，电视用户帐号切换
         */
        SKY_INFO_USER_CHANGED,
        /**
         * Notify：通知手机，电视浏览器数据
         */
        SKY_INFO_BROWSER_DATA,
        /**
         * Notify：通知手机，电视有游戏手柄的游戏数据
         */
        SKY_INFO_GAME_DATA,

        /**
         * Notify: 通知手机当前播放器的播放信息
         */
        SKY_INFO_GET_PLAYDATA,

        /**
         * Notify: 通知手机当前播放器的播放进度值
         */
        SKY_INFO_PLAYER_SEEK,
        /**
         * Notify: 获取当前播放器的播放状态
         */
        SKY_INFO_PLAYER_STATE,
        /**
         * Notify: 通知手机弹出/关闭输入法
         */
        SKY_INFO_INPUT_METHOD_STATUS,
        /**
         * Query: 获取TV端所有用户信息
         */
        SKY_INFO_ALL_USERINFO,
        /**
         * 获取TV端当前用户信息
         */
        SKY_INFO_CURRENT_USERINFO,
        /* ---------------------------- */
        /**
         * Notify: TV 网络状态改变，测试
         */
        SKY_INFO_NET_CHANGED,
        /* ---------------------------- */
        /**
         * Query:获取所有频道列表
         */
        SKY_INFO_ALL_PROGRAMS,
        /**
         * Query: 获取当前节目信息
         */
        SKY_INFO_CURRENT_PROGRAMS,
        @Deprecated
        SKY_INFO_SCREENSHOT_URL,
        /**
         * Query:获取Family ID（即电视ID）
         */
        SKY_INFO_FAMILY_ID,
        // SKY_CMD_SUISERVICE_TERMIALTEXT,
        /**
         * Notify:注册换台通知
         */
        SKY_INFO_NOTIFY_CHANGE_CHANNEL,
        /**
         * Notify:注册电视助手菜单显示状态通知
         */
        SKY_INFO_NOTIFY_ASSISTANT_STATUS,
        /**
         * Notify:电视系统名称通知
         */
        SKY_INFO_SYSTEM_NAME,
        /**
         * Notify:DTV当前频道ID
         */
        SKY_INFO_DTV_ID,
        /**
         * Notify:当前频道
         */
        SKY_INFO_TV_CHANNEL,
        /**
         * Query:当前频道
         */
        SKY_INFO_QUERY_TV_CHANNEL,
        /**
         * Query:获取ap list
         */
        SKY_INFO_APLIST,
        /**
         * Notify:返回AP连接状态
         */
        SKY_INFO_CONNECT_AP,
        /**
         * Notify:返回dongle 信息
         */
        SKY_INFO_DONGLE_DATA,
        /**
         * Notify:推送是否成功
         */
        SKY_INFO_PUSH_STATUS,
        /**
         * Notify:dongle当前时间
         */
        SKY_INFO_DONGLE_CURTIME,
        /**
         * Notify:dongle升级信息
         */
        SKY_INFO_DONGLE_UPDATE,
        /**
         * Notify:获取TV所有应用列表
         */
        SKY_INFO_APP_LIST,

        SKY_INFO_GET_RESOURCEDATA,

        /**
         * dv added 主动返回播放进度
         */
        SKY_INFO_GET_TIMEDATA,

        /*******************************************************/
        /* 系统类 */
        /*******************************************************/
        /**
         * 获取wifi列表
         */
        SKY_INFO_GET_APLIST,
        /**
         * 获取wifi列表详细信息
         */
        SKY_INFO_GET_APINFO,
        /**
         * 获取设备信息,mac
         */
        SKY_INFO_GET_UPGRADEDATA,
        /**
         * 获取当前连接wifi
         */
        SKY_INFO_GET_CONNECTINFO,

        /**
         * 获取设备mac地址
         */
        SKY_INFO_GET_DEVICEMAC,

        /**
         *
         */
        SKY_INFO_SYSTEM_SETTING,
        /**
         * A55 setting
         */
        SKY_INFO_GET_DGTV_SETTING,
        /**
         * 电视基本信息
         */
        SKY_INFO_GET_ABOUT,

        SKY_INFO_GET_IPINFO,

        /** 电视设置网络是否成功 **/
        SKY_INFO_CONNECTAP_STATE,

        /** 电视断开网络是否成功 **/
        SKY_INFO_DISCONNECTAP_STATE,

        /** 电视忘记网络是否成功 **/
        SKY_INFO_FORGETAP_STATE,
        SKY_SETTING_BLUETOOTH_RESULT,

        /* A55 wifi 状态 */
        SKY_INFO_NET_STATE_CHANGED,

        /** 搜素蓝牙 **/
        SKY_SETTING_BLUETOOTH_START_DISCOVERY,

        /** 查询音量 **/
        SKY_INFO_GET_VOLUME,

        /** 查询用户登录信息 **/
        SKY_INFO_GET_USER_TOKEN,

        /*获取系统环境变量*/
        SKY_INFO_GET_SYSTEM_ENV
    }

    public enum SkyDESensorEnum
    {
        /**
         * 传感事件
         */
        SKY_COMMAND_SENSOR_CHANGED
    }

    public enum SkyDEServiceChannelEnum
    {
        /**
         * @Fields SKY_COMMAND_APPSTORE_START_APP TODO key:pkgname value:String pkgname<br/>
         */
        SKY_COMMAND_APPSTORE_MOBILE_START_APP,
        /**
         * @Fields SKY_COMMAND_APPSTORE_GET_INSTALLED_APPS TODO key:result value:String jsonlist<br/>
         *         result item: class AppManagerDatas.AppInfo<br/>
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_INSTALLED_APPS,
        /**
         * @Fields SKY_COMMAND_APPSTORE_DOWNLOAD_SKYAPP TODO(write something)
         */
        SKY_COMMAND_APPSTORE_MOBILE_GET_VERSION,
        SKY_COMMAND_APPSTORE_MOBILE_DOWNLOAD_SKYAPP,
        SKY_COMMAND_APPSTORE_MOBILE_PAUSEDOWNLOAD_SKYAPP,
        SKY_COMMAND_APPSTORE_MOBILE_STARTDOWNLOAD_SKYAPP,
        SKY_COMMAND_APPSTORE_MOBILE_CANCELDOWNLOAD_SKYAPP,
        SKY_COMMAND_APPSTORE_MOBILE_GET_APPSTATUS,
        SKY_COMMAND_APPSTORE_MOBILE_GET_APKINFO,
        SKY_COMMAND_APPSTORE_MOBILE_INSTALL_APP,
        SKY_COMMAND_APPSTORE_MOBILE_UNINSTALL_APP,
        SKY_COMMAND_APPSTORE_MOBILE_GET_TV_CHANNELVERSION,
        SKY_COMMAND_APPSTORE_MOBILE_GET_TV_INFO,
        SKY_COMMAND_APPSTORE_MOBILE_SPEEDUP,
        SKY_COMMAND_APPSTORE_MOBILE_UPDATE_APPS_FLAG,

        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONREADY,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPREPARE,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTART,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONSTOP,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONFINISH,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONDELETE,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONPROCESS,
        SKY_COMMAND_APPSTORE_TV_DOWNLOAD_ONERROR,

        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLSTART,
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONINSTALLED,
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLSTART,
        SKY_COMMAND_APPSTORE_TV_APPMANAGER_ONUNINSTALLED,
        SKY_COMMAND_APPSTORE_TEST,
        SKY_COMMAND_APPSTORE_MOBILE_GET_DOWNLOADED_SKYAPPS,
        
    }

    public enum UserServiceCmdEnum
    {
        USERSERVICE_CMD_CLEAR_HISTORIES, USERSERVICE_CMD_CLEAR_FAVORITES
    }

    public enum SkyLaserTVCmdEnum {
        /**
         * 启动电视端矫正App
         */
        LASER_TV_START,
        /**
         * 电视端App显示矫正的图片
         */
        LASER_TV_SHOW_PATTERN_IMAGE,
        /**
         * 发送测量出来的坐标
         */
        LASER_TV_SEND_POINT_INFO,
        /**
         * 电视端通过de返回处理结果
         */
        LASER_TV_RESULT,
    }
}
