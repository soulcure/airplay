package com.coocaa.tvpi.common;

/**
 * Created by IceStorm on 2018/2/5.
 */

public class UMEventId {

    /* 首页 start */
        // app底部tab   参数：tab_name
        public static final String CLICK_HOME_TAB = "click_home_tab";
        //点击底部悬浮栏，语音，遥控
        public static final String CLICK_FLOATING_BAR = "click_floating_bar";
        //点击悬浮栏-语音
        public static final String CLICK_FLOATING_VOICE = "click_floating_voice";
        //点击悬浮栏-遥控
        public static final String CLICK_FLOATING_REMOTE = "click_floating_remote";
    /* 首页 end */


    /* 播放器 start */
        // 播放器-播放次数 参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_PLAY_COUNT = "click_player_play_count";

        // 播放器-缓冲中断次数 参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_BUFFERING_COUNT = "click_player_buffering_count";

        // 播放器-切换全屏 参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_FULLSCREEN = "click_player_fullscreen";

        // 播放器-进度拖动 参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_DRAGGING = "click_player_dragging";

        // 播放器-调节音量  参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_VOLUME = "click_player_volume";

        // 播放器-调节亮度 参数:(source video_type video_class channel_name)
        public static final String CLICK_PLAYER_BRIGHTNESS = "click_player_brightness";

        // 播放器-播放时长 参数:(source video_type video_class channel_name)      #### 计算
        public static final String CLICK_PLAYER_PLAY_TIME = "click_player_play_time";

        // 播放器-播完进度 (完成比例：分10档，1到10分即可) 参数:(rate)                #### 计算
        public static final String CLICK_PLAYER_PROGRESS = "click_player_progress";

        // 解析器-解析请求 (片源：腾讯、爱奇艺、西瓜。。) 参数：(source)                                         #### 计算
        public static final String CLICK_PLAYER_EXTRACTOR_COUNT = "click_player_extractor_count";

        // 解析器-解析成功 (片源：腾讯、爱奇艺、西瓜。。) 参数：(source)                                         #### 计算
        public static final String CLICK_PLAYER_EXTRACTOR_SUCCESS = "click_player_extractor_success";

        // 解析器-解析失败 (片源：腾讯、爱奇艺、西瓜。。) 参数：(source)                                         #### 计算
        public static final String CLICK_PLAYER_EXTRACTOR_FAILED = "click_player_extractor_failed";
    /*  播放器 end */


    /* 遥控器 start */
        // 遥控-图标点击 (来源页面：推荐、分类、直播、、、) 参数：(source_page)
        public static final String CLICK_REMOTE_ENTER = "click_remote_enter";

        // 遥控-底部切换模式 参数：(switch_type)
        public static final String CLICK_REMOTE_SWITCH_MODE = "click_remote_switch_mode";

        // 遥控-模式 (进入该模式的时候计数)
        public static final String CLICK_REMOTE_MODE = "click_remote_mode";

        // 遥控-关机
        public static final String CLICK_REMOTE_POWER_OFF = "click_remote_power_off";

        // 遥控-静音
        public static final String CLICK_REMOTE_MUTE = "click_remote_mute";

        // 遥控-音量减小
        public static final String CLICK_REMOTE_VOLUME_MINUS = "click_remote_volume_minus";

        // 遥控-音量增加
        public static final String CLICK_REMOTE_VOLUME_PLUS = "click_remote_volume_plus";

        //调节音量（上下滑）
        public static final String REMOTE_TOUCH_VOLUME = "remote_touch_volume";
        //调节进度(左右滑)
        public static final String REMOTE_TOUCH_PROGRESS = "remote_touch_progress";

        //遥控-中间-确定键
        public static final String REMOTE_CLICK_CENTER = "remote_click_center";
        //遥控-方向键-上
        public static final String REMOTE_CLICK_UP = "remote_click_up";
        //遥控-方向键-下
        public static final String REMOTE_CLICK_DOWN = "remote_click_down";
        //遥控-方向键-左
        public static final String REMOTE_CLICK_LEFT = "remote_click_left";
        //遥控-方向键-右
        public static final String REMOTE_CLICK_RIGHT = "remote_click_right";

