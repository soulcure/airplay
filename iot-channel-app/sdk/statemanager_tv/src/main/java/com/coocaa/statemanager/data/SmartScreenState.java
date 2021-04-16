package com.coocaa.statemanager.data;

import android.util.Log;

import java.io.Serializable;


public class SmartScreenState implements Serializable {
    private String pkgName ="";
    private String className = "";
    private SCREEN_TYPE type = SCREEN_TYPE.IDLE;

    public static SmartScreenState Instance = new SmartScreenState();

    public enum SCREEN_TYPE{
        IDLE,
        ONREADY,
        ONSTARTING,
        ONEXIST
    }

    public synchronized void updataState(String pkg,String cName,SCREEN_TYPE stype){
       pkgName = pkg;
       className = cName;
       type = stype;
        Log.d("state","updataState  pkg:"+pkg+ "  className:"+className+ "  stype:"+stype);
    }

    public void setScreenType(SCREEN_TYPE stype){
        type = stype;
    }

    public SCREEN_TYPE getScreenType(){
        return type;
    }

    public String getPkgName(){
        return pkgName;
    }

    public String getClassName(){
        return className;
    }

}
