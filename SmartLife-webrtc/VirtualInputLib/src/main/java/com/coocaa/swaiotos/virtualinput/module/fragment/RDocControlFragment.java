package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.content.Context;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.data.channel.AppInfo;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartscreen.data.channel.DocParams;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.swaiotos.virtualinput.event.GlobalEvent;
import com.coocaa.swaiotos.virtualinput.event.RequestAppInfoEvent;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.module.view.document.DocumentCtrlBlankLayout;
import com.coocaa.swaiotos.virtualinput.module.view.document.DocumentPreviewsLayout;
import com.coocaa.swaiotos.virtualinput.module.view.document.ExcelSheetsLayout;
import com.coocaa.swaiotos.virtualinput.utils.BrightnessTools;
import com.coocaa.swaiotos.virtualinput.utils.DimensUtils;
import com.coocaa.swaiotos.virtualinput.utils.UiUtil;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.sensor.client.ISmartApi;
import swaiotos.sensor.client.SensorClient;
import swaiotos.sensor.client.data.ClientBusinessInfo;
import swaiotos.sensor.connect.IConnectCallback;
import swaiotos.sensor.touch.InputTouchView;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;
import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_CLIENT_MEDIA_PLAYER;

public class RDocControlFragment extends BaseLazyFragment implements View.OnClickListener {
    private final static String TAG = "DocumentControlFragment";
    private Gson mGson;
    private Vibrator vibrator;
    private int interval = 100;
    private String appletId, appletName, owner;
    private String mCurFormat;//当前投放的文档格式
    private int mCurScrollPos = 0;//(0：中间，1：到顶部了，2：到底部了)
    private int mCurrentPage = 1;//当前投屏的文档页面index
    private int mTotalCount = 0;//当前投放的文档总页数
    private int mCurPageWidth = 0, mCurPageHeight = 0;//当前投放的文档页面宽高
    private int mCurPageDirection = LinearLayout.HORIZONTAL;//当前投放的文档页面方向：0横向、1纵向
    private int mOpenState = 0;//当前文档打开的状态（0：未打开，1：已打开，2：正在打开）
    private long mLastSendTime = 0;
    private long VIBRATE_DURATION = 60L;
    private float mCurAppBright = 0;//当前屏幕亮度
    private ImageView mIconPromptV;//控制提示
    private View mAboveBlank, mBelowBlank;
    private DocumentCtrlBlankLayout mCtrlBlankLayout;
    private DocumentPreviewsLayout mDocumentPreviewsLayout;
    private ExcelSheetsLayout mExcelSheetsLayout;
    private View mResetSizeBtn;
    private boolean needRequestPreviews = false;//是否需求开始获取预览图
    private List<String> mPreviewList = new ArrayList<>();
    private List<String> mSheetsList = new ArrayList<>();
    private String mCurSheetName = "";//当前的Excel表名
    //接收并将event事件给SensorClient
    private InputTouchView inputTouchView;
    //实际转发event给dongle的对象
    private SensorClient client;
    private int mIotChannelVerCode = 0;//Dongle端通道的版本号
    private boolean isDestroy = false;
    private final static String CMD_SCROLL_UP = "scroll_up";
    private final static String CMD_SCROLL_DOWN = "scroll_down";
    private final static int POS_CENTER = 0;//中间
    private final static int POS_BORDER_UP = 1;//第一页
    private final static int POS_BORDER_DOWN = 2;//最后一页
    private final static String FORMAT_PPT = "PPT";
    private final static String FORMAT_PDF = "PDF";
    private final static String FORMAT_WORD = "Word";
    private final static String FORMAT_EXCEL = "Excel";

