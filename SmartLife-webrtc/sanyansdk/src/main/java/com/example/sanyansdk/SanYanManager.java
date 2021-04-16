package com.example.sanyansdk;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.chuanglan.shanyan_sdk.OneKeyLoginManager;
import com.chuanglan.shanyan_sdk.listener.ActionListener;
import com.chuanglan.shanyan_sdk.listener.GetPhoneInfoListener;
import com.chuanglan.shanyan_sdk.listener.InitListener;
import com.chuanglan.shanyan_sdk.listener.OneKeyLoginListener;
import com.chuanglan.shanyan_sdk.listener.OpenLoginAuthListener;
import com.example.sanyansdk.utils.ConfigUtils;

public class SanYanManager {
//    public static String APP_ID = "njV3atIS"; old 给智家用了
    public static String APP_ID = "A7chmFPr";//new 给智屏用

    private static volatile SanYanManager manager = null;

    public SanYanManager() {
    }

    public static SanYanManager getInstance() {
        if (manager == null) {
            synchronized (SanYanManager.class) {
                if (manager == null) {
                    manager = new SanYanManager();
                }
            }
        }
        return manager;
    }

    public void init(Context context) {
        //闪验SDK配置debug开关 （必须放在初始化之前，开启后可打印闪验SDK更加详细日志信息）
        OneKeyLoginManager.getInstance().setDebug(true);

        //闪验SDK初始化（建议放在Application的onCreate方法中执行）
        OneKeyLoginManager.getInstance().init(context, APP_ID, new InitListener() {
            @Override
            public void getInitStatus(int code, String result) {
                //闪验SDK初始化结果回调
                Log.e("VVV", "初始化： code==" + code + "   result==" + result);
            }
        });
        //预取手机号，解决第一次安装启动一键登录页面失败的问题
        OneKeyLoginManager.getInstance().getPhoneInfo(new GetPhoneInfoListener() {
            @Override
            public void getPhoneInfoStatus(int code, String result) {
                Log.e("VVV", "getPhoneInfo： code==" + code + "   result==" + result);
            }
        });
    }

    /**
     * 打开授权页
     */
    public void openLoginAuth(final LoginAuthResult authResult,Context context) {
        //自定义授权页
        OneKeyLoginManager.getInstance().setAuthThemeConfig(ConfigUtils.getCJSConfig(context,authResult),null);
        OneKeyLoginManager.getInstance().openLoginAuth(false, new OpenLoginAuthListener() {
            @Override
            public void getOpenLoginAuthStatus(int code, String result) {
                if(authResult!=null){
                    authResult.getOpenLoginAuthStatus(code,result);
                }
            }
        }, new OneKeyLoginListener() {
            @Override
            public void getOneKeyLoginStatus(int code, String result) {
                if(authResult!=null){
                    authResult.getOneKeyLoginStatus(code,result);
                }
            }
        });
    }

    public void setPrivacyCheckListener(final PrivacyCheckListener oneKeyLoginListener) {
        //自定义授权页
        OneKeyLoginManager.getInstance().setActionListener(new ActionListener() {
            /**
             * @param type type=1 ，隐私协议点击事件 type=2 ，checkbox点击事件 type=3 ，一键登录按钮点击事件
             * @param code type=1 ，隐私协议点击事件，code分为0,1,2,3（协议页序号） type=2 ，checkbox点击事件，code分为0,1（0为未选中，1为选中） type=3 ，一键登录点击事件，code分为0,1（0为协议未勾选时，1为协议勾选时）
             * @param operator 点击事件的详细信息
             */
            @Override
            public void ActionListner(int type, int code, String operator) {
                Log.d("SanYanManager", "ActionListner: type" + type);
                Log.d("SanYanManager", "ActionListner: code" + code);
                Log.d("SanYanManager", "ActionListner:operator " + operator);
                if(type == 3){
                    if(code == 0){
                        if(oneKeyLoginListener != null){
                            oneKeyLoginListener.isPrivacyCheckWhenOneKeyLogin(false);
                        }
                    }else if(code == 1){
                        if(oneKeyLoginListener != null){
                            oneKeyLoginListener.isPrivacyCheckWhenOneKeyLogin(true);
                        }
                    }
                }
            }
        });
    }

    public void setCheckboxValue(boolean isChecked){
        OneKeyLoginManager.getInstance().setCheckBoxValue(isChecked);
    }


    /**
     * 销毁授权页
     */
    public void finishLoginAuth() {
        OneKeyLoginManager.getInstance().finishAuthActivity();
    }

    public interface LoginAuthResult {
        void getOpenLoginAuthStatus(int code, String result);

        void getOneKeyLoginStatus(int code, String result);

        void onCustomClick(Context context, View view);
    }

    public interface PrivacyCheckListener {
        /**
         * 一键登录时是否勾选了同意协议
         * @param isChecked  协议勾选框是否选中
         */
        void isPrivacyCheckWhenOneKeyLogin(boolean isChecked);
    }
}
