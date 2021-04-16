package com.coocaa.tvpi.common;

/**
 * @ClassName UMengEventId
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/9/2
 * @Version TODO (write something)
 */
public class UMengEventId {
    //登录相关
    //免验登录页面曝光	免验登录界面显示时
    public static final String LOGIN_WITHOUT_SMS_PAGE_SHOW = "login_without_sms_page_show";
    //验证手机号登录页面曝光	验证手机号登录页面显示时
    public static final String LOGIN_WITH_SMS_PAGE_SHOW = "login_with_sms_page_show";
    //获取验证码  点击获取验证码时
    public static final String LOGIN_SMS = "login_sms";
    //图形验证码	获取图形验证码时
    public static final String LOGIN_IMAGE_CAPTCHA = "login_image_captcha";

    //首页相关

    //头部区
    //设备列表点击
    public static final String MAIN_PAGE_DEVICE_LIST = "main_page_device_list";
    //智屏扫码
    public static final String MAIN_PAGE_SCAN = "main_page_scan";
    //智屏电源
    public static final String MAIN_PAGE_POWER = "main_page_power";
    //智屏静音
    public static final String MAIN_PAGE_MUTE = "main_page_mute";
    //智屏同屏控制
    public static final String MAIN_PAGE_CONTROL_TV = "main_page_control_tv";
    //智屏截屏
    public static final String MAIN_PAGE_SCREEN_SHOT= "main_page_screen_shot";
    //智屏遥控器
    public static final String MAIN_PAGE_REMOTE_CONTROL = "main_page_remote_control";

    //金刚区
    //智屏屏幕镜像
    public static final String MAIN_PAGE_CAST_PHONE = "main_page_cast_phone";
    //智屏影视投屏
    public static final String MAIN_PAGE_CAST_MOVIE = "main_page_cast_movie";
    //智屏直播投屏
    public static final String MAIN_PAGE_CAST_LIVE = "main_page_cast_live";
    //智屏电视应用
    public static final String MAIN_PAGE_TV_APP = "main_page_tv_app";
    //智屏本地投屏
    public static final String MAIN_PAGE_CAST_LOCAL = "main_page_cast_local";
    //智屏家长管理
    public static final String MAIN_PAGE_PARENT_CONTROL = "main_page_parent_control";
    //智屏家庭看护
    public static final String MAIN_PAGE_HOME_GUARD = "main_page_home_guard";
    //智屏留言板
    public static final String MAIN_PAGE_MESSAGE = "main_page_message";
    //智屏视频通话
    public static final String MAIN_PAGE_VIDEO_CALL = "main_page_video_call";

    //快捷区
    //智屏视频通话全部
    public static final String MAIN_PAGE_VIDEO_CALL_ALL = "main_page_video_call_all";
    //智屏视频通话添加联系人
    public static final String MAIN_PAGE_VIDEO_CALL_ADD_CONTACT = "main_page_video_call_add_contact";
    //智屏视频通话呼叫
    public static final String MAIN_PAGE_VIDEO_CALL_START = "main_page_video_call_start";

    /**
     * tab点击
     * params：
     * int index(0, 1, 2, 3)
     * String tab_name(smart_screen, smart_home, shopping_mall, my)
     */
    public static final String MAIN_PAGE_TAB = "main_page_tab";

    /**
     * 二维码扫描结果
     * params:
     * String type(device, mall, video_call, web)
     * String result(success, fail)
     */
    public static final String SCAN_QR_RESULT = "scan_qr_result";

    //智屏设备相关
    /**
     * 连接事件
     * params:
     * String type(user, auto) 用户操作或者自动重连
     * String result(success, fail)
     */
    public static final String DEVICE_CONNECT = "device_connect";

    /**
     * 添加设备（网络请求成功）
     * params:
     * String result(success, fail)
     */
    public static final String DEVICE_ADD = "device_add";

    /** 该事件待确认需求是否要做弹框
     * 同屏控制广域网弹窗
     * params:
     * String result(cancel, wifi_setting)
     */
    public static final String PHONE_CTR_TV_NET_DIALOG = "phone_ctr_tv_net_dialog";

    //截屏页面
    //再截一张
    public static final String SCREEN_SHOT_AGAIN = "screen_shot_again";

    //截屏未保存
//    public static final String SCREEN_SHOT_UNSAVED = "screen_shot_unsaved";
    //和下面这个重复了，不使用

    /**
     * 保存至相册点击
     * params:
     * String isClicked(true, false)
     */
    public static final String SCREEN_SHOT_SAVE = "screen_shot_save";
    /**
     * 截屏分享
     * params:
     * String platform(wechat, wechat_circle, QQ, QZone)
     */
    public static final String SCREEN_SHOT_SHARE = "screen_shot_share";

    /**
     * 遥控器使用详情
     * params:
     * String event(定义的遥控key的String)
     */
    public static final String REMOTE_CONTROL_OPERATION = "remote_control_operation";

    //直播投屏 点击任意一个投屏时
    public static final String CHANNEL_CAST = "channel_cast";

    //应用相关
    //电视应用页面 电视应用应用tab展示
    public static final String TV_APP_PAGE_SHOW = "tv_app_page_show";
    //电视应用页面 应用商店tab展示
    public static final String APP_STORE_PAGE_SHOW = "app_store_page_show";
    //应用搜索页面展示
    public static final String APP_SEARCH_PAGE_SHOW = "app_search_page_show";
    //应用搜索按钮
    public static final String APP_SEARCH_BTN_CLICK = "app_search_btn_click";
    //应用卸载
    public static final String APP_UNSTALL = "app_unstall";
    /**
     * 应用安装
     * params:
     * String page(main 应用首页, category 分类页, search 搜索页)
     */
    public static final String APP_INSTALL = "app_install";
    //应用安装成功
    public static final String APP_INSTALLED = "app_installed";

    //本地投屏相关
    //图片投屏页面
    public static final String CAST_PICTURE_PAGE_SHOW = "cast_picture_page_show";
    /**
     * 图片投屏次数
     * params:
     * String result(success, fail)
     */
    public static final String CAST_PICTURE = "cast_picture";

    //音乐投屏页面
    public static final String CAST_MUSIC_PAGE_SHOW = "cast_music_page_show";
    /**
     * 音乐投屏次数
     * params:
     * String result(success, fail)
     */
    public static final String CAST_MUSIC = "cast_music";

    //视频投屏页面
    public static final String CAST_VIDEO_PAGE_SHOW = "cast_video_page_show";
    /**
     * 视频投屏次数
     * params:
     * String result(success, fail)
     */
    public static final String CAST_VIDEO = "cast_video";

    /**
     * 本地文件投屏次数（三合一）
     * params:
     * String type(picture, music, video)
     * String result(success, fail)
     */
    public static final String CAST_LOCAL_RESOURCE = "cast_local_resource";

}