    @Override
    protected int getContentViewId() {
        return R.layout.vi_document_view;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        UiUtil.instance(getContext());
        mIconPromptV = view.findViewById(R.id.icon_prompt);
        mCtrlBlankLayout = view.findViewById(R.id.blank_layout);
        mAboveBlank = view.findViewById(R.id.above_blank);
        mBelowBlank = view.findViewById(R.id.below_blank);
        mResetSizeBtn = view.findViewById(R.id.reset_size_btn);
        mDocumentPreviewsLayout = new DocumentPreviewsLayout(getContext(), (RecyclerView) view.findViewById(R.id.preview_layout));
        mExcelSheetsLayout = new ExcelSheetsLayout(getContext(), (RecyclerView) view.findViewById(R.id.sheets_layout));
        mAboveBlank.setOnClickListener(this);
        mBelowBlank.setOnClickListener(this);
        mResetSizeBtn.setOnClickListener(this);
        mResetSizeBtn.setVisibility(View.GONE);
        mCtrlBlankLayout.setOnSlideCtrlListener(new DocumentCtrlBlankLayout.OnSlideCtrlListener() {
            @Override
            public void prePage() {
                scroll(CMD_SCROLL_UP);
            }

            @Override
            public void nextPage() {
                scroll(CMD_SCROLL_DOWN);
            }
        });
        requestIotChannelVersion();
        initSensorClient(view);
        if (mGson == null) {
            mGson = new Gson();
        }
    }

    private void requestIotChannelVersion() {
        EventBus.getDefault().register(this);
        //获取Dongle通道版本号
        List<String> packageList = new ArrayList<>();
        packageList.add("swaiotos.channel.iot");
        String param = new Gson().toJson(packageList);
        CmdData data = new CmdData("getAppInfos", CmdData.CMD_TYPE.APP_INFOS.toString(), param);
        String cmd = data.toJson();
        H5ChannelInstance.getSingleton().sendText(TARGET_APPSTATE, cmd, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestAppInfoEvent(RequestAppInfoEvent appInfoEvent) {
        if (null != appInfoEvent && null != appInfoEvent.appInfoList && appInfoEvent.appInfoList.size() > 0) {
            for (AppInfo appInfo : appInfoEvent.appInfoList) {
                if (appInfo.pkgName.equals("swaiotos.channel.iot")) {
                    Log.i(TAG, "onRequestAppInfoEvent: " + appInfo.appName + "---" + appInfo.versionCode);
                    mIotChannelVerCode = appInfo.versionCode;
                }
            }
        }
    }

    private void initSensorClient(View view) {
        ClientBusinessInfo clientBusinessInfo = new ClientBusinessInfo("ss-doc-control-client",
                "ss-clientID-UniversalMediaPlayer", "文档共享控制", 0, 0);
        clientBusinessInfo.protoVersion = 0;//增加版本拉平
        client = new SensorClient(getContext(), clientBusinessInfo,
                VirtualInputUtils.getAccountInfo());
        client.setSmartApi(new ISmartApi() {
            @Override
            public boolean isSameWifi() {
                return SmartApi.isSameWifi();
            }

            @Override
            public void startConnectSameWifi() {
                SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            }
        });
        client.setCallback(new IConnectCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "SensorClient connect onSuccess: ");
            }

            @Override
            public void onFail(String reason) {
                Log.i(TAG, "SensorClient connect onFail: " + reason);
            }

            @Override
            public void onFailOnce(String reason) {

            }

            @Override
            public void onClose() {
                Log.i(TAG, "SensorClient onClose: ");
            }

            @Override
            public void onMessage(String msg) {
                Log.i(TAG, "SensorClient onMessage: " + msg);
            }
        });

