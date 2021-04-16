package com.coocaa.tvpi.module.mine;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.voice.VoiceAdviceInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.VoiceControlRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.skyworth.ai.speech.svs.SVSSDKProxy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import swaiotos.channel.iot.ss.device.TVDeviceInfo;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * @author chenaojun
 */
public class VoiceControllerActivity extends BaseActivity {

    private static final String TAG = VoiceControllerActivity.class.getSimpleName();

    private final int STATE_ADVICE = 0x01;
    private final int STATE_LISTEN = 0x02;
    private final int STATE_LISTEN_ING = 0x03;
    private final int STATE_LISTEN_ERROR = 0x09;
    private final int STATE_SEND_ERROR = 0x04;
    private final int STATE_SEND_ING = 0x05;
    private final int STATE_SEND_SUCCESS = 0x06;
    private final int STATE_SEND_SUCCEED = 0x10;
    private final int LISTEN_CHECK = 0x07;
    private final int LISTEN_LIMIT = 0x08;
    private final int SEND_CHECK = 0x12;

    private View bgVoiceController;
    private ImageView imgExit;
    private LottieAnimationView lvVoiceControllerState;
    private LottieAnimationView lvVoiceListenerState;
    private TextView tvVoiceContent;
    private TextView tvSendState;
    private TextView tvControlState;
    private TextView tvContent1;
    private TextView tvContent2;
    private TextView tvContent3;
    private TextView tvAdviceTitle;

    private VoiceControllerActivity.ViewControlHanlder viewControlHanlder;
    private ObjectAnimator fadeIn;
    private SVSSDKProxy svssdkProxy;
    private int currentState;
    private boolean isSure = false;
    private boolean isSendSuccess = false;

    private SVSSDKProxy.ISVSSDKCallBack sVSSDKCallBack = new SVSSDKProxy.ISVSSDKCallBack() {
        //开始录音
        @Override
        public void onBeginningOfSpeech() {
            isSure = false;
            isSendSuccess = false;
            Log.d(TAG, "onBeginningOfSpeech: ");
        }

        //录音大小 db 只回调
        @Override
        public void onRmsChanged(float v) {
            Log.d(TAG, "onRmsChanged: " + v);
            if (currentState == STATE_LISTEN && v > 4 && viewControlHanlder != null) {
                viewControlHanlder.sendEmptyMessage(STATE_LISTEN_ING);
            }
        }

        //停止录音
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");
        }

        //语音识别局部识别结果
        @Override
        public void onPartialResults(String s) {
            Log.d(TAG, "onPartialResults: " + s);
            updateContent(s);

        }

        //语音识别最终识别结果
        @Override
        public void onResults(String s) {
            Log.d(TAG, "onResults: " + s);
            updateContent(s);
            isSure = true;
            if (!"我在，你说".equals(tvVoiceContent.getText()) && viewControlHanlder != null) {
                viewControlHanlder.sendEmptyMessage(STATE_SEND_ING);
            }

        }

        /**
         * 语音识别过程中的异常错误码
         * ISpeechRecognizer.ERROR_AUDIO 表示声音采集出错.
         * ISpeechRecognizer.ERROR_NETWORK_TIMEOUT 网络超时错误.1
         * ISpeechRecognizer.ERROR_NO_MATCH 没有识别到语音结果.
         */
        @Override
        public void onError(int i) {
            Log.d(TAG, "onError: " + i);
            if (currentState == STATE_LISTEN || currentState == STATE_LISTEN_ING) {
                viewControlHanlder.sendEmptyMessage(STATE_LISTEN_ERROR);
            }
        }

