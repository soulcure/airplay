package com.coocaa.swaiotos.virtualinput.module.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.swaiotos.virtualinput.data.FragmentName;
import com.coocaa.swaiotos.virtualinput.data.RemoteSubtitleBean;
import com.coocaa.swaiotos.virtualinput.event.GlobalEvent;
import com.coocaa.swaiotos.virtualinput.module.adapter.RFragmentPagerAdapter;
import com.coocaa.swaiotos.virtualinput.module.adapter.RemoteControlAdapter;
import com.coocaa.swaiotos.virtualinput.module.fragment.RControlFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RDefaultFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RDocControlFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RGameFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RLiveFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RMusicFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RPhotoAlbumFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RSmartBrowserFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RSpeakFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RVideoFragment;
import com.coocaa.swaiotos.virtualinput.module.fragment.RWhiteboardFragment;
import com.coocaa.swaiotos.virtualinput.module.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateChangeListener;
import com.coocaa.swaiotos.virtualinput.state.FloatVIStateManager;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import swaiotos.runtime.base.StatusBarHelper;
import swaiotos.runtime.h5.BaseH5AppletActivity;
import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.core.os.H5RunType;

import static swaiotos.runtime.h5.core.os.H5RunType.RunType.MOBILE_RUNTYPE_ENUM;

/**
 * @ClassName RemoteControlActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/16
 */
public class RemoteControlActivity extends BaseH5AppletActivity implements RemoteControlAdapter.OnSubTitleClickListener {

    private final String TAG = RemoteControlActivity.class.getSimpleName();
    private ImageView backImg;
    private RecyclerView subtitleRecycler;
    private RemoteControlAdapter subtitleAdapter;
    private ViewPager2 mViewPager2;
    private RFragmentPagerAdapter viewPagerAdapter;

    RDefaultFragment rDefaultFragment;
    RLiveFragment rLiveFragment;
    RVideoFragment rVideoFragment;
    RMusicFragment rMusicFragment;
    RControlFragment rControlFragment;
    RGameFragment rGameFragment;
    RDocControlFragment rDocControlFragment;
    RPhotoAlbumFragment rPhotoAlbumFragment;
    RSpeakFragment rSpeakFragment;
    RWhiteboardFragment mWhiteboardFragment;
    RSmartBrowserFragment rSmartBrowserFragment;

    private SparseArray<Fragment> mFragmentSparseArray = new SparseArray<>();
    private SceneConfigBean mSceneConfigBean;
    private BusinessState mBusinessState;
    private String mContentType;
    private int currentPageId;
    private int userSelectPosition = 0;
    private Map<String, String> params;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        overridePendingTransition(R.anim.remote_launch, 0);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarDarkMode(this);
        H5ChannelInstance.getSingleton().open(this.getApplicationContext());
        initView();
        setSubtitleData();
        createFragment();