        RelativeLayout touchLayout = view.findViewById(R.id.touch_layout);
        int width = DimensUtils.getDeviceWidth(getContext()) - UiUtil.Div(20);
        inputTouchView = (InputTouchView) client.getView();
        inputTouchView.setNeedTwoFinger(true);
        inputTouchView.setBackground(getResources().getDrawable(R.drawable.bg_round_12_black_alpha20));
        RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        viewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        touchLayout.addView(inputTouchView, viewParams);
//        inputTouchView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        //不在同一wifi，弹连接wifi弹窗，防止被误点
        if (!SmartApi.isSameWifi()) {
            SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            return;
        }
        if (view.getId() == R.id.above_blank) {
            //点击上方空白区域，上一张
            scroll(CMD_SCROLL_UP);
        } else if (view.getId() == R.id.below_blank) {
            //点击下方空白区域，下一张
            scroll(CMD_SCROLL_DOWN);
        } else if (view.getId() == R.id.reset_size_btn) {
            sendCmd("restore_size", "");
        }
    }

    private void scroll(String cmd) {
        if (POS_BORDER_UP == mCurScrollPos && CMD_SCROLL_UP.equals(cmd)) {
            VIBRATE_DURATION = 180L;
            ToastUtils.getInstance().showGlobalLong("已经是第一页");
        } else if (POS_BORDER_DOWN == mCurScrollPos && CMD_SCROLL_DOWN.equals(cmd)) {
            VIBRATE_DURATION = 180L;
            ToastUtils.getInstance().showGlobalLong("已经是最后一页");
        } else {
            VIBRATE_DURATION = 60L;
        }
        sendScrollCmd(cmd);
        playVibrate();
    }

    private void playVibrate() {
        if (vibrator == null) {
            vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null) {
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private void sendScrollCmd(String cmd) {
        long curTime = System.currentTimeMillis();
        long diff = (curTime - mLastSendTime);
        if (diff < interval) {
            Log.i(TAG, "sendCmd: diff < " + interval + " :" + diff);
            mLastSendTime = curTime;
            return;
        }
        mLastSendTime = curTime;
        DocParams docParams = new DocParams();
        docParams.platform = "android";
        docParams.scale = 0.58;
        String paramJson = mGson.toJson(docParams);
        sendCmd(cmd, paramJson);
        GlobalEvent.onClick(appletId, appletName, cmd);
    }

    private void sendCmd(String cmd, String paramJson) {
        Log.i(TAG, "doc sendCmd: " + cmd + "--" + paramJson);
        GlobalIOT.iot.sendCmd(cmd, "doc", paramJson, TARGET_CLIENT_MEDIA_PLAYER, owner);
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        Log.i(TAG, "setFragmentData --> state:" + BusinessState.encode(stateBean));
        if (sceneConfigBean != null) {
            appletId = sceneConfigBean.appletUri;
            appletName = sceneConfigBean.appletName;
        }
        owner = User.encode(stateBean.owner);
        setLayoutOwner();
        if (stateBean.values != null) {
            try {
                JSONObject values = JSONObject.parseObject(stateBean.values);
                mCurFormat = values.getString("format");
                mCurScrollPos = getInteger(values, "scrollPosition");
                mOpenState = getInteger(values, "openState");
                sensorClientStart();
                excel(values);
                checkPreviews(values);
                refreshIconPrompt();
                refreshTouchView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setLayoutOwner() {
        if (mDocumentPreviewsLayout != null) {
            mDocumentPreviewsLayout.setOwner(owner);
        }
        if (mExcelSheetsLayout != null) {
            mExcelSheetsLayout.setOwner(owner);
        }
    }

    private void sensorClientStart() {
        try {
            if (!FORMAT_PPT.equals(mCurFormat)) {
                if (mOpenState == 1) {
                    if (client != null) {
                        client.start();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void excel(final JSONObject values) {
        if (FORMAT_EXCEL.equals(mCurFormat)) {
            try {
                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        mCurSheetName = values.getString("sheetName");
                        String sheets = values.getString("sheets");
                        if (!TextUtils.isEmpty(sheets)) {
                            List<String> sheetList = (List<String>) JSONArray.parse(sheets);
                            if (sheetList != null && sheetList.size() > 1) {
                                showResetSizeBtn(true);
                                mSheetsList.clear();
                                mSheetsList.addAll(sheetList);
                                mExcelSheetsLayout.setVisibility(View.VISIBLE);
                                mExcelSheetsLayout.refreshData(mSheetsList, mCurSheetName);
                            }
                        } else {
                            showResetSizeBtn(false);
                            mSheetsList.clear();
                            mExcelSheetsLayout.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int getInteger(JSONObject values, String key) {
        Integer intValue = values.getInteger(key);
        return intValue == null ? 0 : intValue;
    }

    private void checkPreviews(JSONObject values) {
        switch (mCurFormat) {
            case FORMAT_PPT:
            case FORMAT_PDF:
            case FORMAT_WORD:
                mCtrlBlankLayout.setDocFormat(mCurFormat);
                try {
                    mTotalCount = getInteger(values, "totalCount");
                    mCurrentPage = getInteger(values, "currentPage");
                    mCurPageWidth = getInteger(values, "pageWidth");
                    mCurPageHeight = getInteger(values, "pageHeight");
                    if (mCurrentPage < 0) {
                        mCurrentPage = 0;
                    } else if (mCurrentPage > mTotalCount) {
                        mCurrentPage = mTotalCount;
                    }
                } catch (Exception e) {
                    mTotalCount = 0;
                    mCurrentPage = 0;
                    mCurPageWidth = 0;
                    mCurPageHeight = 0;
                    e.printStackTrace();
                }
                if (mTotalCount > 0) {
                    showResetSizeBtn(!mCurFormat.equals(FORMAT_PPT));
                    if (mCurPageWidth < mCurPageHeight) {
                        //竖版
                        mCurPageDirection = LinearLayout.VERTICAL;
                    } else {
                        //默认横版
                        mCurPageDirection = LinearLayout.HORIZONTAL;
                    }
                    if (mPreviewList.size() == 0 || mPreviewList.size() != mTotalCount) {
                        mPreviewList.clear();
                        needRequestPreviews = true;
                        for (int i = 0; i < mTotalCount; i++) {
                            mPreviewList.add("");
                        }
                    }
                    String imageCache = values.getString("imageCache");
//                Log.i(TAG, "setFragmentData imageCache: " + imageCache);
                    List<String> images;
                    if (!TextUtils.isEmpty(imageCache)) {
                        images = JSONArray.parseArray(imageCache, String.class);
                        if (images != null && images.size() > 0) {
                            if (isNeedUpdate(images)) {
                                for (int i = 0; i < images.size(); i++) {
                                    mPreviewList.set(i, images.get(i));
                                }
                            }
                        }
                    }
                    requestPreviews();
                    if (!mPreviewList.contains("")) {
                        Log.i(TAG, "checkPreviews: stop requestPreviews");
                        HomeIOThread.removeTask(mRequestPreviewsRunable);
                    }
                } else {
                    mPreviewList.clear();
                }
                break;
            default:
                mPreviewList.clear();
                break;
        }
        refreshPreviewUI();
    }

    private void refreshIconPrompt() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (mCurFormat) {
                        case FORMAT_PPT:
                            mIconPromptV.setImageResource(R.drawable.doc_ctrl_prompt_ppt);
                            mExcelSheetsLayout.setVisibility(View.GONE);
                            mDocumentPreviewsLayout.setVisibility(View.VISIBLE);
                            break;
                        case FORMAT_PDF:
                        case FORMAT_WORD:
                            mIconPromptV.setImageResource(R.drawable.doc_ctrl_prompt_pdf_word);
                            mExcelSheetsLayout.setVisibility(View.GONE);
                            mDocumentPreviewsLayout.setVisibility(View.VISIBLE);
                            break;
                        case FORMAT_EXCEL:
                            mIconPromptV.setImageResource(R.drawable.doc_ctrl_prompt_excel);
                            mExcelSheetsLayout.setVisibility(View.VISIBLE);
                            mDocumentPreviewsLayout.setVisibility(View.GONE);
                            break;
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void showResetSizeBtn(boolean show) {
        if (show && mIotChannelVerCode >= 2021040119) {
            mResetSizeBtn.setVisibility(View.VISIBLE);
        } else {
            mResetSizeBtn.setVisibility(View.GONE);
        }
    }

    private void refreshTouchView() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                if (FORMAT_PPT.equals(mCurFormat)) {
                    inputTouchView.setVisibility(View.GONE);
                } else {
                    inputTouchView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void requestPreviews() {
        if (needRequestPreviews) {
            needRequestPreviews = false;
            //主动获取预览图
            Log.i(TAG, "start requestPreviews");
            HomeIOThread.removeTask(mRequestPreviewsRunable);
            HomeIOThread.execute(1500, mRequestPreviewsRunable);
        }
    }

    private Runnable mRequestPreviewsRunable = new Runnable() {
        @Override
        public void run() {
            if (isDestroy) {
                return;
            }
            sendCmd("requestPreviews", "");
            HomeIOThread.execute(2000, this);
        }
    };

    private boolean isNeedUpdate(List<String> images) {
        for (String imgUrl : images) {
            if (!mPreviewList.contains(imgUrl)) {
                return true;
            }
        }
        return false;
    }

    private void refreshPreviewUI() {
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                if (mDocumentPreviewsLayout != null) {
                    mDocumentPreviewsLayout.setCurPageDirection(mCurPageDirection);
                    mDocumentPreviewsLayout.refreshData(mPreviewList, mCurrentPage > 0 ? mCurrentPage - 1 : 0);
                }
            }
        });
    }

    private Runnable mBrightnessRunable = new Runnable() {
        @Override
        public void run() {
            mCurAppBright = BrightnessTools.getAppBrightness(getContext());
            Log.i(TAG, "BrightnessRunable mCurAppBright: " + mCurAppBright);
            //降低屏幕亮度为默认的1/10
            BrightnessTools.setAppBrightness(getContext(), mCurAppBright / 10f);
        }
    };

    /**
     * 15秒无操作降低屏幕亮度
     */
    private void delaySetAppBrightness() {
        HomeUIThread.removeTask(mBrightnessRunable);
        HomeUIThread.execute(15 * 1000, mBrightnessRunable);
    }

    /**
     * 恢复屏幕亮度（跟随系统）
     */
    private void resetDefaultBrightness() {
        try {
            HomeUIThread.removeTask(mBrightnessRunable);
            float appBright = BrightnessTools.getAppBrightness(getContext());
            if (appBright != mCurAppBright) {
                Log.i(TAG, "resetDefaultBrightness --> curBri: " + appBright + "---lastBri: " + mCurAppBright);
                mCurAppBright = appBright;
                BrightnessTools.setAppBrightness(getContext(), -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        isDestroy = false;
        sensorClientStart();
        //判断从别的页面切回来是否需要继续获取预览图
        if (mPreviewList.size() > 0 && mPreviewList.contains("")) {
            needRequestPreviews = true;
            requestPreviews();
        }
        delaySetAppBrightness();
    }

    /**
     * 控制器页面的触摸事件
     *
     * @param ev
     */
    public void dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            resetDefaultBrightness();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            delaySetAppBrightness();
        }
    }

    /**
     * 控制器页面的屏幕显示/隐藏监听
     *
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.i(TAG, "onWindowFocusChanged: " + hasFocus);
        if (hasFocus) {
            delaySetAppBrightness();
        } else {
            resetDefaultBrightness();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        HomeIOThread.removeTask(mRequestPreviewsRunable);//页面离开了就不再请求预览图
        resetDefaultBrightness();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (client != null) client.stop();
        Log.i(TAG, "onStop: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        isDestroy = true;
        HomeIOThread.removeTask(mRequestPreviewsRunable);
        if (client != null) client.stop();
        if (mDocumentPreviewsLayout != null) {
            mDocumentPreviewsLayout.destroy();
        }
        if (mExcelSheetsLayout != null) {
            mExcelSheetsLayout.destroy();
        }
        resetDefaultBrightness();
        EventBus.getDefault().unregister(this);
    }
}