        // 遥控-触摸模式-触摸方向 参数:(direct)
        public static final String CLICK_REMOTE_TOUCH_DIRECT = "click_remote_touch_direct";

        // 遥控-触摸模式-返回
        public static final String CLICK_REMOTE_TOUCH_BACK = "click_remote_touch_back";

        // 遥控-触摸模式-主页
        public static final String CLICK_REMOTE_TOUCH_HOME = "click_remote_touch_home";

        // 遥控-触摸模式-设置
        public static final String CLICK_REMOTE_TOUCH_SETTING = "click_remote_touch_setting";


        // 遥控-播放模式-触摸方向 参数:(direct)
        public static final String CLICK_REMOTE_PLAY_DIRECT = "click_remote_play_direct";

        // 遥控-播放模式-音量设置 参数:(volume)
        public static final String CLICK_REMOTE_PLAY_VOLUME = "click_remote_play_volume";

        // 遥控-播放模式-进度条拖拽  参数:(direct)
        public static final String CLICK_REMOTE_PLAY_DRAG = "click_remote_play_drag";

        // 遥控-播放模式-上一个
        public static final String CLICK_REMOTE_PLAY_PRE = "click_remote_play_pre";

        // 遥控-播放模式-静音
        public static final String CLICK_REMOTE_PLAY_SILENCE = "click_remote_play_silence";

        // 遥控-播放模式-下一个
        public static final String CLICK_REMOTE_PLAY_NEXT = "click_remote_play_next";


        // 搜索连接-搜索设备数 参数:(device_count)      #### 计算
        public static final String CLICK_REMOTE_CONNECT_SEARCH_TV_COUNT = "click_remote_connect_search_tv_count";

        // 搜索连接-搜索时长 参数:(second)              #### 计算
        public static final String CLICK_REMOTE_CONNECT_SEARCH_TIME = "click_remote_connect_search_time";

        // 搜索连接-连接设备
        public static final String CLICK_REMOTE_CONNECT_TV_COUNT = "click_remote_connect_tv_count";

        // 遥控-帮助按钮-点击
        public static final String CLICK_REMOTE_HELP = "click_remote_help";

        // 遥控-初次引导-点击
        public static final String CLICK_REMOTE_FIRST_GUIDE = "click_remote_first_guide";

        // 遥控-搜索结果 result(success, failure)
        public static final String REMOTE_SEARCH_RESULT = "remote_search_result";

        // 遥控-连接结果 参数：type(web， dlna), result(success, failure)
        public static final String REMOTE_CONNECT_RESULT = "remote_connect_result";


    /* 遥控器 end */


    /* 搜索 start */
        /*// 搜索-入口 参数:(source_page)--这个用页面跳转统计替换了
        public static final String CLICK_SEARCH_ENTER = "click_search_enter";*/

        // 搜索-搜索次数
        public static final String CLICK_SEARCH_COUNT = "click_search_count";

        // 搜索-结果点击次数 (分类：短视频、长视频) 参数：（video_type）
        public static final String CLICK_SEARCH_RESULT = "click_search_result";

        // 搜索-历史条目点击
        public static final String CLICK_SEARCH_HISTORY_ITEM = "click_search_history_item";

        // 搜索-删除历史
        public static final String CLICK_SEARCH_HISTORY_CLEAN = "click_search_history_clean";

        // 搜索-热门条目点击
        public static final String CLICK_SEARCH_HOT_ITEM = "click_search_hot_item";

        // 搜索-清除输入文字
        public static final String CLICK_SEARCH_CLEAN = "click_search_clean";

        // 搜索-关闭
        public static final String CLICK_SEARCH_CLOSE = "click_search_close";
    /* 搜索 end */


    /* 推荐首页 start */
        // 推荐-分类切换  参数:(video_class)
        public static final String CLICK_RECOMMEND_VIDEO_CLASS = "click_recommend_video_class";