        /** push 语音识别结果到电视端的回调。
         *  true 表示 成功 push 到后台服务器，但是不能保证电视可以正常收到消息。
         *  false 表示 没有 push 到后台服务器。
         */
        @Override
        public void onPushResult(boolean isSucceed) {
            Log.d(TAG, "onPushResult: " + isSucceed);

            if (isSucceed) {
                viewControlHanlder.sendEmptyMessage(STATE_SEND_SUCCESS);
                isSendSuccess = true;
            } else {
                viewControlHanlder.sendEmptyMessage(STATE_SEND_ERROR);
            }
        }
    };

    private void updateContent(String s) {
        runOnUiThread(() -> {
            setMaxEcplise(tvVoiceContent, 2, s);
//                tvVoiceContent.setText(s);
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(com.coocaa.publib.R.anim.dialog_enter, com.coocaa.publib.R.anim.dialog_out);
        setContentView(R.layout.activity_voice_controller);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initVoice();
        initView();
        initListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (svssdkProxy != null) {
            svssdkProxy.stopListening();
            svssdkProxy.cancelListening();
            svssdkProxy = null;
        }
        if (viewControlHanlder != null) {
            viewControlHanlder.removeCallbacksAndMessages(null);
            viewControlHanlder = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        bgVoiceController.setVisibility(View.GONE);
        overridePendingTransition(com.coocaa.publib.R.anim.push_left_in, com.coocaa.publib.R.anim.dialog_out);
    }

    private void initVoice() {
        if (!isConnected()) {
            ToastUtils.getInstance().showGlobalLong("请连接设备");
            return;
        }
        TVDeviceInfo info = (TVDeviceInfo) SSConnectManager.getInstance().getDevice().getInfo();
        if (info.activeId == null) {
            ToastUtils.getInstance().showGlobalLong("请连接正确设备");
            return;
        }
        Log.e(TAG, "initVoice: " + info.toString());
        svssdkProxy = new SVSSDKProxy
                .Builder()
                .setContext(this)
                //被绑定电视的系统版本
                .setClientVersion(info.cTcVersion)
                //被绑定电视的mac地址
                .setMac(info.MAC)
                //被绑定电视的mac机芯
                .setModel(info.mChip)
                //被绑定电视的mac机型
                .setType(info.mModel)
                //被绑定电视的激活id，当激活id为空时，会出现异常。
                .setSn(info.activeId)
                .setSVSSDKCallBack(sVSSDKCallBack)
                .createSVSSDKProxy();
    }

    private void initView() {
        bgVoiceController = findViewById(R.id.bg_voice_controller);
        lvVoiceControllerState = findViewById(R.id.lv_voice_state);
        lvVoiceListenerState = findViewById(R.id.lv_voice_listener_state);
        tvVoiceContent = findViewById(R.id.tv_voice_content);
        tvSendState = findViewById(R.id.tv_send_state);
        tvControlState = findViewById(R.id.tv_control_state);
        imgExit = findViewById(R.id.voice_exit_img);
        tvContent1 = findViewById(R.id.advice_content1);
        tvContent2 = findViewById(R.id.advice_content2);
        tvContent3 = findViewById(R.id.advice_content3);
        tvAdviceTitle = findViewById(R.id.advice_title);

        //开始动画
        fadeIn = ObjectAnimator.ofFloat(bgVoiceController, "alpha", 0f, 1f);
        fadeIn.setDuration(500);
        bgVoiceController.postDelayed(() -> {
            bgVoiceController.setVisibility(View.VISIBLE);
            fadeIn.start();
        }, 200);

        //初始状态
        viewControlHanlder = new ViewControlHanlder(VoiceControllerActivity.this);
        viewControlHanlder.sendEmptyMessage(STATE_LISTEN);
    }

    private void initListener() {
//        imgExit.setOnClickListener(v -> finish());

        bgVoiceController.setOnClickListener(v -> finish());

        lvVoiceControllerState.setOnClickListener(v -> {
            Log.d(TAG, "onClick: ");
            switchListen();
        });

    }

    private void switchListen() {
        if (svssdkProxy == null) {
            return;
        }

        if ("我在，你说".equals(tvVoiceContent.getText().toString())) {
            viewControlHanlder.sendEmptyMessage(STATE_LISTEN_ERROR);
            svssdkProxy.stopListening();
            svssdkProxy.cancelListening();
            return;
        }

        if (!isSure && (currentState == STATE_LISTEN || currentState == STATE_LISTEN_ING)) {
            svssdkProxy.stopListening();
            // svssdkProxy.cancelListening();
            viewControlHanlder.sendEmptyMessage(STATE_SEND_ING);
            return;
        }
        if (currentState == STATE_LISTEN || currentState == STATE_LISTEN_ING) {
            svssdkProxy.stopListening();
            svssdkProxy.cancelListening();
            tvControlState.setVisibility(View.INVISIBLE);
            Log.d(TAG, "switchListen: stop");
        } else if (currentState == STATE_ADVICE ||
                currentState == STATE_SEND_SUCCESS ||
                currentState == STATE_SEND_SUCCEED ||
                currentState == STATE_SEND_ERROR ||
                currentState == STATE_LISTEN_ERROR) {
            Log.d(TAG, "switchListen: start");
            viewControlHanlder.sendEmptyMessage(STATE_LISTEN);
            tvControlState.setText("点击停止聆听");
        }
    }


    private void startRecord() {
        viewControlHanlder.sendEmptyMessageDelayed(LISTEN_CHECK, 10000);
        viewControlHanlder.sendEmptyMessageDelayed(LISTEN_LIMIT, 30000);
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("isVad", true);
                //开启语音识别
                if (svssdkProxy != null) {
                    Log.d(TAG, "startRecord: ");
                    svssdkProxy.startListening(params);
                }
            }

            @Override
            public void permissionDenied(String[] permission) {

            }
        }, Manifest.permission.RECORD_AUDIO);
    }

    private boolean isConnected() {
        return SSConnectManager.getInstance().isConnected();
    }

    private void showVoiceState(String targetJson) {
        lvVoiceControllerState.setAnimation(targetJson);
        lvVoiceControllerState.setRepeatCount(INFINITE);
        lvVoiceControllerState.playAnimation();
    }

    private void showUpVoiceState(String targetJson) {
        lvVoiceListenerState.setVisibility(View.VISIBLE);
        lvVoiceListenerState.setAnimation(targetJson);
        lvVoiceListenerState.setSpeed(2);
        lvVoiceListenerState.setRepeatCount(INFINITE);
        lvVoiceListenerState.playAnimation();
    }

    private void showSendSuccess() {
        lvVoiceControllerState.setAnimation("voice_send_success.json");
        lvVoiceControllerState.setRepeatCount(0);
        lvVoiceControllerState.playAnimation();
    }

    /*********************控制动画***************************/

    @SuppressLint("HandlerLeak")
    class ViewControlHanlder extends Handler {


        private WeakReference<VoiceControllerActivity> weakRef;

        ViewControlHanlder(VoiceControllerActivity voiceControllerActivity) {
            weakRef = new WeakReference<>(voiceControllerActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            VoiceControllerActivity voiceControllerActivity = weakRef.get();

            super.handleMessage(msg);
            switch (msg.what) {
                case STATE_ADVICE:
                    currentState = STATE_ADVICE;
                    tvVoiceContent.setVisibility(View.INVISIBLE);
                    tvSendState.setVisibility(View.INVISIBLE);
                    lvVoiceListenerState.setVisibility(View.INVISIBLE);
                    voiceControllerActivity.showVoiceState("voice_advice.json");
                    tvControlState.setVisibility(View.VISIBLE);
                    tvControlState.setText("点击进入聆听");
                    voiceControllerActivity.showAdvice();
                    break;
                case STATE_LISTEN:
                    currentState = STATE_LISTEN;
                    dismissAdvice();
                    tvSendState.setVisibility(View.INVISIBLE);
                    tvVoiceContent.setVisibility(View.GONE);
                    tvVoiceContent.setVisibility(View.VISIBLE);
                    tvVoiceContent.setText("我在，你说");
                    viewControlHanlder.removeCallbacksAndMessages(null);
                    startRecord();
                    voiceControllerActivity.showVoiceState("voice_listen_wait.json");
                    voiceControllerActivity.showUpVoiceState("voice_listen_wait_up.json");
                    break;
                case STATE_LISTEN_ING:
                    currentState = STATE_LISTEN_ING;
                    voiceControllerActivity.showVoiceState("voice_listen.json");
                    voiceControllerActivity.showUpVoiceState("voice_listen_up.json");
                    break;
                case STATE_LISTEN_ERROR:
                    Log.d(TAG, "handleMessage: STATE_LISTEN_ERROR");
                    currentState = STATE_LISTEN_ERROR;
                    voiceControllerActivity.showVoiceState("voice_send_error.json");
                    tvVoiceContent.setText("抱歉，小维没有听清");
                    lvVoiceListenerState.setVisibility(View.INVISIBLE);
                    tvControlState.setVisibility(View.VISIBLE);
                    tvControlState.setText("点击进入聆听");
                    viewControlHanlder.removeCallbacksAndMessages(null);
                    viewControlHanlder.sendEmptyMessageDelayed(STATE_ADVICE, 1500);
                    break;
                case STATE_SEND_ERROR:
                    currentState = STATE_SEND_ERROR;
                    tvSendState.setVisibility(View.VISIBLE);
                    tvSendState.setText("发送失败");
                    tvControlState.setVisibility(View.VISIBLE);
                    tvControlState.setText("点击进入聆听");
                    voiceControllerActivity.showVoiceState("voice_send_error.json");
                    viewControlHanlder.removeCallbacksAndMessages(null);
                    viewControlHanlder.sendEmptyMessageDelayed(STATE_ADVICE, 1500);
                    break;
                case STATE_SEND_ING:
                    currentState = STATE_SEND_ING;
                    lvVoiceListenerState.setVisibility(View.INVISIBLE);
                    tvSendState.setVisibility(View.VISIBLE);
                    tvSendState.setText("发送中...");
                    tvControlState.setVisibility(View.INVISIBLE);
                    voiceControllerActivity.showVoiceState("voice_send_ing.json");
                    viewControlHanlder.removeCallbacksAndMessages(null);
                    viewControlHanlder.sendEmptyMessageDelayed(SEND_CHECK, 3000);
                    break;
                case STATE_SEND_SUCCESS:
                    currentState = STATE_SEND_SUCCESS;
                    lvVoiceListenerState.setVisibility(View.INVISIBLE);
                    tvSendState.setVisibility(View.VISIBLE);
                    tvSendState.setText("发送成功");
                    tvControlState.setVisibility(View.VISIBLE);
                    tvControlState.setText("点击进入聆听");
                    viewControlHanlder.removeCallbacksAndMessages(null);
                    voiceControllerActivity.showSendSuccess();
                    viewControlHanlder.sendEmptyMessageDelayed(STATE_ADVICE, 1500);
                    break;
                case STATE_SEND_SUCCEED:
                    tvControlState.setVisibility(View.VISIBLE);
                    tvControlState.setText("点击进入聆听");
                    voiceControllerActivity.showVoiceState("voice_advice.json");
                    break;
                case LISTEN_CHECK:
                    Log.d(TAG, "handleMessage: LISTEN_CHECK");
                    if ("我在，你说".equals(tvVoiceContent.getText().toString())) {
                        viewControlHanlder.sendEmptyMessage(STATE_LISTEN_ERROR);
                    }
                    break;
                case LISTEN_LIMIT:
                    Log.d(TAG, "handleMessage: LISTEN_LIMIT");
                    svssdkProxy.stopListening();
                    break;
                case SEND_CHECK:
                    if (!isSendSuccess) {
                        viewControlHanlder.sendEmptyMessage(STATE_SEND_ERROR);
                    }
                    break;
                default:
                    break;
            }

        }
    }

    private void showAdvice() {
        if (Preferences.VoiceAdvice.getUpdateTime() == null) {
            loadData();
            return;
        }
//        Log.d(TAG, "showAdvice: "+Preferences.VoiceAdvice.getUpdateTime());
//        Log.d(TAG, "showAdvice: "+System.currentTimeMillis());
        Log.d(TAG, "showAdvice: " + String.valueOf(System.currentTimeMillis() - Long.valueOf(Preferences.VoiceAdvice.getUpdateTime())));
        if (System.currentTimeMillis() - Long.valueOf(Preferences.VoiceAdvice.getUpdateTime()) < 60 * 60 * 12 * 1000) {
            tvContent1.setVisibility(View.VISIBLE);
            tvContent2.setVisibility(View.VISIBLE);
            tvContent3.setVisibility(View.VISIBLE);
            tvAdviceTitle.setVisibility(View.VISIBLE);

            String advice = Preferences.VoiceAdvice.getVoiceAdvice();
            if (advice == null) {
                //tvAdviceTitle.setText("网络不好，没有相关建议");
                return;
            }
            advice = advice.replace("\"", "").replace("|", " ");
            Log.d(TAG, "showAdvice: " + advice);
            String[] list = advice.split(" ");

            Random random = new Random();
            int index = random.nextInt(list.length);
            List<String> stringList = new ArrayList<>();
            while (stringList.size() < 3 && list.length > 3) {
                stringList.add(list[index]);
                index++;
                if (index == list.length) {
                    index = 0;
                }

            }
            if(stringList.size() < 3){
                return;
            }
            if (stringList.get(0) != null) {
                tvContent1.setText(stringList.get(0));
            }
            if (stringList.get(1) != null) {
                tvContent2.setText(stringList.get(1));
            }
            if (stringList.get(2) != null) {
                tvContent3.setText(stringList.get(2));
            }
        } else {
            loadData();
        }
    }

    private void loadData() {
        Repository.get(VoiceControlRepository.class)
                .getAdvice()
                .setCallback(new BaseRepositoryCallback<VoiceAdviceInfo>() {
                    @Override
                    public void onSuccess(VoiceAdviceInfo adviceInfo) {
                        super.onSuccess(adviceInfo);
                        JsonElement jsonElement = new JsonParser().parse(adviceInfo.getValue());
                        Set<Map.Entry<String, JsonElement>> es = jsonElement.getAsJsonObject().entrySet();
                        for (Map.Entry<String, JsonElement> en : es) {
                            Log.d(TAG, "onSuccess: " + en.getKey() + " " + en.getValue().toString());
                            Preferences.VoiceAdvice.saveVoiceAdvice(en.getValue().toString());
                            Preferences.VoiceAdvice.saveUpdateTime(String.valueOf(System.currentTimeMillis()));
                            showAdvice();
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        Preferences.VoiceAdvice.saveUpdateTime(String.valueOf(System.currentTimeMillis()));
                        showAdvice();
                    }
                });
    }

    private void dismissAdvice() {
        tvContent1.setVisibility(View.INVISIBLE);
        tvContent2.setVisibility(View.INVISIBLE);
        tvContent3.setVisibility(View.INVISIBLE);
        tvAdviceTitle.setVisibility(View.INVISIBLE);
    }

    /**
     * 参数：maxLines 要限制的最大行数
     * 参数：content  指TextView中要显示的内容
     */
    public void setMaxEcplise(final TextView mTextView, final int maxLines, final String content) {
        Log.d(TAG, "setMaxEcplise: " + mTextView.getLineCount());

        if (mTextView.getLineCount() > maxLines) {
            int lineEndIndex = mTextView.getLayout().getLineEnd(maxLines - 1);
            //下面这句代码中：我在项目中用数字3发现效果不好，改成1了
            String text = "..." + content.subSequence(content.length() - lineEndIndex, content.length());
            mTextView.setText(text);
        } else {
            mTextView.setText(content);
        }

//        ViewTreeObserver observer = mTextView.getViewTreeObserver();
//        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mTextView.setText(content);
//                if (mTextView.getLineCount() > maxLines) {
//                    int lineEndIndex = mTextView.getLayout().getLineEnd(maxLines - 1);
//                    //下面这句代码中：我在项目中用数字3发现效果不好，改成1了
//                    String text = "..." + content.subSequence(content.length() - lineEndIndex, content.length());
//                    mTextView.setText(text);
//                } else {
//                    mTextView.setText(content);
//                    removeGlobalOnLayoutListener(mTextView.getViewTreeObserver(), this);
//                }
//            }
//        });
    }

//    @SuppressLint("NewApi")
//    private void removeGlobalOnLayoutListener(ViewTreeObserver obs, ViewTreeObserver.OnGlobalLayoutListener listener) {
//        if (obs == null) {
//            return;
//        }
//        obs.removeOnGlobalLayoutListener(listener);
//    }

}