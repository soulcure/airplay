package swaiotos.runtime.h5.gameengine;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.gameengine.expiringmap.ExpirationPolicy;
import swaiotos.runtime.h5.gameengine.expiringmap.ExpiringMap;

/**
 *
 * 本地的按键值，需要分发给到本地的h5应用
 * Author:rico
 * Email:lvweilong@cocoaa.com
 *
 */
public class IotChannelKeyBoard {

    Context mContext;
    ChannelKeyBoardListener mListener;
    static IotChannelKeyBoard instance;
    ExpiringMap<IotKeyCodeMapKey, IotKeyCode> keyCodeExpireMap;

    FixAtomicInteger fixAtomicInteger;
    /**
     * 采用自动过期清理的map用来存储全部的按键值
     * 游戏engine在读取按键值的时候，从map中获得
     */
    private IotChannelKeyBoard(){
        // maxSize: 设置最大值,添加第11个entry时，会导致第1个立马过期(即使没到过期时间)
        // expiration：设置每个key有效时间10s, 如果key不设置过期时间，key永久有效。
        // variableExpiration: 允许更新过期时间值,如果不设置variableExpiration，不允许后面更改过期时间,一旦执行更改过期时间操作会抛异常UnsupportedOperationException
        // policy:
        //        CREATED: 只在put和replace方法清零过期时间
        //        ACCESSED: 在CREATED策略基础上增加, 在还没过期时get方法清零过期时间。
        //        清零过期时间也就是重置过期时间，重新计算过期时间.
        keyCodeExpireMap = ExpiringMap.builder()
                .maxSize(512)
                .expiration(3, TimeUnit.SECONDS)
                .expirationPolicy(ExpirationPolicy.ACCESSED).build();

        fixAtomicInteger = new FixAtomicInteger();
    }

    public void init(Context context,ChannelKeyBoardListener listener){
        mContext = context;
        mListener = listener;
    }

    public void cleanUp(){
        mListener = null;
    }

    public static IotChannelKeyBoard getSingleton() {
        if (instance == null) {
            synchronized (H5ChannelInstance.class) {
                if (instance == null) {
                    instance = new IotChannelKeyBoard();
                }
            }
        }
        return instance;
    }

    /***
     * 将键值输入buffer，如果h5有游戏或者应用注册键值监听，则进入到h5应用/游戏的按键buffer队列，
     * Native的按键值通过bufferKeyEvent的方式注入
     *
     * @param remoteUserID 代表client的唯一标识（多人同时发送/接收key）
     * @param keyAction 代表client的按键的状态是按下或者放开
     * @param keyCode 代表要buffer的键值
     */
    public void bufferKeyEvent(String remoteUserID, IotKeyCode.KEY_ACTION_ENUM keyAction, IotKeyCode.KEYCODE_CODE_ENUM keyCode){
        if(mListener !=null){
            int currKeycodeInteger = fixAtomicInteger.incrementAndGet();
            IotKeyCode userKeyCode = new IotKeyCode.Builder().identify(remoteUserID).keyCodeID(currKeycodeInteger).keyCode(keyCode).keyAction(keyAction).build();
            IotKeyCodeMapKey mapKey = new IotKeyCodeMapKey(currKeycodeInteger,remoteUserID);
            keyCodeExpireMap.put(mapKey,userKeyCode);
        }else{
            LogUtil.w("bufferKeyEvent but have not listener");
        }
    }

    /**
     * 通过id获得keycode
     * 每一帧游戏循环可以通过调用改方法获得键值
     *
     * @param identify 按键值唯一标识
     * @return
     */
    public IotKeyCode getKeyCodeByIdentify(String identify){
        if(mListener!=null){
            for(IotKeyCodeMapKey key:keyCodeExpireMap.keySet()){
                if(key.identify.equals(identify)){
                    IotKeyCode keycode =keyCodeExpireMap.remove(key);
                    return keycode;
                }
            }
        }else{
            LogUtil.w("getKeyCodeByIdentify but have not listener");
        }

        return null;
    }

    /**
     * 通过ids获得所有的keycode
     * 每一帧游戏循环可以通过调用改方法获得键值
     *
     * @param identifyList 按键值唯一标识列表
     * @return
     */
    public List<IotKeyCode> getKeyCodeByIdentifies(List<String> identifyList){
        if(mListener!=null){
            List<IotKeyCode> keyCodes = new ArrayList<>();
            for (String identify:identifyList){
                for(IotKeyCodeMapKey key:keyCodeExpireMap.keySet()){
                    if(key.identify.equals(identify)){
                        IotKeyCode keycode =keyCodeExpireMap.remove(key);
                        keyCodes.add(keycode);
                    }
                }
            }

            if(keyCodes.size()>0){
                return keyCodes;
            }
        }else{
            LogUtil.w("getKeyCodeByIdentifies but have not listener");
        }

        return null;
    }
}
