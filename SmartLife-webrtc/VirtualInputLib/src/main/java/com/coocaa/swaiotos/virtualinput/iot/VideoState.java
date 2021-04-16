package com.coocaa.swaiotos.virtualinput.iot;

/**
 * @ClassName: VideoState
 * @Author: lu
 * @CreateDate: 2020/10/15 9:23 PM
 * @Description:
 */
public class VideoState extends State {
    public static final String TYPE = "video";
    public static final int VERSION = 1;

    public enum STATE {
        LOADING,
        PLAYING,
        PAUSED,
        IDLE,
    }

    public VideoState() {
        super(TYPE, VERSION);
    }

    public VideoState(State state) {
        super(state);
    }

    public void setMediaName(String mediaName) {
        put("mediaName", mediaName);
    }

    public String getMediaName() {
        return get("mediaName");
    }

    public void setDuration(long duration) {
        put("duration", String.valueOf(duration));
    }

    public long getDuration() {
        return Long.valueOf(get("duration"));
    }

    public void setPosition(long position) {
        put("position", String.valueOf(position));
    }

    public long getPosition() {
        return Long.valueOf(get("position"));
    }

    public void setPlayerState(MusicState.STATE state) {
        put("state", state.name());
    }

    public MusicState.STATE getPlayerState() {
        return MusicState.STATE.valueOf(get("state"));
    }
}
