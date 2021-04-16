package com.coocaa.swaiotos.virtualinput.iot;

/**
 * @ClassName: MusicState
 * @Author: lu
 * @CreateDate: 2020/10/15 9:27 PM
 * @Description:
 */
public class MusicState extends State {
    public static final String TYPE = "music";
    public static final int VERSION = 1;

    public enum STATE {
        LOADING,
        PLAYING,
        PAUSED,
        IDLE,
    }

    public MusicState() {
        super(TYPE, VERSION);
    }

    public MusicState(State state) {
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

    public void setPlayerState(STATE state) {
        put("state", state.name());
    }

    public STATE getPlayerState() {
        return STATE.valueOf(get("state"));
    }
}
