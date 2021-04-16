package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.swaiotos.virtualinput.action.GlobalAction;
import com.coocaa.swaiotos.virtualinput.data.WhiteboardData;
import com.coocaa.swaiotos.virtualinput.data.WhiteboardUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import swaiotos.sensor.data.AccountInfo;

/**
 * @ClassName RWhiteboardFragment
 * @Description TODO (write something)
 * @User heni
 * @Date 3/30/21
 */
public class RWhiteboardFragment extends BaseLazyFragment {

    private View mView;
    private TextView tvNum;
    private TextView tvNames;
    private TextView btnOpen;
    private BusinessState mBusinessState;
    private SceneConfigBean mSceneConfigBean;
    private List<AccountInfo> mUserList = null;

    private WhiteboardData mWhiteboardData;
    private WhiteboardUser mWhiteboardUser;
    private StringBuffer nameString;

    @Override
    protected int getContentViewId() {
        return R.layout.remote_whiteboard_fragment;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        tvNum = view.findViewById(R.id.tv_num);
        tvNames = view.findViewById(R.id.tv_names);
        btnOpen = view.findViewById(R.id.btn_open_whiteboard);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhiteboard();
            }
        });
    }

    private void openWhiteboard() {
        if (getContext() != null) {
            GlobalAction.action.startActivity(getContext(), "np://com.coocaa.smart.whiteboard/index");
            ((Activity) getContext()).finish();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mUserList = new ArrayList<>();
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        mBusinessState = stateBean;
        mSceneConfigBean = sceneConfigBean;
        updateUI();
    }

    private void updateUI() {
        if (mView != null && mBusinessState != null && mBusinessState.values != null) {
            mWhiteboardData = BaseData.load(mBusinessState.values, WhiteboardData.class);
            if (mWhiteboardData != null && mWhiteboardData.owner != null) {
                mWhiteboardUser = BaseData.load(mWhiteboardData.owner, WhiteboardUser.class);
                if (mWhiteboardUser != null) {
                    mUserList = mWhiteboardUser.userList;
                    if (mUserList != null && mUserList.size() > 0) {
                        tvNum.setText(mUserList.size() + "人正在使用画板");
                        if (nameString == null) {
                            nameString = new StringBuffer();
                        } else {
                            nameString.setLength(0);
                        }

                        for (AccountInfo accountInfo : mUserList) {
                            Log.d("heni", "updateUI: " + new Gson().toJson(accountInfo));
                            if (accountInfo.nickName != null) {
                                nameString.append(accountInfo.nickName);
                                nameString.append("、");
                            } else if (accountInfo.mobile != null) {
                                nameString.append(accountInfo.mobile);
                                nameString.append("、");
                            }
                        }
                        nameString.deleteCharAt(nameString.length() - 1);
                        tvNames.setText(nameString);
                        return;
                    }
                }
            }
            tvNum.setText("");
            tvNames.setText("");
        }
    }

}