        userSelectPosition = 0;
        currentPageId = mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DEFALUT);
        Log.d(TAG, "onCreate: currentPageId: " + currentPageId);

        initDataListener();
        getCurState();
        submitPanelShow(0);
    }

    @Override
    protected H5RunType.RunType runType() {
        return MOBILE_RUNTYPE_ENUM;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.dialog_out);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (currentPageId == mFragmentSparseArray.indexOfValue(rDocControlFragment)) {
            rDocControlFragment.dispatchTouchEvent(ev);
        } else if (currentPageId == mFragmentSparseArray.indexOfValue(rPhotoAlbumFragment)) {
            rPhotoAlbumFragment.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentPageId == mFragmentSparseArray.indexOfValue(rDocControlFragment)) {
            rDocControlFragment.onWindowFocusChanged(hasFocus);
        } else if (currentPageId == mFragmentSparseArray.indexOfValue(rPhotoAlbumFragment)) {
            rPhotoAlbumFragment.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        return new RemoteLayoutBuilder();
    }

    public static class RemoteLayoutBuilder implements LayoutBuilder {

        @Override
        public View build(View content) {
            return content;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setKeepScreenOn(false);
        FloatVIStateManager.INSTANCE.removeListener(mFloatVIStateChangeListener);
    }

    @Override
    public void onSubTitleClick(int position) {
        boolean isLaser = false;//激光笔单独通知切换
        hideKeyboard();
        try {
            userSelectPosition = position;
            switch (position) {
                case 0:
                    getCurState();
                    break;
                case 1:
                    mViewPager2.setCurrentItem(mFragmentSparseArray.indexOfKey(FragmentName.PAGE_SPEAK), false);
                    break;
                case 2:
                    mViewPager2.setCurrentItem(mFragmentSparseArray.indexOfKey(FragmentName.PAGE_CONTROL_CENTER), false);
                    break;
                default:
                    mViewPager2.setCurrentItem(currentPageId, false);
                    break;
            }
            submitPanelShow(position);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initView() {
        backImg = findViewById(R.id.back);
        subtitleRecycler = findViewById(R.id.subtitle_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        CommonHorizontalItemDecoration decoration =
                new CommonHorizontalItemDecoration(0, 56);
        subtitleRecycler.setLayoutManager(layoutManager);
        subtitleRecycler.addItemDecoration(decoration);
        subtitleAdapter = new RemoteControlAdapter();
        subtitleAdapter.setOnSubTitleClickListener(this);
        subtitleRecycler.setAdapter(subtitleAdapter);

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setSubtitleData() {
        List<RemoteSubtitleBean> dataList = new ArrayList<>();
        RemoteSubtitleBean currentData = new RemoteSubtitleBean();
        currentData.title = "当前内容";
        currentData.idNormalState = R.drawable.remote_current_normal;
        currentData.idSelectState = R.drawable.remote_current_select;
        dataList.add(currentData);

        RemoteSubtitleBean speakData = new RemoteSubtitleBean();
        speakData.title = "互动";
        speakData.idNormalState = R.drawable.remote_speak_normal;
        speakData.idSelectState = R.drawable.remote_speak_select;
        dataList.add(speakData);

        RemoteSubtitleBean controlData = new RemoteSubtitleBean();
        controlData.title = "遥控器";
        controlData.idNormalState = R.drawable.remote_control_normal;
        controlData.idSelectState = R.drawable.remote_control_select;
        dataList.add(controlData);

        subtitleAdapter.addAll(dataList);
    }

    private void createFragment() {
        rDefaultFragment = new RDefaultFragment();
        rLiveFragment = new RLiveFragment();
        rVideoFragment = new RVideoFragment();
        rMusicFragment = new RMusicFragment();
        rControlFragment = new RControlFragment();
        rGameFragment = new RGameFragment();
        rDocControlFragment = new RDocControlFragment();
        rPhotoAlbumFragment = new RPhotoAlbumFragment();
        rSpeakFragment = new RSpeakFragment();
        mWhiteboardFragment = new RWhiteboardFragment();
        rSmartBrowserFragment = new RSmartBrowserFragment();

        mFragmentSparseArray.put(FragmentName.PAGE_DEFALUT, rDefaultFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_LIVE, rLiveFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_LOCAL_VIDEO, rVideoFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_MUSIC, rMusicFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_CONTROL_CENTER, rControlFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_GAME, rGameFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_DOCUMENT, rDocControlFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_PHOTO_ALBUM, rPhotoAlbumFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_SPEAK, rSpeakFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_WHITEBOARD, mWhiteboardFragment);
        mFragmentSparseArray.put(FragmentName.PAGE_SMART_BROWSER, rSmartBrowserFragment);

        mViewPager2 = findViewById(R.id.view_pager2);
        mViewPager2.setUserInputEnabled(false);
        viewPagerAdapter = new RFragmentPagerAdapter(getSupportFragmentManager(), getLifecycle(),
                mFragmentSparseArray);
        mViewPager2.setAdapter(viewPagerAdapter);
        //设置预加载数量，让ViewPager2预先创建出所有的Fragment，防止切换造成的频繁销毁和创建
        mViewPager2.setOffscreenPageLimit(mFragmentSparseArray.size());
        mViewPager2.registerOnPageChangeCallback(mOnPageChangeCallback);
    }

    ViewPager2.OnPageChangeCallback mOnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);

        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DOCUMENT)) {
                Log.d(TAG, "onPageSelected: change document page, keep screen on.");
                setKeepScreenOn(true);
            } else {
                setKeepScreenOn(false);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };

    private void initDataListener() {
        FloatVIStateManager.INSTANCE.addListener(mFloatVIStateChangeListener);
    }

    private void getCurState() {
        FloatVIStateManager.INSTANCE.freshState();
        mBusinessState = FloatVIStateManager.INSTANCE.getCurState();
        Log.d(TAG, "getCurState: mBusinessState: " + BusinessState.encode(mBusinessState));
        if (mBusinessState == null) {
            getSceneConfigBean("DEFAULT");
            currentPageId = mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DEFALUT);
            mViewPager2.setCurrentItem(currentPageId, false);
        } else {
            parseBusinessState(mBusinessState);
        }
    }

    FloatVIStateChangeListener mFloatVIStateChangeListener = new FloatVIStateChangeListener() {
        @Override
        public void onProgressResult(@NotNull String json) {

        }

        @Override
        public void onProgressLoading(@NotNull String json) {

        }

        @Override
        public void onStateInit() {

        }

        @Override
        public void onStateChanged(@org.jetbrains.annotations.Nullable BusinessState businessState) {
            Log.d(TAG, "onUdpateBusinessState: 回调的业务状态消息：" + new Gson().toJson(businessState));
            parseBusinessState(businessState);
        }

        @Override
        public void onDeviceConnectChanged(boolean isConnect) {
            if (!isConnect) {
                Log.d(TAG, "onDeviceConnectChanged,isConnect false,change page default.");
            }
        }
    };

    /**
     * @param state 解析后台给的业务数据
     */
    private void parseBusinessState(BusinessState state) {
        if (state == null) {
            return;
        }
        //当前内容标签下
        if (userSelectPosition == 0) {
            // 当id为null时，表示是旧版业务数据,需要根据type去解析配置文件
            if (TextUtils.isEmpty(state.id)) {
                if (!TextUtils.isEmpty(state.type)) {
                    getSceneConfigBean(state.type.toUpperCase());
                    switchPage(state, state.type.toUpperCase());
                }
            } else {
                //当id不是null时，表示是新版业务数据,需要根据id去解析配置文件
                getSceneConfigBean(state.id.toUpperCase());
                switchPage(state, state.id.toUpperCase());
            }
        } else {
            return;
        }
    }

    private void getSceneConfigBean(String typeOrId) {
        mSceneConfigBean = FloatVIStateManager.INSTANCE.getSceneConfigBean(typeOrId);
        Log.d(TAG, "getSceneConfigBean: " + mSceneConfigBean);
        if (mSceneConfigBean != null) {
            mContentType = mSceneConfigBean.contentType;
        }
    }

    /**
     * @param typeOrId 跟contentUrl比对，再去判断contentType
     */
    private void switchPage(final BusinessState businessState, final String typeOrId) {
        mBusinessState = businessState;
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run1: " + typeOrId);
                if (!TextUtils.isEmpty(mContentType) && mContentType.equals("np")) {
                    if (mSceneConfigBean != null && !TextUtils.isEmpty(mSceneConfigBean.contentUrl)) {
                        switch (mSceneConfigBean.contentUrl) {
                            case "DEFAULT":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DEFALUT);
                                break;
                            case "VIDEO":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_LOCAL_VIDEO);
                                rVideoFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                            case "IMAGE":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_PHOTO_ALBUM);
                                rPhotoAlbumFragment.setFragmentData(mBusinessState,
                                        mSceneConfigBean);
                                break;
                            case "AUDIO":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_MUSIC);
                                rMusicFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                            case "DOC":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DOCUMENT);
                                rDocControlFragment.setFragmentData(mBusinessState,
                                        mSceneConfigBean);
                                break;
                            case "LIVE":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_LIVE);
                                rLiveFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                            case "H5_ATMOSPHERE":
                                currentPageId =
                                        mFragmentSparseArray.indexOfKey(FragmentName.PAGE_MUSIC);
                                rMusicFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                            case "WHITEBOARD":
                                currentPageId = mFragmentSparseArray.indexOfKey(FragmentName.PAGE_WHITEBOARD);
                                mWhiteboardFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                            case "BROWSER":
                                currentPageId = mFragmentSparseArray.indexOfKey(FragmentName.PAGE_SMART_BROWSER);
                                rSmartBrowserFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                                break;
                        }
                    } else {
                        currentPageId =
                                mFragmentSparseArray.indexOfKey(FragmentName.PAGE_DEFALUT);
                    }
                } else if (!TextUtils.isEmpty(mContentType) && mContentType.equals("h5")) {
                    currentPageId = mFragmentSparseArray.indexOfKey(FragmentName.PAGE_GAME);
                    rGameFragment.setFragmentData(mBusinessState, mSceneConfigBean);
                }
                mViewPager2.setCurrentItem(currentPageId, false);
            }
        });
    }

    private void setKeepScreenOn(boolean isScreenOn) {
        if (isScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //隐藏虚拟键盘
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    private void submitPanelShow(int tabId) {
        if (mSceneConfigBean != null && !TextUtils.isEmpty(mSceneConfigBean.id)) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put("applet_id", mSceneConfigBean.id);
            params.put("applet_name", mSceneConfigBean.appletName);

            if (tabId == 0) {
                params.put("tab_name", "当前内容");
            } else if (tabId == 1) {
                params.put("tab_name", "发言");
            } else if (tabId == 2)  {
                params.put("tab_name", "激光笔");
            } else if (tabId == 3) {
                params.put("tab_name", "控制中心");
            }
            GlobalEvent.onEvent("remote_panel_show", params);
        }
    }
}