        // 推荐-Banner点击 参数：（video_class--现在只有首页顶部有，不传参数最好）
        public static final String CLICK_RECOMMEND_BANNER = "click_recommend_banner";

        // 推荐-视频列表点击
        public static final String CLICK_RECOMMEND_VIDEO = "click_recommend_video";

        //短视频-左滑-相关正片
        public static final String HOME_SHORT_VIDEO_SWIPE_LEFT = "home_short_video_swipe_left";
        //短视频-右滑-不喜欢
        public static final String HOME_SHORT_VIDEO_SWIPE_RIGHT = "home_short_video_swipe_right";
    /* 推荐首页 end */

    //推送至电视 参数:(source video_type video_class channel_name)
    public static final String CLICK_PUSH_TO_TV = "click_push_to_tv";
    //喜欢-点击
    public static final String LIKE_CLICK = "like_click";
    //视频扩展view上的更多-点击
    public static final String MORE_CLICK = "more_click";
    //相关正片按钮-点击
    public static final String RELATE_LONG_CLICK = "relate_long_click";
    //不喜欢标签-点击
    public static final String DISLIKE_LABEL_CLICK = "dislike_label_click";
    //我的-历史-点击
    public static final String MINE_HISTORY_CLICK = "mine_history_click";
    //推送历史页面-影片-点击
    public static final String PUSH_HISTORY_CLICK = "push_history_click";
    //播放页-锁屏键-点击
    public static final String PLAYER_LOCK_CLICK = "player_lock_click";
    //播放页-清晰度-点击
    public static final String PLAYER_DEFINITION_CLICK = "player_definition_click";
    //遥控器-打开WiFi按钮-点击
    public static final String REMOTE_OPEN_WIFI_CLICK = "remote_open_WiFi_click";
    //我的-设置-截屏水印-点击
    public static final String MINE_SET_WATERMARK_CLICK = "mine_set_watermark_click";
    //遥控器-输入IP入口-点击
    public static final String REMOTE_INPUT_IP_CLICK = "remote_input_IP_click";
    //输入IP页-按钮-点击
    public static final String IP_PAGE_BUTTON_CLICK = "IP_page_button_click";
    //精选-影片播放-点击
    public static final String HANDPICK_SHORT_CLICK = "handpick_short_click";
    //精选-影片标题-点击
    public static final String HANDPICK_SHORT_TITLE_CLICK = "handpick_short_title_click";


    /* 短视频发现 start */
        // 发现-连续播放视频个数 参数：(play_count)        #### 计算
        public static final String CLICK_DISCOVERY_CONTINUE_PLAY_COUNT = "click_discovery_continue_play_count";

        // 发现-主动点击海报播放
        public static final String CLICK_DISCOVERY_POSTER = "click_discovery_poster";

        // 发现-点击关联正片
        public static final String CLICK_DISCOVERY_RELATE_LONG = "click_discovery_relate_long";

        // 发现-点赞
        public static final String CLICK_DISCOVERY_PRAISE = "click_discovery_praise";

        // 发现-收藏
        public static final String CLICK_DISCOVERY_COLLECT = "click_discovery_collect";
    /* 短视频发现 end */


    /* 分类首页 start*/
        // 分类-Banner点击
        public static final String CLICK_CATEGORY_BANNER = "click_category_banner";

        // 分类-视频分类点击 参数：("video_class source")
        public static final String CLICK_CATEGORY_ITEM = "click_category_item";

        // 分类-App分类点击 参数：("video_class source")
        public static final String CLICK_APP_CATEGORY_ITEM = "click_app_category_item";

        // 分类-推荐视频 参数：("video_class source")
        public static final String CLICK_CATEGORY_RECOMMAND_VIDEO_ITEM = "click_category_recommand_video_item";
    /* 分类首页 end*/


    /* 片库筛选 start*/
        // 片库刷选-刷选条件 (每行条件作为一个参数类型)
        public static final String CLICK_CATEGORY_FILTER_CONDITION = "click_category_filter_condition";

