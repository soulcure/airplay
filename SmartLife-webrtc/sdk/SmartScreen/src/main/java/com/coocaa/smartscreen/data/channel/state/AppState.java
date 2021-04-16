package com.coocaa.smartscreen.data.channel.state;

/**
 * @ClassName: MusicState
 * @Author: lu
 * @CreateDate: 2020/10/15 9:27 PM
 * @Description:
 */
public class AppState extends State {
    public static final String TYPE = "app";
    public static final int VERSION = 1;

    public AppState() {
        super(TYPE, VERSION);
    }

    public AppState(State state) {
        super(state);
    }

    public void setPkgName(String pkgName) {
        put("pkgName", pkgName);
    }

    public String getPkgName() {
        return get("pkgName");
    }

    public void setClassName(String className) {
        put("className", className);
    }

    public String getClassName() {
        return get("className");
    }

    public void  setClientID(String clientID){put("clientID", clientID);}

    public String getClientID(){return get("clientID");}

}
