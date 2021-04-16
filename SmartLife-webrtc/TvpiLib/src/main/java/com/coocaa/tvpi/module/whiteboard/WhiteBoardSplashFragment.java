package com.coocaa.tvpi.module.whiteboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import swaiotos.runtime.base.AppletActivity;

public class WhiteBoardSplashFragment extends Fragment {

    View layout;
    private View defaultLayout;
//    private View continueLayout;
    private TextView tvShare;
    private TextView btnOpen;
//    private TextView btnContinueEdit;
    private TextView btnNew;

    final String TAG = "WBClient";

    protected AppletActivity.HeaderHandler mHeaderHandler;

    public void setHeaderHandler(AppletActivity.HeaderHandler handler) {
        this.mHeaderHandler = handler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(layout == null) {
            layout = inflater.inflate(R.layout.whiteboard_acitvity, null);
        }
        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initTitle();
        initView();
        initListener();
        openWhiteBoard();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initTitle() {
        StatusBarHelper.translucent(getActivity());
        StatusBarHelper.setStatusBarLightMode(getActivity());
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle("画板");
        }
    }

    private void initView() {
//        defaultLayout = layout.findViewById(R.id.whiteboard_default_layout);
//        continueLayout = layout.findViewById(R.id.whiteboard_continue_layout);
//        tvShare = layout.findViewById(R.id.whiteboard_share_txt);
//        btnOpen = layout.findViewById(R.id.whiteboard_open_btn);
//        btnContinueEdit = layout.findViewById(R.id.whiteboard_continue_edit_btn);
//        btnNew = layout.findViewById(R.id.whiteboard_new_btn);
    }

    private void initListener() {
        btnOpen.setOnClickListener(mOnClickListener);
//        btnContinueEdit.setOnClickListener(mOnClickListener);
        btnNew.setOnClickListener(mOnClickListener);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnOpen) {

            }  else if (v == btnNew) {

            }
        }
    };

    private void openWhiteBoard() {
        Log.d(TAG, "openWhiteBoard2222222");
    }
}