        // 片库-刷选结果点击
        public static final String CLICK_CATEGORY_FILTER_RESULT = "click_category_filter_result";
    /* 片库筛选 end*/


    /* 正片详情页 start*/
        // 正片-进入来源 (来源页面：片库刷选、搜索结果) 参数：(source_page)
        public static final String CLICK_LONG_DETAIL_ENTER = "click_long_detail_enter";

        // 正片-倒计时/了解会员权益点击 (片源：腾讯、爱奇艺) 参数:(source)
        public static final String CLICK_LONG_DETAIL_KNOW_VIP_MORE = "click_long_detail_know_vip_more";

        // 正片-查看所有剧集
        public static final String CLICK_LONG_DETAIL_ALL_EPISODES = "click_long_detail_all_episodes";

        // 正片-切集
        public static final String CLICK_LONG_DETAIL_SWITCH_EPISODE = "click_long_detail_switch_episode";

        // 正片-点赞
        public static final String CLICK_LONG_DETAIL_PRAISE = "click_long_detail_praise";

        // 正片-收藏
        public static final String CLICK_LONG_DETAIL_COLLECT = "click_long_detail_collect";

        // 正片-点击相关正片
        public static final String CLICK_LONG_DETAIL_RELATED_VIDEO = "click_long_detail_related_video";

    /* 正片详情页 end*/


    /* 语音 start*/
        // 语音-录制语音次数
        public static final String CLICK_VOICE_RECORD_COUNT = "click_voice_record_count";

        // 语音-录制语音取消次数
        public static final String CLICK_VOICE_RECORD_CANCEL_COUNT = "click_voice_record_cancel_count";

        // 语音-有效语音次数
        public static final String CLICK_VOICE_EFFECTIVE_RECORD_COUNT = "click_voice_effective_record_count";

        // 语音-录制时长          参数:(second)              #### 计算
        public static final String CLICK_VOICE_RECORD_TIME = "click_voice_record_time";
        //Tv端语音版本
        public static final String TV_VOICE_APP_VERSION = "tv_voice_app_version";
        //语音被TV拒绝回调
        public static final String VOICE_REFUSED_BY_TV_COUNT = "voice_refused_by_tv_count";
        //升级小维AI
        public static final String TV_INSTALL_XIAOWEI_AI = "tv_install_xiaowei_ai";
        //小维AI下载进度
        public static final String TV_XIAOWEI_D_PROGRESS = "tv_xiaowei_d_progress";
        //小维AI安装进度
        public static final String TV_XIAOWEI_I_STATUS = "tv_xiaowei_i_status";


    /* 语音 end*/


    /* 电视台直播首页 start*/
        // 直播-首页-切换频道分类 参数:(channel_type)
        public static final String CLICK_LIVE_CLASS = "click_live_class";

        // 直播-首页-频道列表点击  参数:(channel_name)
        public static final String CLICK_LIVE_CHANNEL= "click_live_channel";
    /* 电视台直播首页 end*/


    /* 电视台直播详情页 start*/
        // 直播-播放次数     参数:(channel_name)
        public static final String CLICK_LIVE_DETAIL_PLAY_COUNT= "click_live_detail_play_count";

        // 直播-倒计时/了解会员权益点击
        public static final String CLICK_LIVE_DETAIL_KNOW_VIP_MORE= "click_live_detail_know_vip_more";

        // 直播-预定
        public static final String CLICK_LIVE_DETAIL_RESERVE= "click_live_detail_reserve";
    /* 电视台直播详情页 end*/


    /* 我的会员权益 start*/
        // 会员-会员类型条目点击  (会员类型名称：奇异果VIP、影视VIP。。。) 参数:(vip_type)
        public static final String CLICK_PRIVILEGE_ITEM= "click_privilege_item";
    /* 我的会员权益 end*/


    /* 会员购买页 start*/
        // 会员-产品包种类点击  参数:(vip_product_type)
        public static final String CLICK_PURCHASE_PRODUCT_ITEM= "click_purchase_product_item";

