package com.coocaa.publib.data.def;

public class SkyworthKeyMap
{
    public enum SkyworthKey{    
        /*
         * 普通遥控键值
         * 待机; 1; 2; 3; 4; 5; 6; 7; 8; 9; 0; 3D\<-; 交替; 频道+; 频道-; 
         * 音量+; 音量-; 静音; 信号源; 确定; 语音[长按确定键]; 上; 下; 
         * 左; 右; 返回; 菜单; 主页; 云分享; 导视; "红\电视广播"; "绿\声道"; 
         * "黄\信息"; "蓝\点播"; 图像模式; 声音模式; 显示模式; 屏显; 上一首; 
         * 下一首; 快退; 快进; 播放|暂停; 停止; 点歌系统; 关联; 预约; 喜爱; 
         * 音效控制; 功能; 原声; 录音; 已点歌曲; 优先; 删除; 评分显示; 数位键; 
         * 长按主页；长按返回；长按菜单；
         */
        SKY_KEY_POWER,
        SKY_KEY_1,
        SKY_KEY_2,
        SKY_KEY_3,
        SKY_KEY_4,
        SKY_KEY_5,
        SKY_KEY_6,
        SKY_KEY_7,
        SKY_KEY_8,
        SKY_KEY_9,
        SKY_KEY_0,
        SKY_KEY_3D_MODE,
        SKY_KEY_ALTERNATE,
        SKY_KEY_CHANNEL_UP,
        SKY_KEY_CHANNEL_DOWN,
        SKY_KEY_VOLUME_UP,
        SKY_KEY_VOLUME_DOWN,
        SKY_KEY_VOLUME_MUTE,
        SKY_KEY_TV_INPUT,
        SKY_KEY_CENTER,
        SKY_KEY_VOICE,
        SKY_KEY_UP,
        SKY_KEY_DOWN,
        SKY_KEY_LEFT,
        SKY_KEY_RIGHT,
        SKY_KEY_BACK,
        SKY_KEY_MENU,
        SKY_KEY_HOME,
        SKY_KEY_SHARE,
        SKY_KEY_ENTER_EPG,
        SKY_KEY_RED,
        SKY_KEY_GREEN,
        SKY_KEY_YELLOW,
        SKY_KEY_BLUE,
        SKY_KEY_IMAGE_MODE,
        SKY_KEY_SOUND_MODE,
        SKY_KEY_DISPLAY_MODE,
        SKY_KEY_SCREEN_DISPLAY,
        SKY_KEY_MEDIA_PREVIOUS,
        SKY_KEY_MEDIA_NEXT,
        SKY_KEY_MEDIA_REWIND,
        SKY_KEY_MEDIA_FAST_FORWARD,
        SKY_KEY_MEDIA_PLAY_PAUSE,
        SKY_KEY_MEDIA_STOP,
        SKY_KEY_MEDIA_SONG_SYSTEM,
        SKY_KEY_MEDIA_RELATIONSHIP,
        SKY_KEY_MEDIA_BOOKING,
        SKY_KEY_MEDIA_FAVORITES,
        SKY_KEY_MEDIA_AUDIO_CONTROL,
        SKY_KEY_MEDIA_FUNCTION,
        SKY_KEY_MEDIA_ORIGINAL_SOUNDTRACK,
        SKY_KEY_MEDIA_RECORD,
        SKY_KEY_MEDIA_SELECTED_SONGS,
        SKY_KEY_MEDIA_PRIORITY,
        SKY_KEY_MEDIA_DELETE,
        SKY_KEY_MEDIA_SCORE_DISPLAY,
        SKY_KEY_INPUT_NUMBER,

