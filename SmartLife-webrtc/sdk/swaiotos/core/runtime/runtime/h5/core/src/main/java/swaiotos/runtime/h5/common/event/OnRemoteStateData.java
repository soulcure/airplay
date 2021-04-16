package swaiotos.runtime.h5.common.event;

public class OnRemoteStateData {

    public String event;

    public String _playCmd;
    public String _type;
    public String _param;

    public OnRemoteStateData(){

    }

    public OnRemoteStateData(String event,
                             String playCmd,
                             String param,
                             String type){
        this.event = event;
        this._playCmd = playCmd;
        this._type = type;
        this._param = param;
    }

    @Override
    public String toString() {
        return "OnRemoteStateData{" +
                "event='" + event + '\'' +
                ", _playCmd='" + _playCmd + '\'' +
                "_type='" + _type + '\'' +
                ", _param='" + _param + '\'' +
                '}';
    }
}
