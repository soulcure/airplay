package com.coocaa.tvpi.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.data.channel.DeviceParams;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.tvpi.module.login.SmsLoginActivity;
import com.example.sanyansdk.SanYanManager;
import com.coocaa.smartscreen.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import me.jessyan.retrofiturlmanager.RetrofitUrlManager;
import me.jessyan.retrofiturlmanager.onUrlChangeListener;
import okhttp3.HttpUrl;

/**
 * @ClassName com.coocaa.tvpi.test.TestActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/3/28
 * @Version TODO (write something)
 */
public class TestActivity extends BaseActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        img = findViewById(R.id.img);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        RetrofitUrlManager.getInstance().registerUrlChangeListener(new ChangeListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GlideApp.with(TestActivity.this)
                        .load(url)
                        .skipMemoryCache(true) // 不使用内存缓存
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                        .centerCrop()
                        .into(img);
            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.up:
                CmdUtil.sendKey(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case R.id.down:
                CmdUtil.sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case R.id.left:
                CmdUtil.sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
                break;
            case R.id.right:
                CmdUtil.sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;
            case R.id.center:
                CmdUtil.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
                break;
            case R.id.home:
                CmdUtil.sendKey(KeyEvent.KEYCODE_HOME);
                break;
            case R.id.back:
                CmdUtil.sendKey(KeyEvent.KEYCODE_BACK);
                break;
            case R.id.settings:
                CmdUtil.sendKey(KeyEvent.KEYCODE_MENU);
                break;
            case R.id.volume_up:
                CmdUtil.sendKey(KeyEvent.KEYCODE_VOLUME_UP);
                break;
            case R.id.volume_down:
                CmdUtil.sendKey(KeyEvent.KEYCODE_VOLUME_DOWN);
                break;
            case R.id.mute:
                CmdUtil.sendKey(KeyEvent.KEYCODE_VOLUME_MUTE);
                break;
            case R.id.power:
                CmdUtil.sendKey(KeyEvent.KEYCODE_POWER);
                break;

            case R.id.screen_shot:
                CmdUtil.sendScreenshot();
                break;

            case R.id.live_video:
                CmdUtil.pushLiveVideo("62", "1780");
                break;
            case R.id.one_key_clear:
                CmdUtil.onKeyClear();
                break;
            case R.id.screensaver:
//                SSConnectManager.getInstance().customScreensaver();
                break;

            case R.id.connect_tv:
                CmdUtil.sendDeviceCmd(DeviceParams.CMD.CONNECT.toString());
                break;
            case R.id.disconnect_tv:
                CmdUtil.sendDeviceCmd(DeviceParams.CMD.DISCONNECT.toString());
                break;
            case R.id.device_info:
                CmdUtil.sendDeviceCmd(DeviceParams.CMD.DEVICE_INTO.toString());
                break;

            case R.id.get_video_source:
//                SSConnectManager.getInstance().sendVideoCmd(PlayParams.CMD.GET_SOURCE.toString());
                break;
            case R.id.play_video:
//                SSConnectManager.getInstance().sendVideoCmd(PlayParams.CMD.ONLINE_VIDEO.toString());
                break;

            case R.id.start_app_test_activity:
                startActivity(new Intent(this, AppTestActivity.class));
                break;
            case R.id.clean_SanYanToken:
                SpUtil.putString(getApplicationContext(), sanyanKey,null);
                ToastUtils.getInstance().showGlobalShort("清除闪验Token成功");
//                LoginUtils.getInstance().getUserInfo2("");
                break;
            case R.id.ones_login:
                //创蓝闪验登录测试代码
                String sanyanToken = SpUtil.getString(this, sanyanKey, null);
                if (TextUtils.isEmpty(sanyanToken)) {
                    SanYanManager.getInstance().openLoginAuth(new SanYanManager.LoginAuthResult() {
                        @Override
                        public void getOpenLoginAuthStatus(int code, String result) {
                            if (1000 == code) {
                                //拉起授权页成功
                                Log.e("VVV", "拉起授权页成功： _code==" + code + "   _result==" + result);
                            } else {
                                //拉起授权页失败
                                Log.e("VVV", "拉起授权页失败： _code==" + code + "   _result==" + result);
                            }
                        }

                        @Override
                        public void getOneKeyLoginStatus(int code, String result) {
                            if (1011 == code) {
                                Log.e("VVV", "用户点击授权页返回： _code==" + code + "   _result==" + result);
                                return;
                            } else if (1000 == code) {
                                Log.e("VVV", "用户点击登录获取token成功： _code==" + code + "   _result==" + result);
                                //OneKeyLoginManager.getInstance().setLoadingVisibility(false);
                                //AbScreenUtils.showToast(getApplicationContext(), "用户点击登录获取token成功");
                                SanYanManager.getInstance().finishLoginAuth();
                                try {
                                    JSONObject object = new JSONObject(result);
                                    String token = object.getString("token");
                                    SpUtil.putString(getApplicationContext(), sanyanKey,token);
                                    getServerToken(token);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e("VVV", "用户点击登录获取token失败： _code==" + code + "   _result==" + result);
                            }
                        }

                        @Override
                        public void onCustomClick(Context context, View view) {
                            ToastUtils.getInstance().showGlobalShort("其他手机号码登录="+view.getId());
                            SmsLoginActivity.start(TestActivity.this, false);
                        }
                    },getApplicationContext());
                }else{
                    ToastUtils.getInstance().showGlobalShort("已获取闪验token，正在交换服务器token");
                    getServerToken(sanyanToken);
                }


                break;

        }
    }
    final String sanyanKey = "SanYanToken";
    private void getServerToken(String SanYanToken) {
        HashMap<String, String> map = new HashMap<>();
        map.put("token", SanYanToken);
        map.put("systemType", "1");
        HashMap<String, String> pubMap = ParamsUtil.getCoocaaAccountPublicMap(map,null);

        Log.e("VVV", "map==" + map);
        Log.e("VVV", "pubMap==" + pubMap);
//        NetWorkManager.getInstance()
//                .getCoocaaAccountApiService()
//                .oneClickLogin(pubMap)
//                .compose(ResponseTransformer.<LoginInfo>handleResult())
//                .subscribe(new DefaultObserver<LoginInfo>() {
//                    @Override
//                    public void onNext(LoginInfo loginInfo) {
//                        Log.e("VVV", "loginInfo" + loginInfo.access_token);
//                        getUserInfo2(loginInfo.access_token);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.e("VVV", "onError=" + e.getMessage());
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        Log.e("VVV", "onComplete");
//                    }
//                });
    }

    private void getUserInfo2(final String access_token){
        HashMap<String, String> map = new HashMap<>();
        map.put("access_token",access_token);
        /*NetWorkManager.getInstance()
                .getCoocaaAccountApiService()
                .getUserInfo2(map)
                .compose(ResponseTransformer.handleFreeResult())
                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onNext(String info) {
                        Log.e("VVV", "getUserInfo2" + info);
                        //将access_token存入UserCore公共库中
                        LoginResultData loginResultData = new LoginResultData();
                        loginResultData.data = new LoginData();
                        loginResultData.data.access_token = access_token;
                        loginResultData.data.account = info;
                        boolean result =ProviderUtils.INSTANCE.syncLoginData(loginResultData);
                        Log.e("VVV", "result=" + result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "getUserInfo2 onError: " + e.toString());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "getUserInfo2 onComplete: ");
                    }
                });*/
    }

    private class ChangeListener implements onUrlChangeListener {

        @Override
        public void onUrlChangeBefore(HttpUrl oldUrl, String domainName) {
            Log.d(TAG, String.format("The oldUrl is <%s>, ready fetch <%s> from DomainNameHub",
                    oldUrl.toString(),
                    domainName));
        }

        @Override
        public void onUrlChanged(final HttpUrl newUrl, HttpUrl oldUrl) {
            Observable.just(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Object>() {
                        @Override
                        public void accept(Object o) throws Exception {
                            Log.d(TAG, "The newUrl is { " + newUrl.toString() + " }");
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    }


}