        // 会员-服务协议点击
        public static final String CLICK_PURCHASE_PROTOCOL= "click_purchase_protocol";

        // 会员-立即开通点击
        public static final String CLICK_PURCHASE_BUY= "click_purchase_buy";

        // 会员-支付方式   参数:(method)
        public static final String CLICK_PURCHASE_PAY_METHOD= "click_purchase_pay_method";

        // 会员-支付拉起微信
        public static final String CLICK_PURCHASE_OPEN_WECHAT= "click_purchase_open_wechat";

        // 会员-微信支付返回结果  参数:(result)
        public static final String CLICK_PURCHASE_WECHAT_PAY_RESULT= "click_purchase_wechat_pay_result";

        // 会员-发起订单金额  参数:("amount pay_method vip_type vip_product_type")        #### 计算
        public static final String CLICK_PURCHASE_ORDER_AMOUNT_PREPAY= "click_purchase_order_amount_prepay";

        // 会员-成功订单的金额 参数:("amount pay_method vip_type vip_product_type")       #### 计算
        public static final String CLICK_PURCHASE_ORDER_AMOUNT_PAYED= "click_purchase_order_amount_payed";
    /* 会员购买页 end*/


    /* 我的 start*/
        // 我的-头像点击
        public static final String CLICK_MINE_AVATAR= "click_mine_avatar";

        // 我的-列表条目点击  (条目名称：我的会员权益、搜索设备、收藏、播放历史、关于) 参数:(item_name)
        public static final String CLICK_MINE_ITEM= "click_mine_item";

        // 收藏类型切换  参数：（item_name）
        public static final String CLICK_COLLECT_TYPE_SWITCH= "click_collect_type_switch";

        // (收藏页) 收藏-点击收藏视频
        public static final String CLICK_COLLECT_ITEM= "click_collect_item";

        // (收藏页) 收藏-删除收藏视频
        public static final String CLICK_COLLECT_DELETE= "click_collect_delete";

        // (播放历史) 历史-点击历史视频
        public static final String CLICK_HISTORY_ITEM= "click_history_item";

        // (播放历史) 历史-删除历史视频
        public static final String CLICK_HISTORY_DELETE= "click_history_delete";

        // (关于)   关于-检查升级    参数:("cur_version target_version")
        public static final String CLICK_UPDATE= "click_update";

        // 我的-本地文件-点击
        public static final String CLICK_MINE_LOCALFILE_MEDIA = "click_mine_localfile_media";

        // 本地-推送-点击
        public static final String CLICK_LOCAL_PUSH = "click_local_push";
    /* 我的 end*/

    /* 一言速荐 start*/
        // 一言速荐列表类型切换  参数：（item_name）
        public static final String CLICK_RECOMMEND_LIST_TYPE_SWITCH="click_recommend_list_type_switch";

        // 一言速荐列表点击
        public static final String CLICK_RECOMMEND_LIST_ITEM = "click_recommend_list_item";

        // 一言速荐详情item点击
        public static final String CLICK_RECOMMEND_DETAIL_ITEM = "click_recommend_detail_item";

        // 一言速荐收藏点击
        public static final String CLICK_RECOMMEND_DETAIL_COLLECT = "click_recommend_detail_collect";

        // 一言速荐item切换  参数：（item_name）
        public static final String CLICK_RECOMMEND_DETAIL_ITEM_SWITCH = "click_recommend_detail_item_switch";

     /* 一言速荐 end*/

    /* 登录页 start*/
        // 登录页  (登录方式：手机验证码、账号密码、token自动登录)   参数:(method)
        public static final String CLICK_LOGIN_METHOD= "click_login_method";

        // 退出
        public static final String CLICK_LOGOUT= "click_logout";
    /* 登录页 end*/

    /*截屏统计时间start*/
    //截屏按钮点击 status:1)normal：正常；2）tryAgain:重载；计数
    public static final String CLICK_SCREEN_SHOT = "click_screen_shot";

