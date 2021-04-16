package com.coocaa.tvpi.module.reversescreen.control;


/**
 * @ClassName: ReverseScreenKeyControl
 * @Author: XuZeXiao
 * @CreateDate: 2019-12-20 18:38
 * @Description:
 */
public class ReverseScreenKeyControl {
    public static final String KEY_BASE = "";
    public static final String KEY_POWER = KEY_BASE + "key_power";
    public static final String KEY_INPUT = KEY_BASE + "key_input";
    public static final String KEY_MENU = KEY_BASE + "key_menu";
    public static final String KEY_VOLUMEUP = KEY_BASE + "key_volumeup";
    public static final String KEY_VOLUMEDOWN = KEY_BASE + "key_volumedown";
    public static final String KEY_HOME = KEY_BASE + "key_home";
    public static final String KEY_UP = KEY_BASE + "key_up";
    public static final String KEY_DOWN = KEY_BASE + "key_down";
    public static final String KEY_LEFT = KEY_BASE + "key_left";
    public static final String KEY_RIGHT = KEY_BASE + "key_right";
    public static final String KEY_ENTER = KEY_BASE + "key_enter";
    public static final String KEY_MUTE = KEY_BASE + "key_mute";
    public static final String KEY_BACK = KEY_BASE + "key_back";

    public static void sendKey(String key) {
//        DeviceControllerManager.getInstance().sendRemoteControl(KeyData.getKey("RemoteKeyEvent", key));
    }
}
