package com.coocaa.swaiotos.virtualinput.statemachine;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.coocaa.swaiotos.virtualinput.VirtualInputTypeDefine;
import com.coocaa.swaiotos.virtualinput.iot.State;
import com.coocaa.swaiotos.virtualinput.utils.EmptyUtils;

import java.util.Iterator;
import java.util.Map;


/**
 * @Author: yuzhan
 */
public class SceneMachineManager {

    private SceneMachine sceneMachine = new SceneMachine();
    private SceneMachine defaultSceneMachine = new SceneMachine();
    private StateMachineConfig config;


    private Map<String, Integer> smMap = new ArrayMap<>();
    private SparseArray<String> typeMap = new SparseArray<>();
    private SparseArray<String> sceneMap = new SparseArray<>();

    private final static String TAG = "SmartVI";

    private final static SceneMachineManager instance = new SceneMachineManager();

    public final static SceneMachineManager getInstance() {return instance;}

    private SceneMachineManager() {
        config = new StateMachineConfig();
        if(config.getConfig() != null) {
            smMap.putAll(config.getConfig());
        }
        if(config.getTypeName() != null) {
            Iterator<Map.Entry<Integer, String>> iterator = config.getTypeName().entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Integer, String> entry = iterator.next();
                typeMap.put(entry.getKey(), entry.getValue());
            }
        }
        Iterator<Map.Entry<String, Integer>> iterator = smMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            sceneMap.put(entry.getValue(), entry.getKey());
        }

        Log.d(TAG, "init scene-type config : " + smMap);
        Log.d(TAG, "init type-scene config : " + sceneMap);
        Log.d(TAG, "init typeMap : " + typeMap);
    }

//    public boolean notifySceneChanged(String stateString) {
//        if(TextUtils.isEmpty(stateString)) {
//            return false;
//        }
//        try {
//            State state = State.decode(stateString);
//            AppState appState = new AppState(state);
//            Log.d(TAG, "appState=" + appState);
//            String key = getKey(appState.getPkgName(), appState.getClassName());
//            int lastSceneType = sceneMachine.sceneType;
//            Log.d(TAG, "lastScene=" + sceneMachine.toString());
//            if(smMap.get(key) != null) {
//                sceneMachine.sceneName = key;
//                sceneMachine.clienId = appState.getClientID();
//                sceneMachine.sceneType = smMap.get(key);
//                sceneMachine.typeName = typeMap.get(sceneMachine.sceneType);
//                Log.d(TAG, "new app state : " + sceneMachine.toString());
//            } else {
//                Log.d(TAG, "unknown state type : sceneType=" + key);
//                sceneMachine.refresh(defaultSceneMachine);
//            }
//            return lastSceneType != sceneMachine.sceneType; //scene changed.
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /**
     * 通知场景变化
     * @param state 根据状态state切换
     * @return
     */
    public boolean notifySceneChanged(String stateString) {
        try {
            State state = State.decode(stateString);
            int lastSceneType = sceneMachine.sceneType;
            Log.d(TAG, "lastScene=" + sceneMachine.toString());
            if(EmptyUtils.isNotEmpty(state.getType())) {
                sceneMachine.sceneName = state.getType();
                sceneMachine.clienId = "";
                sceneMachine.sceneType = getStateType(state.getType().toUpperCase());
                sceneMachine.typeName = state.getType();
                Log.d(TAG, "new app state : " + sceneMachine.toString());
            } else {
                Log.d(TAG, "unknown state type : sceneType=" + state.getType());
                sceneMachine.refresh(defaultSceneMachine);
            }
            return lastSceneType != sceneMachine.sceneType; //scene changed.
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public SceneMachine getSceneMachine() {
        return sceneMachine;
    }

    private String getKey(String pkg, String cls) {
        StringBuilder sb = new StringBuilder();
        if(!TextUtils.isEmpty(pkg)) {
            sb.append(pkg);
        }
        sb.append("/");
        if(!TextUtils.isEmpty(cls)) {
            sb.append(cls);
        }
        return StateMachineDefine.fromApp(sb.toString());
    }

    /**
     * 获取StateType类型
     * @param typeName
     * @return
     */
    public static int getStateType(String typeName){
        if(EmptyUtils.isEmpty(typeName)){
            return VirtualInputTypeDefine.TYPE_DEFAULT;
        }
        switch (typeName){
            case VirtualInputTypeDefine.NAME_DEFAULT:
                return VirtualInputTypeDefine.TYPE_DEFAULT;
            case VirtualInputTypeDefine.NAME_VIDEO:
                return VirtualInputTypeDefine.TYPE_LOCAL_VIDEO;
            case VirtualInputTypeDefine.NAME_IMAGE:
                return VirtualInputTypeDefine.TYPE_PICTURE;
            case VirtualInputTypeDefine.NAME_MUSIC:
                return VirtualInputTypeDefine.TYPE_MUSIC;
            case VirtualInputTypeDefine.NAME_DOCUMENT:
                return VirtualInputTypeDefine.TYPE_DOCUMENT;
            case VirtualInputTypeDefine.NAME_LIVE:
                return VirtualInputTypeDefine.TYPE_LIVE;
            case VirtualInputTypeDefine.NAME_H5_ATMOSPHERE:
                return VirtualInputTypeDefine.TYPE_H5_ATMOSPHERE;
            case VirtualInputTypeDefine.NAME_H5_GAME:
                return VirtualInputTypeDefine.TYPE_H5_GAME;
        }
        return VirtualInputTypeDefine.TYPE_DEFAULT;
    }
}