        /*
         * 工厂遥控键值
         * 工厂调试键; 调测恢复键; 通道+; 通道-; 退出工厂模式键; 总线键
         * 老化模式键; ADC校正键; 视频1通道键; RF-AGC键; 视频2通道键
         * 视频3通道键; S视频1通道键; 分量1通道键; 分量2通道键; VGA通道键
         * HDMI1通道键; HDMI2通道键; HDMI3通道键; 酷k调测键; Uplayer调测键
         * 网络调测键; 屏变调测键; 白平衡调整键; 单独听调测键; CA卡信息键
         * 电子码; 向上搜台; 向下搜台
         */
        SKY_KEY_FACTORY_FACTORY_MODE,
        SKY_KEY_FACTORY_RESET,
        SKY_KEY_FACTORY_SOURCE_ADD,
        SKY_KEY_FACTORY_SOURCE_REDUCE,
        SKY_KEY_FACTORY_OUTSET,
        SKY_KEY_FACTORY_BUS_OFF,
        SKY_KEY_FACTORY_AGING_MODE,
        SKY_KEY_FACTORY_AUTO_ADC,
        SKY_KEY_FACTORY_AV1,
        SKY_KEY_FACTORY_RF_AGC,
        SKY_KEY_FACTORY_AV2,
        SKY_KEY_FACTORY_AV3,
        SKY_KEY_FACTORY_S1,
        SKY_KEY_FACTORY_YUV1,
        SKY_KEY_FACTORY_YUV2,
        SKY_KEY_FACTORY_VGA,
        SKY_KEY_FACTORY_HDMI1,
        SKY_KEY_FACTORY_HDMI2,
        SKY_KEY_FACTORY_HDMI3,
        SKY_KEY_FACTORY_KALA_OK,
        SKY_KEY_FACTORY_UPLAYER,
        SKY_KEY_FACTORY_LAN,
        SKY_KEY_FACTORY_DREAM_PANEL,
        SKY_KEY_FACTORY_WHITE_BALANCE,
        SKY_KEY_FACTORY_ALONE_LISTEN,
        SKY_KEY_FACTORY_CA_CARD,
        SKY_KEY_FACTORY_BARCODE,
        SKY_KEY_FACTORY_SEARCH_UP,
        SKY_KEY_FACTORY_SEARCH_DOWN,
        /*
         * 键控板感应键值
         * 靠近键控板感应; 靠近键控板菜单键感应; 靠近键控板确定键感应
         * 靠近键控板返回键感应; 靠近键控板音量加感应; 靠近键控板音量减感应
         * 靠近键控板频道加感应; 靠近键控板频道减感应; 离开键控板感应
         */
        SKY_KEY_SENSE_ALL,
        SKY_KEY_SENSE_MENU,
        SKY_KEY_SENSE_CENTER,
        SKY_KEY_SENSE_BACK,
        SKY_KEY_SENSE_VOLUME_UP,
        SKY_KEY_SENSE_VOLUME_DOWN,
        SKY_KEY_SENSE_CHANNEL_UP,
        SKY_KEY_SENSE_CHANNEL_DOWN,
        SKY_KEY_SENSE_LEAVE,
        /*
         * 自定义虚拟键值
         * 鼠标左键；鼠标中键；鼠标右键; 图片放大; 图片缩小
         */
        SKY_KEY_MOUSE_OK,
        SKY_KEY_MOUSE_MIDDLE,
        SKY_KEY_MOUSE_BACK, 
        SKY_KEY_ZOOM_IN,
        SKY_KEY_ZOOM_OUT,
        
        // 2012-06-27 Gui Qingwen add for shuttle key
        SKY_KEY_SHUTTLE_LEFT_SPEED_1,
        SKY_KEY_SHUTTLE_LEFT_SPEED_2,
        SKY_KEY_SHUTTLE_LEFT_SPEED_3,
        SKY_KEY_SHUTTLE_LEFT_SPEED_4,
        SKY_KEY_SHUTTLE_LEFT_SPEED_5,
        SKY_KEY_SHUTTLE_RIGHT_SPEED_1,
        SKY_KEY_SHUTTLE_RIGHT_SPEED_2,
        SKY_KEY_SHUTTLE_RIGHT_SPEED_3,
        SKY_KEY_SHUTTLE_RIGHT_SPEED_4,
        SKY_KEY_SHUTTLE_RIGHT_SPEED_5,
        SKY_KEY_HOME_LONG,
        SKY_KEY_BACK_LONG,
        SKY_KEY_NOTIFICATION;
        public int key_value()
        {
            return this.ordinal() + 0x80000000;
        }
    }
    /**
     * 对应老的SkyworthSDK里面的键值，SKY_KEY_POWER在老的SkyworthSDK的SkyworthKey里面，对应位置是0；SKY_KEY_1对应5；这里面有的键值，老的里面没有的话，直接
     * 对应-1
     * 这部分代码应该是被废弃掉的。
     */
    public static int[] SkyOldKeyMap =
    {
    	0,//KEY_POWER
    	5,//KEY_1
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	27,//KEY_CONFIRM
    	-1,
    	25,//KEY_UP
    	29,//KEY_DOWN
    	26,//KEY_LEFT
    	28,//KEY_RIGHT
    	30,//KEY_BACK
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    	-1,
    };
}
