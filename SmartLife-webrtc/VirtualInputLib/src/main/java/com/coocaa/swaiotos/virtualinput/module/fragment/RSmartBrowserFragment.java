package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartscreen.utils.AndroidUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.SmartApiListener;
import com.coocaa.smartsdk.SmartApiListenerImpl;
import com.coocaa.swaiotos.virtualinput.data.SmartBrowserConfig;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;
import com.coocaa.tvpi.module.io.HomeUIThread;

import swaiotos.runtime.h5.H5ChannelInstance;

@SuppressLint("ClickableViewAccessibility")
public class RSmartBrowserFragment extends BaseLazyFragment {
    public static final String TAG = "SmartBrowser";
    //根布局
    private RelativeLayout mLayout;
    private Context context;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    private float y1 = 0;
    private float y2 = 0;
    private String owner;
    private TextView tvPlayVideo;
    private boolean hasVideoPlay = false;
    private String extJsUrl = null;
    private final static String TARGET_ID = "ss-clientID-runtime-h5-channel";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        loadConfig();
    }

    @Override
    protected View getContentView() {

        context = getActivity();
        if (context == null) return null;
        mLayout = new RelativeLayout(context);
        ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        mLayout.setLayoutParams(layoutParams);

        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.doc_ctrl_prompt_ppt);
        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        childLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLayout.addView(imageView, childLayoutParams);

        tvPlayVideo = new TextView(context);
        tvPlayVideo.setText("播放视频");
        tvPlayVideo.setTextSize(16);
        tvPlayVideo.setTypeface(Typeface.DEFAULT_BOLD);
        tvPlayVideo.setTextColor(Color.parseColor("#ccffffff"));
        tvPlayVideo.setBackground(getResources().getDrawable(R.drawable.bg_0cffffff_round_25));
        tvPlayVideo.setGravity(Gravity.CENTER);
        tvPlayVideo.setVisibility(View.GONE);
        childLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,  DimensUtils.dp2Px(context, 50));
        childLayoutParams.leftMargin = DimensUtils.dp2Px(context, 20);
        childLayoutParams.rightMargin = DimensUtils.dp2Px(context, 20);
        childLayoutParams.bottomMargin = DimensUtils.dp2Px(context, 30);
        childLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mLayout.addView(tvPlayVideo, childLayoutParams);

        tvPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasVideoPlay && !TextUtils.isEmpty(extJsUrl)) {
                    sendCmd("loadExtJs", extJsUrl);
                }
            }
        });

        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        y1 = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        //当手指离开的时候
                        y2 = event.getY();
                        if (y1 - y2 > 50) {
                            //上滑
                            sendTouchEvent(false);
                        } else if (y2 - y1 > 50) {
                            //下滑
                            sendTouchEvent(true);
                        }
                        break;
                }
                return false;
            }
        });
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "-- onResume");
        SmartApi.addListener(listener);
        SmartApi.setMsgDispatchEnable(TARGET_ID, true);
        requestPageUrl();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "-- onStop");
        super.onStop();
        SmartApi.removeListener(listener);
        SmartApi.setMsgDispatchEnable(TARGET_ID, false);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "~~ onDestroy");
        super.onDestroy();
    }

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        if (stateBean.owner != null) {
            owner = User.encode(stateBean.owner);
        }
    }

    /**
     * @param isDown 是否下滑
     */
    private void sendTouchEvent(boolean isDown) {
        sendCmd("scrollY", isDown ? "up" : "down");
    }

    private void sendCmd(String cmd, String extra) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("do", "web_control");
        jsonObject.put("cmd", cmd);
        jsonObject.put("extra", extra);
        Log.d(TAG, "send web control cmd : " + cmd + ", extra : " + extra);
        H5ChannelInstance.getSingleton().sendText(TARGET_ID, JSON.toJSONString(jsonObject), owner, null);
    }

    private SmartApiListener listener = new SmartApiListenerImpl() {
        @Override
        public void onDispatchMessage(String clientId, String msgJson) {
            try {
                Log.d(TAG, "onDispatchMessage =" + msgJson);
                JSONObject msgObject = JSON.parseObject(msgJson);
                String content = msgObject.getString("content");
                JSONObject jsonObject = JSON.parseObject(content);
                if(jsonObject.containsKey("cmd")) {
                    if("response_web_info".equals(jsonObject.get("cmd"))) {
                        parseWebInfo(jsonObject);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void parseWebInfo(JSONObject jsonObject) {
        String url = jsonObject.getString("url");
        Log.d(TAG, "url=" + url);
        String host = Uri.parse(url).getAuthority();
        Log.d(TAG, "host=" + host);
        boolean showVideo = false;
        if(config != null && config.dataList != null) {
            for(SmartBrowserConfig.SmartBrowserConfigBean bean : config.dataList) {
                if(TextUtils.equals(host, bean.host)) {
                    Log.d(TAG, "find host config : " + bean);
                    RSmartBrowserFragment.this.hasVideoPlay = bean.showVideo;
                    RSmartBrowserFragment.this.extJsUrl = bean.extJs;
                    showVideo = bean.showVideo;
                }
            }
        }
        Log.d(TAG, "after parseWebInfo, url=" + url + ", extJs=" + extJsUrl + ", showVideo=" + showVideo);
        final boolean finalShowVideo = showVideo;
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                tvPlayVideo.setVisibility(finalShowVideo ? View.VISIBLE : View.GONE);
            }
        });
    }

    SmartBrowserConfig config;
    private void loadConfig() {
        String configString = AndroidUtil.readAssetFile("smart_browser_config.json");
        Log.d(TAG, "configString = " + configString);
        try {
            config = JSON.parseObject(configString, SmartBrowserConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "loadConfig ret : " + config);
    }

    private void requestPageUrl() {
        sendCmd("request_web_info", "");
    }
}
