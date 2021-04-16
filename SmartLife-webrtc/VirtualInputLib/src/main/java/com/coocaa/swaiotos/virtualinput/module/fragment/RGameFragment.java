package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.core.os.exts.SW;

/**
 * @ClassName RGameFragment
 * @Description 游戏/芝士视频/影视小程序...等等通用的webview页面
 * @User heni
 * @Date 2020/12/23
 */
public class RGameFragment extends BaseLazyFragment {

    private String TAG = RGameFragment.class.getSimpleName();
    private H5CoreOS h5CoreOS;
    private BusinessState mBusinessState;
    private SceneConfigBean mSceneConfigBean;
    private String urlPre = "";

    @Override
    protected int getContentViewId() {
        return 0;
    }

    @Override
    protected View getContentView() {
        LinearLayout rootLayout = new LinearLayout(getActivity());
        ViewGroup.LayoutParams layoutParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(layoutParams);
        rootLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        h5CoreOS = new H5CoreOS(H5RunType.RunType.MOBILE_RUNTYPE_ENUM, null);
        Map<String, H5CoreExt> extension = new HashMap<>();
        extension.put(SW.NAME, SW.get(getActivity()));

        View mView = h5CoreOS.create(getActivity(), extension);
        h5CoreOS.setBackgroundColor(Color.TRANSPARENT);

        int padding = (int) getResources().getDimension(R.dimen.global_horizontal_margin_10);
        LinearLayout.LayoutParams viewParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        // viewParams.setMargins(padding, 0, padding, 0);
        rootLayout.addView(mView, viewParams);
        return rootLayout;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateUI();
    }

    @Override
    protected void initEvent() {
        super.initEvent();
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (h5CoreOS != null)
            h5CoreOS.destroy();
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        mBusinessState = stateBean;
        mSceneConfigBean = sceneConfigBean;
        if(mSceneConfigBean != null && mSceneConfigBean.contentUrl != null && !mSceneConfigBean.contentUrl.equals(urlPre)){
            updateUI();
        }

        if (h5CoreOS != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("event", "onBusinessState");
            map.put("state", stateBean);
            String data = JSON.toJSONString(map);
            Log.d(TAG, "setFragmentData() called with: data = [" + data + "], sceneConfigBean = [" + sceneConfigBean + "]");
            h5CoreOS.evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
        }
    }

    private void updateUI() {
        if (mBusinessState != null && mSceneConfigBean != null && stateControlView != null) {
            if ("h5".equals(mSceneConfigBean.contentType)) {
                h5CoreOS.load(mSceneConfigBean.contentUrl);
                urlPre = mSceneConfigBean.contentUrl;
            }
        }
    }
}
