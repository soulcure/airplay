package com.coocaa.swaiotos.virtualinput.iot;

import android.text.TextUtils;

import com.umeng.commonsdk.debug.E;

/**
 * @ClassName: MediaState
 * @Author: AwenZeng
 * @CreateDate: 2020/10/17 17:51
 * @Description:
 */
public class MediaState extends State{
    /**
     * type : video/audio/image
     * uri : http://172.20.148.236:37019/e261b2d0ae498e9921bbf805783893a9
     * playCmd : play/pause/stop
     * currentVolume : 100
     * mediaCurrent : 100010
     * mediaTitle : title
     * mediaTime : 10020
     * rotate : 20
     */
    private String type;
    private String uri;
    private String playCmd;
    private int currentVolume;
    private long mediaCurrent;
    private String mediaTitle;
    private long mediaTime;
    private int rotate;

    public MediaState(State state) {
        super(state);
        try{
            type = state.getValues().get("type");
            uri = state.getValues().get("uri");
            playCmd = state.getValues().get("playCmd");
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            if(!TextUtils.isEmpty(state.getValues().get("mediaCurrent")) && !TextUtils.equals("null", state.getValues().get("mediaCurrent"))) {
                mediaCurrent = Long.parseLong(state.getValues().get("mediaCurrent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(!TextUtils.isEmpty(state.getValues().get("mediaTitle")) && !TextUtils.equals("null", state.getValues().get("mediaTitle"))) {
                mediaTitle = state.getValues().get("mediaTitle");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(!TextUtils.isEmpty(state.getValues().get("mediaTime")) && !TextUtils.equals("null", state.getValues().get("mediaTime"))) {
                mediaTime = Integer.parseInt(state.getValues().get("mediaTime"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(!TextUtils.isEmpty(state.getValues().get("currentVolume")) && !TextUtils.equals("null", state.getValues().get("currentVolume"))) {
                currentVolume = (int) Float.parseFloat(state.getValues().get("currentVolume"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(!TextUtils.isEmpty(state.getValues().get("rotate")) && !TextUtils.equals("null", state.getValues().get("rotate"))) {
                rotate = Integer.parseInt(state.getValues().get("rotate"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPlayCmd() {
        return playCmd;
    }

    public void setPlayCmd(String playCmd) {
        this.playCmd = playCmd;
    }

    public int getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(int currentVolume) {
        this.currentVolume = currentVolume;
    }

    public long getMediaCurrent() {
        return mediaCurrent;
    }

    public void setMediaCurrent(long mediaCurrent) {
        this.mediaCurrent = mediaCurrent;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public long getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(long mediaTime) {
        this.mediaTime = mediaTime;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }
}
