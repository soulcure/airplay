package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.coocaa.swaiotos.virtualinput.action.GlobalAction;
import com.coocaa.swaiotos.virtualinput.event.GlobalEvent;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.utils.VirtualInputUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import swaiotos.runtime.h5.core.os.H5RunType;

/**
 * @ClassName BaseLazyFragment
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/18
 */
public abstract class BaseLazyFragment extends Fragment {
    private String TAG = BaseLazyFragment.class.getSimpleName();
    private Context mContext;
    protected View stateControlView;
    protected RelativeLayout contentOne, contentTwo, contentOneLeft;
    protected TextView btnStop; //结束分享按钮，之前左右两部分的结构，缩放渐变的动画都不要了
    protected TextView btnStopPlay;
    protected TextView btnCancel;

    protected ImageView imgIcon;
    protected TextView tvTitle;
    protected TextView tvSubtitle;

    private boolean isFirstLoad = true; // 是否第一次加载
    protected BusinessState mStateBean;
    protected SceneConfigBean mSceneConfigBean;
    private String mPreType;
    private String mUserID = null;

    PropertyValuesHolder fadeOutAnim = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);
    PropertyValuesHolder fadeInAnim = PropertyValuesHolder.ofFloat("alpha", 0f, 1f);
    PropertyValuesHolder zoomInXAnim = PropertyValuesHolder.ofFloat("scaleX", 0.9f, 1f);
    PropertyValuesHolder zoomInYAnim = PropertyValuesHolder.ofFloat("scaleY", 0.9f, 1f);
    PropertyValuesHolder zoomOutXAnim = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.9f);
    PropertyValuesHolder zoomOutYAnim = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.9f);
    AnimatorSet showAnimSet;
    AnimatorSet hideAnimSet;
    private Map<String, String> params;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("HHH", "onCreateView: ");
        LinearLayout layout = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(layoutParams);

        stateControlView =
                LayoutInflater.from(mContext).inflate(R.layout.remote_state_control_layout, null);

        View view = getContentView();
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(getContentViewId(), null);
        }

        layout.addView(stateControlView);
        layout.addView(view);
        initStateView();
        initView(layout);
        initListener();
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        contentOne.setVisibility(View.VISIBLE);
        contentOne.setAlpha(1);
        contentTwo.setVisibility(View.GONE);

        if (isFirstLoad) {
            // 将数据加载逻辑放到onResume()方法中
            initData();
            initEvent();
            isFirstLoad = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserID = null;
        if (showAnimSet != null)
            showAnimSet.cancel();
        if (hideAnimSet != null) {
            hideAnimSet.cancel();
        }
    }

    private void initStateView() {
        contentOne = stateControlView.findViewById(R.id.content_one);
        contentOneLeft = stateControlView.findViewById(R.id.content_one_left_rl);
        contentTwo = stateControlView.findViewById(R.id.content_two);
        btnStop = stateControlView.findViewById(R.id.btn_stop);
        btnCancel = stateControlView.findViewById(R.id.btn_cancel);
        btnStopPlay = stateControlView.findViewById(R.id.btn_stop_paly);

        imgIcon = stateControlView.findViewById(R.id.img_type_icon);
        tvTitle = stateControlView.findViewById(R.id.tv_title);
        tvSubtitle = stateControlView.findViewById(R.id.tv_subtitle);
    }

    private void initListener() {
        btnStop.setOnClickListener(btnOnClickLis);
        btnCancel.setOnClickListener(btnOnClickLis);
        btnStopPlay.setOnClickListener(btnOnClickLis);
        contentOneLeft.setOnClickListener(btnOnClickLis);
    }

    View.OnClickListener btnOnClickLis = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btnStop) {
//                showStopPlayBtnAnim();
                VirtualInputUtils.playVibrate();
                sendStopCmd();
                submitClickEvent("结束播放", "当前内容");
            } else if (view == btnCancel) {
                hideStopPlayBtnAnim();
            } else if (view == btnStopPlay) {
                sendStopCmd();
                submitClickEvent("结束播放", "当前内容");
            } else if (view == contentOneLeft) {
                startApplet();
                if (mSceneConfigBean != null) {
                    submitClickEvent("打开小程序_" + mSceneConfigBean.appletName, "当前内容");
                }
            }
        }
    };

    /**
     * 设置布局资源id
     *
     * @return
     */
    protected abstract int getContentViewId();

    /**
     * 设置布局资源view
     *
     * @return
     */
    protected View getContentView() {
        return null;
    }

    /**
     * 初始化视图
     *
     * @param view
     */
    protected void initView(View view) {

    }

    /**
     * 初始化数据
     */
    protected void initData() {

    }

    /**
     * 初始化事件
     */
    protected void initEvent() {

    }

    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        if (stateBean != null && sceneConfigBean != null) {
            Log.d(TAG, "setFragmentData: businessState: " + BusinessState.encode(stateBean));
            Log.d(TAG, "setFragmentData: sceneBean: " + sceneConfigBean.toString());
        }
        mStateBean = stateBean;
        mSceneConfigBean = sceneConfigBean;
        updateUI();
    }

    private void updateUI() {
        if (mStateBean != null && mSceneConfigBean != null && stateControlView != null) {
            //新业务数据
            if (!TextUtils.isEmpty(mStateBean.id)) {
                Log.d(TAG, "updateUI: 新业务数据");
                //更新标题
                getUserId();
                if (mStateBean.owner != null) {
                    refreshTitle();
                } else {
                    tvTitle.setText(mSceneConfigBean.titleFormat);
                }
                //更新子标题
                refreshSubtitle();

                if (mPreType == null) {
                    loadImage();
                    mPreType = mStateBean.id;
                } else if (!mPreType.equals(mStateBean.id)) {
                    loadImage();
                    mPreType = mStateBean.id;
                }

            } else {
                Log.d(TAG, "updateUI: 旧业务数据");
                oldDataUpdateUI();
            }
        }
    }

    private void loadImage() {
        Log.d(TAG, "loadImage: load appletIcon");
        if (mSceneConfigBean != null) {
            if (TextUtils.isEmpty(mSceneConfigBean.appletIcon)) {
                imgIcon.setImageResource(R.drawable.remote_pager_default_type_icon);
                return;
            } else {
                try {
                    if (getActivity() != null && !getActivity().isDestroyed()) {
                        GlideApp.with(getActivity())
                                .load(mSceneConfigBean.appletIcon)
                                .error(R.drawable.remote_pager_default_type_icon)
                                .centerCrop()
                                .into(imgIcon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            imgIcon.setImageResource(R.drawable.remote_pager_default_type_icon);
        }
    }

    private void oldDataUpdateUI() {
        if (mStateBean != null) {
            switch (mStateBean.type.toUpperCase()) {
                case "VIDEO":
                    tvTitle.setText("正在使用「视频共享」");
                    imgIcon.setImageResource(R.drawable.vi_scene_title_icon_video);
                    break;
                case "IMAGE":
                    tvTitle.setText("正在使用「照片投屏」");
                    imgIcon.setImageResource(R.drawable.vi_scene_title_icon_picture);
                    break;
                case "DOC":
                    tvTitle.setText("正在使用「文档共享」");
                    imgIcon.setImageResource(R.drawable.vi_scene_title_icon_doc);
                    break;
                case "LIVE":
                    tvTitle.setText("正在使用「直播投屏」");
                    imgIcon.setImageResource(R.drawable.vi_scene_title_icon_live);
                    break;
                case "H5_ATMOSPHERE":
                    tvTitle.setText("正在使用「背景定制」");
                    imgIcon.setImageResource(R.drawable.vi_scene_title_icon_qifen);
                    break;
                default:
                    tvTitle.setText("共享屏无内容播放");
                    imgIcon.setImageResource(R.drawable.remote_pager_default_type_icon);
                    break;
            }
            tvSubtitle.setVisibility(View.GONE);
        }
    }

    private void sendStopCmd() {
        Log.d(TAG, "sendStopCmd: " + SmartApi.isSameWifi());
        if (!SmartApi.isSameWifi()) {
            SmartApi.startConnectSameWifi(H5RunType.RUNTIME_NETWORK_FORCE_LAN);
            return;
        }
        GlobalIOT.iot.sendKeyEvent(KeyEvent.KEYCODE_HOME, KeyEvent.ACTION_DOWN);
        ((Activity) mContext).finish();
    }

    private void startApplet() {
        //新老数据，打开小程序是一样的方式, h5 np也是一样的打开方式，所以不需要判断
        if (mSceneConfigBean != null) {
            if (!TextUtils.isEmpty(mSceneConfigBean.appletUri)) {
                GlobalAction.action.startActivity(getContext(), mSceneConfigBean.appletUri);
                ((Activity) mContext).finish();
            }
        }
    }

    private void refreshTitle() {
        String titleFormat = mSceneConfigBean.titleFormat;
        if(!TextUtils.isEmpty(titleFormat)){
            if (titleFormat.contains("%u")) {
                titleFormat = parseUser(titleFormat);
            }
            if (titleFormat.contains("%t")) {
                titleFormat = parseTitle(titleFormat);
            }
        }
        tvTitle.setText(titleFormat);
    }

    private void refreshSubtitle() {
        String sceneSubtitle = mSceneConfigBean.subTitle;
        if (TextUtils.isEmpty(sceneSubtitle)) {
            tvSubtitle.setVisibility(View.GONE);
        } else {
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(parseTitle(sceneSubtitle));
        }
    }

    private String parseUser(String targetString) {
        String titleFormat = targetString;
        String userID = mStateBean.owner.userID;
        String nickName = mStateBean.owner.nickName;
        String mobile = mStateBean.owner.mobile;
        Log.d(TAG, "refreshTitle: statebean_id: " + userID);
        Log.d(TAG, "refreshTitle: userinfo_id: " + mUserID);

        //userId相等显示[我]，不相等的情况下能拿到昵称优先显示昵称，拿不到昵称显示手机号
        if (!TextUtils.isEmpty(titleFormat) && titleFormat.contains("%u")) {
            if (!TextUtils.isEmpty(userID)) {
                if (userID.equals(mUserID)) {
                    titleFormat = titleFormat.replace("%u", "我");
                } else if (!TextUtils.isEmpty(nickName)) {
                    titleFormat = titleFormat.replace("%u", nickName + " ");
                } else if (!TextUtils.isEmpty(mobile)) {
                    titleFormat = titleFormat.replace("%u",
                            mobile.substring(0, 3) + "****" + mobile.substring(7) + " ");
                } else {
                    titleFormat = titleFormat.replace("u%", "");
                }
            } else {
                titleFormat = titleFormat.replace("u%", "");
            }
        }
        return titleFormat;
    }

    private String parseTitle(String targetString) {
        String titleFormat = targetString;
        if (!TextUtils.isEmpty(titleFormat) && titleFormat.contains("%t")) {
            String values = mStateBean.values;
            try {
                JSONObject jsonObject = new JSONObject(values);
                String title = jsonObject.optString("title");
                Log.d(TAG, "refreshTitle: " + title);
                if (!TextUtils.isEmpty(title)) {
                    titleFormat = titleFormat.replace("%t", title);
                } else {
                    titleFormat = titleFormat.replace("%t", "");
                }
            } catch (JSONException e) {
                titleFormat = titleFormat.replace("%t", "");
                e.printStackTrace();
            }
        }
        return titleFormat;
    }

    private void getUserId() {
        IUserInfo mCoocaaUserInfo = SmartApi.getUserInfo();
        if (mCoocaaUserInfo != null) {
            mUserID = mCoocaaUserInfo.open_id;
        }
    }

    /**
     * contentOne淡出同时contentTwo淡入,200ms之后btnStopPlay、btnCancel放大同时淡入
     */
    private void showStopPlayBtnAnim() {
        ObjectAnimator anim1 =
                ObjectAnimator.ofPropertyValuesHolder(contentOne, fadeOutAnim).setDuration(300);
        ObjectAnimator anim2 =
                ObjectAnimator.ofPropertyValuesHolder(contentTwo, fadeInAnim).setDuration(300);
        ObjectAnimator anim3 = ObjectAnimator.ofPropertyValuesHolder(btnStopPlay, fadeInAnim,
                zoomInXAnim, zoomInYAnim).setDuration(300);
        ObjectAnimator anim4 = ObjectAnimator.ofPropertyValuesHolder(btnCancel, fadeInAnim,
                zoomInXAnim, zoomInYAnim).setDuration(300);

        if (showAnimSet == null) {
            showAnimSet = new AnimatorSet();
            showAnimSet.play(anim1).with(anim2).after(200).with(anim3).with(anim4);
            showAnimSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    contentOne.setVisibility(View.GONE);
                    contentTwo.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    contentTwo.setVisibility(View.VISIBLE);
                    contentTwo.setAlpha(0);
                }
            });
        }
        showAnimSet.start();
    }

    /**
     * btnStopPlay、btnCancel缩小淡出，200ms之后 contentTwo淡出同时contentOne淡入
     */
    private void hideStopPlayBtnAnim() {
        ObjectAnimator anim1 =
                ObjectAnimator.ofPropertyValuesHolder(contentOne, fadeInAnim).setDuration(300);
        ObjectAnimator anim2 =
                ObjectAnimator.ofPropertyValuesHolder(contentTwo, fadeOutAnim).setDuration(300);
        ObjectAnimator anim3 = ObjectAnimator.ofPropertyValuesHolder(btnStopPlay, fadeOutAnim,
                zoomOutXAnim, zoomOutYAnim).setDuration(300);
        ObjectAnimator anim4 = ObjectAnimator.ofPropertyValuesHolder(btnCancel, fadeOutAnim,
                zoomOutXAnim, zoomOutYAnim).setDuration(300);

        if (hideAnimSet == null) {
            hideAnimSet = new AnimatorSet();
            hideAnimSet.play(anim3).with(anim4).after(200).with(anim1).with(anim2);
            hideAnimSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    contentOne.setVisibility(View.VISIBLE);
                    contentTwo.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    contentOne.setVisibility(View.VISIBLE);
                    contentOne.setAlpha(0);
                }
            });
        }
        hideAnimSet.start();
    }

    protected void submitClickEvent(String btnName, String tabName) {
        if (mSceneConfigBean != null) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.put("applet_id", mSceneConfigBean.id);
            params.put("applet_name", mSceneConfigBean.appletName);
            params.put("btn_name", btnName);
            params.put("tab_name", tabName);
            GlobalEvent.onEvent("remote_btn_clicked", params);
        }
    }

}
