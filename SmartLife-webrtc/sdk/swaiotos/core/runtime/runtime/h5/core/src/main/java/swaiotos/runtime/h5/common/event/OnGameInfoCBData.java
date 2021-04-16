package swaiotos.runtime.h5.common.event;

public class OnGameInfoCBData {

    public String event;
    public OnGameEngineInfo gameInfo;

    public OnGameInfoCBData(String event,OnGameEngineInfo info){
        this.event = event;
        this.gameInfo = info;
    }

}
