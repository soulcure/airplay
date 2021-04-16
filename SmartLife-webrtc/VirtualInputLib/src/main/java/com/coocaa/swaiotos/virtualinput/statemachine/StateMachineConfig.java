package com.coocaa.swaiotos.virtualinput.statemachine;

import com.coocaa.swaiotos.virtualinput.VirtualInputTypeDefine;

import java.util.HashMap;
import java.util.Map;

import static com.coocaa.swaiotos.virtualinput.statemachine.StateMachineDefine.fromApp;

/**
 * @Author: yuzhan
 */
public class StateMachineConfig {

    Map<String, Integer> configMap = new HashMap<>();
    Map<Integer, String> typeNameMap = new HashMap<>();


    public StateMachineConfig() {
        init();
    }

    private void init() {
        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.video.VideoPlayerActivity"), VirtualInputTypeDefine.TYPE_LOCAL_VIDEO);

        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.video.page.VideoPlayerActivity"), VirtualInputTypeDefine.TYPE_LOCAL_VIDEO);

        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.image.page.ImageActivity"), VirtualInputTypeDefine.TYPE_PICTURE);

        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.audio.page.AudioPlayerActivity"), VirtualInputTypeDefine.TYPE_MUSIC);

        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.document.page.DocumentPlayerActivity"), VirtualInputTypeDefine.TYPE_DOCUMENT);

        configMap.put(fromApp("com.swaiotos.universalmediaplayerdemo/com.swaiotos.universalmediaplayer.video.VideoPlayerActivity"), VirtualInputTypeDefine.TYPE_LOCAL_VIDEO);
        configMap.put(fromApp("com.swaiotos.universalmediaplayerdemo/com.swaiotos.universalmediaplayer.video.page.VideoPlayerActivity"), VirtualInputTypeDefine.TYPE_LOCAL_VIDEO);
        configMap.put(fromApp("com.swaiotos.universalmediaplayerdemo/com.swaiotos.universalmediaplayer.image.page.ImageActivity"), VirtualInputTypeDefine.TYPE_PICTURE);
        configMap.put(fromApp("com.swaiotos.universalmediaplayerdemo/com.swaiotos.universalmediaplayer.audio.page.AudioPlayerActivity"), VirtualInputTypeDefine.TYPE_MUSIC);
        configMap.put(fromApp("com.swaiotos.universalmediaplayerdemo/com.swaiotos.universalmediaplayer.document.page.DocumentPlayerActivity"), VirtualInputTypeDefine.TYPE_DOCUMENT);

//        configMap.put(fromApp("swaiotos.runtime.h5.app/swaiotos.runtime.h5.app.H5TVAppletActivity"), VirtualInputTypeDefine.TYPE_H5_ATMOSPHERE);

        typeNameMap.put(VirtualInputTypeDefine.TYPE_LOCAL_VIDEO, VirtualInputTypeDefine.NAME_VIDEO);
        typeNameMap.put(VirtualInputTypeDefine.TYPE_PICTURE, VirtualInputTypeDefine.NAME_IMAGE);
        typeNameMap.put(VirtualInputTypeDefine.TYPE_MUSIC, VirtualInputTypeDefine.NAME_MUSIC);
        typeNameMap.put(VirtualInputTypeDefine.TYPE_DOCUMENT, VirtualInputTypeDefine.NAME_DOCUMENT);
//        typeNameMap.put(VirtualInputTypeDefine.TYPE_H5_ATMOSPHERE, VirtualInputTypeDefine.NAME_H5_ATMOSPHERE);
        configMap.put(fromApp("swaiotos.channel.iot/com.swaiotos.universalmediaplayer.live.page.LiveActivity"), VirtualInputTypeDefine.TYPE_LIVE);
        typeNameMap.put(VirtualInputTypeDefine.TYPE_LIVE, VirtualInputTypeDefine.NAME_LIVE);
    }

    public Map<String, Integer> getConfig() {
        return configMap;
    }

    public Map<Integer, String> getTypeName() {
        return typeNameMap;
    }
}
