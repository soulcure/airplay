package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.swaiotos.virtualinput.action.GlobalAction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @ClassName RDefaultFragment
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/17
 */
public class RDefaultFragment extends BaseLazyFragment {

    private String TAG = RDefaultFragment.class.getSimpleName();

    @Override
    protected int getContentViewId() {
        return R.layout.remote_default_fragment;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        btnStop.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateDefaultUI();
    }

    private void updateDefaultUI() {
        if (getContext() != null) {
            if (stateControlView != null) {
                tvTitle.setText("共享屏无内容播放");
                tvTitle.setTextColor(getResources().getColor(R.color.color_white_40));
                tvSubtitle.setVisibility(View.GONE);
                imgIcon.setImageResource(R.drawable.remote_pager_default_type_icon);
            }
        }
    }
}