    //截屏完成时长 计算
    public static final String SCREEN_SHOT_DURATION = "screen_shot_duration";

    //保存到相册 计数
    public static final String CLICK_SCREEN_SAVE = "click_screen_save";

    //取消 计数
    public static final String CLICK_SCREEN_CANCEL = "click_screen_cancel";

    //完成 计数
    public static final String CLICK_SCREEN_FINISH = "click_screen_finish";

    //分享 计数 type:1）weixin，2）weixin_circle,3）qq,4）qq_zone,5）weibo
    public static final String CLICK_SCREEN_SHARE = "click_screen_share";
    /*截屏统计时间end*/

    /*升级检测*/
    public static final String CLICK_UPDATE_FREE = "click_update_free";
    public static final String CLICK_UPDATE_FORCED = "click_update_forced";

    //应用圈app安装按钮点击事件 参数 status:0 安装， 1 打开；
    public static final String CLICK_APP_INSTALL = "click_app_install";

    //解析库下载更新请求
    public static final String EXTRACTOR_LIB_REQUEST = "extractor_lib_request";
    //解析库下载更新完成
    public static final String EXTRACTOR_LIB_DOWNLOADED = "extractor_lib_downloaded";

    //连接页面打开次数统计
    public static final String DEVICE_CONNECT_PAGE_COUNT = "device_connect_page_count";
    //连接命令发送次数统计
    public static final String DEVICE_CONNECT_CMD_COUNT = "device_connect_cmd_count";

    //我的_百度网盘_点击
    public static final String BAIDU_NET_DISK_CLICK = "baidu_net_disk_click";
    //我的_百度网盘_推送_点击
    public static final String BAIDU_NET_DISK_PUSH_CLICK = "baidu_net_disk_push_click";
    //文章_点击
    public static final String ARTICLE_CLICK = "article_click";
    //文章_相关影片_点击
    public static final String ARTICLE_RELATE_VIDEO_CLICK = "article_relate_video_click";

    //直播推送事件 参数success 0 成功，1 失败
    public static final String LIVE_PROGRAM_PUSH = "live_program_push";
    //连接设备底下的手动添加点击事件统计
    public static final String CLICK_ADD_CONNECT = "click_add_connect";
    //扫码和激活id连接设备成功与否事件统计
    public static final String WEB_CONNECT_TV_RESULT = "web_connect_tv_result";


    //云信视频电话
    //视频通话入口点击
    public static final String YX_VIDEO_CALL_CLICK = "video_call_click";
    //登录结果
    public static final String YX_LOG_ON_RESULT = "log_on_result";
    //未添加联系人页面按钮点击
    public static final String YX_NULL_CONTACT_BUTTON_CLICK = "null_contact_button_click";
    //扫码添加联系人结果
    public static final String YX_SCAN_ADD_CONTACT_RESULT = "scan_add_contact_result";
    //手动添加联系人结果
    public static final String YX_MANUAL_ADD_CONTACT_RESULT = "manual_add_contact_result";
    //联系人数量
    public static final String YX_CONTACT_NUMBER = "contact_number";
    //联系人页面按钮点击
    public static final String YX_CONTACT_BUTTON_CLICK = "contact_button_click";
    //联系人详情页面按钮点击
    public static final String YX_CONTACT_DETAILS_BUTTON_CLICK = "contact_details_button_click";
    //弹窗按钮点击
    public static final String YX_POPUP_BUTTON_CLICK = "popup_button_click";
    //接听页面按钮点击
    public static final String YX_ANSWER_CALL_BUTTON_CLICK = "answer_call_button_click";
    //接听结果
    public static final String YX_ANSWER_CALL_RESULT = "answer_call_result";
    //呼出页面按钮点击
    public static final String YX_OUTBOUND_CALL_BUTTON_CLICK = "outbound_call_button_click";
    //呼出结果
    public static final String YX_OUTBOUND_CALL_RESULT = "outbound_call_result";
    //通话中按钮点击
    public static final String YX_VIDEO_CALLING_BUTTON_CLICK = "video_calling_button_click";

}
