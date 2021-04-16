package swaiotos.runtime.h5.common.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class OnGameEngineInfo {
    public String type;

    public String engineEvent;
    public String mobileGameUrl;
    public String tvGameUrl;

    public String userID;
    public String userNick;
    public String userAvatar;
    public String userMobile;

    public int keyAction;
    public int keyCode;
    public int keyCodeID;

    public String extra;

    public OnGameEngineInfo(){

    }

    public OnGameEngineInfo(String engineEvent,String mobileGameUrl,String tvGameUrl){
        this.type = "game_engine";
        this.engineEvent = engineEvent;
        this.mobileGameUrl = mobileGameUrl;
        this.tvGameUrl = tvGameUrl;
    }

    public OnGameEngineInfo(String engineEvent,String mobileGameUrl,String tvGameUrl,int keyAction,int keyCodeID,int keyCode){
        this.type = "game_engine";
        this.engineEvent = engineEvent;
        this.mobileGameUrl = mobileGameUrl;
        this.tvGameUrl = tvGameUrl;
        this.keyAction = keyAction;
        this.keyCodeID = keyCodeID;
        this.keyCode = keyCode;
    }

    public OnGameEngineInfo(String engineEvent,String mobileGameUrl,String tvGameUrl,String extra){
        this.type = "game_engine";
        this.engineEvent = engineEvent;
        this.mobileGameUrl = mobileGameUrl;
        this.tvGameUrl = tvGameUrl;
        this.extra = extra;
    }

    public OnGameEngineInfo(String engineEvent,String mobileGameUrl,String tvGameUrl,String userID,String userNick,String userAvatar,String userMobile,int keyAction,int keyCodeID,int keyCode){
        this.type = "game_engine";
        this.engineEvent = engineEvent;
        this.mobileGameUrl = mobileGameUrl;
        this.tvGameUrl = tvGameUrl;

        this.userID = userID;
        this.userNick = userNick;
        this.userAvatar = userAvatar;
        this.userMobile = userMobile;

        this.keyAction = keyAction;
        this.keyCodeID = keyCodeID;
        this.keyCode = keyCode;
    }

    @Override
    public String toString() {
        return "OnGameEngineInfo{" +
                ", type='" + type + '\'' +
                ", engineEvent='" + engineEvent + '\'' +
                ", mobileGameUrl='" + mobileGameUrl + '\'' +
                ", tvGameUrl='" + tvGameUrl + '\'' +
                ", userID='" + userID + '\'' +
                ", userNick='" + userNick + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", userMobile='" + userMobile + '\'' +
                ", keyAction=" + keyAction +
                ", keyCode=" + keyCode +
                ", keyCodeID=" + keyCodeID +
                ", extra='" + extra + '\'' +
                '}';
    }

    public static String toJSONString(OnGameEngineInfo gameInfo){
        return JSON.toJSONString(gameInfo);
    }

    public static OnGameEngineInfo fromJSONString(String gameInfoString){
        OnGameEngineInfo gameEngineInfo = (OnGameEngineInfo) JSONObject.parseObject(gameInfoString,OnGameEngineInfo.class);
        return gameEngineInfo;
    }
}
