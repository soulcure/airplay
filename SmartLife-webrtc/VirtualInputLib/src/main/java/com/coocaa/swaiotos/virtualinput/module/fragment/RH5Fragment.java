package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cocaa.swaiotos.virtualinput.R;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.core.os.exts.SW;

/**
 * @ClassName RH5Fragment
 * @Description 表情包/弹幕等全屏显示页面
 * @User heni
 * @Date 2020/12/17
 */
public class RH5Fragment extends Fragment {

    private String TAG = RH5Fragment.class.getSimpleName();
    private LinearLayout mLayout;
    private View mWebView;

    H5CoreOS h5CoreOS;
    private String mUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(R.layout.remote_h5_layout, container, false);
        return mLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        loadData();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    private void initView() {
        h5CoreOS = new H5CoreOS(H5RunType.RunType.MOBILE_RUNTYPE_ENUM, null);
        Map<String, H5CoreExt> extension = new HashMap<>();
        extension.put(SW.NAME, SW.get(getActivity()));
        mWebView = h5CoreOS.create(getActivity(), extension);
        h5CoreOS.setBackgroundColor(Color.TRANSPARENT);
        h5CoreOS.updateAppletNetType(H5RunType.RUNTIME_NETWORK_FORCE_LAN);

//        int width = DimensUtils.getDeviceWidth(getActivity()) - UiUtil.Div(20);
        LinearLayout.LayoutParams viewParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mLayout.addView(mWebView, viewParams);
    }

    public void setContentUrl(String contentUrl) {
        if(!TextUtils.isEmpty(contentUrl)){
            if(!contentUrl.equals(mUrl)) {
                mUrl = contentUrl;
                loadData();
            }
        }
    }

    /**
     * 表情包：https://webapp.skyworthiot.com/barrage/h5/#/emoji
     * 弹幕：https://webapp.skyworthiot.com/barrage/h5/#/text
     */
    private void loadData() {
        if (!TextUtils.isEmpty(mUrl) && mLayout != null) {
            h5CoreOS.load(mUrl);
        }
    }
}
