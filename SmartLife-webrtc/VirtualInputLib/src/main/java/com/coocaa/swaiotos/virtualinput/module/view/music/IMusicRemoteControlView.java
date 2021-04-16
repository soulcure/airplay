package com.coocaa.swaiotos.virtualinput.module.view.music;

import android.view.View;

import com.coocaa.swaiotos.virtualinput.iot.MediaState;
import com.coocaa.swaiotos.virtualinput.iot.MusicState;

/**
 * @ClassName: IMusicRemoteControlView
 * @Author: AwenZeng
 * @CreateDate: 2020/10/17 14:43
 * @Description:
 */
public interface IMusicRemoteControlView {

    View getView();
    void setClientId(String id);
    void refreshUI(MediaState musicState);
    void refreshMusicProgress();

}
