package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.swaiotos.virtualinput.iot.AtmosphereData;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class RMusicFragment extends BaseLazyFragment {

    private static final long VIBRATE_DURATION = 100L;

    private View mView;
    private ImageView imgMusicPauseAndPlay;
    private ImageView imgCircleOut;
    private ImageView imgCircleIn;
    private TextView tvMusicState;
    private RelativeLayout ryMusicContent;
    private ObjectAnimator musicLogoRotate;
    private boolean isPlay = false;

    private Animation scaleAnimationOut;
    private Animation scaleAnimationIn;

    private String owner;
    //业务发起者
    BusinessState mBusinessState;
    private boolean vibrate;
    private String type;

    @Override
    protected int getContentViewId() {
        return R.layout.vi_music_scene_layout;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mView = view;
        ryMusicContent = view.findViewById(R.id.vi_music_content_layout);
        imgMusicPauseAndPlay = view.findViewById(R.id.music_pause_play_img);
        tvMusicState = view.findViewById(R.id.tv_music_state);
        imgCircleIn = view.findViewById(R.id.music_circle_in);
        imgCircleOut = view.findViewById(R.id.music_circle_out);
        vibrate = SpUtil.getBoolean(getContext(), SpUtil.Keys.REMOTE_VIBRATE, true);
        ryMusicContent.setVisibility(View.INVISIBLE);
        ryMusicContent.setEnabled(false);
        initAnimator();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HomeUIThread.removeTask(circleOutRunnable);
        HomeUIThread.removeTask(circleInRunnable);
    }

    private void initAnimator() {
        musicLogoRotate = ObjectAnimator.ofFloat(imgMusicPauseAndPlay, "rotation", 0, 360).setDuration(3000);
        musicLogoRotate.setRepeatCount(-1);
        musicLogoRotate.setInterpolator(new LinearInterpolator());
        musicLogoRotate.start();
        musicLogoRotate.pause();
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        ryMusicContent.setOnClickListener(musicClickListener);
    }

    private void musicPlayAndPause() {
        playVibrate();
        if (isPlay) {
            sendCmd("pause");
        } else {
            sendCmd("play");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateAtmosphereUI();
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        mBusinessState = stateBean;
        if (sceneConfigBean != null) {
            if (sceneConfigBean.contentUrl != null) {
                type = sceneConfigBean.contentUrl;
            }
        }
        updateAtmosphereUI();
    }

    private void updateAtmosphereUI() {
        if (mBusinessState != null && mView != null) {
            AtmosphereData atmosphereData = null;
            try {
                Log.d("chen", "setFragmentData: " + mBusinessState.values);
                atmosphereData = new Gson().fromJson(mBusinessState.values, AtmosphereData.class);
                Log.d("chen", "setFragmentData: " + atmosphereData.getPlayCmd());
            } catch (Exception ignore) {

            }
            if (atmosphereData == null) {
                return;
            }

            if (mBusinessState.owner != null) {
                owner = User.encode(mBusinessState.owner);
            }


            if ("play".equals(atmosphereData.getPlayCmd())) {
                ryMusicContent.setVisibility(View.VISIBLE);
                ryMusicContent.setEnabled(true);
                isPlay = true;
                tvMusicState.setText(R.string.vi_music_play);
                animationMusicPlay();
            } else if ("pause".equals(atmosphereData.getPlayCmd())) {
                ryMusicContent.setVisibility(View.VISIBLE);
                ryMusicContent.setEnabled(true);
                isPlay = false;
                tvMusicState.setText(R.string.vi_music_pause);
                animationMuiscPause();
            } else if ("none".equals(atmosphereData.getPlayCmd())) {
                ryMusicContent.setVisibility(View.INVISIBLE);
                ryMusicContent.setEnabled(false);
            } else {
                ryMusicContent.setVisibility(View.INVISIBLE);
                ryMusicContent.setEnabled(false);
            }
        }
    }

    private void animationMusicPlay() {
        if (musicLogoRotate != null && musicLogoRotate.isRunning()) {
            musicLogoRotate.resume();
        }
        if (scaleAnimationIn == null) {
            scaleAnimationIn = AnimationUtils.loadAnimation(getContext(), R.anim.music_circle_scale);
            HomeUIThread.execute(1000, circleInRunnable);
        }
        if (scaleAnimationOut == null) {
            scaleAnimationOut = AnimationUtils.loadAnimation(getContext(), R.anim.music_circle_scale);
            HomeUIThread.execute(circleOutRunnable);
        }
    }

    private void animationMuiscPause() {
        if (musicLogoRotate != null) {
            musicLogoRotate.pause();
        }
        if (scaleAnimationOut != null) {
            scaleAnimationOut.cancel();
        }
        if (scaleAnimationIn != null) {
            scaleAnimationIn.cancel();
        }
        HomeUIThread.removeTask(circleOutRunnable);
        HomeUIThread.removeTask(circleInRunnable);
        scaleAnimationOut = null;
        scaleAnimationIn = null;
    }

    protected void sendCmd(String cmd) {
        if (type == null) {
            return;
        }
        if ("H5_ATMOSPHERE".equals(type)) {
            GlobalIOT.iot.sendCmd(cmd, "H5_ATMOSPHERE", "", "client-runtime-h5", owner);
        }
        if ("AUDIO".equals(type)) {
            GlobalIOT.iot.sendCmd(cmd, "audio", "", "ss-clientID-UniversalMediaPlayer", owner);
        }
    }

    private void playVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    private final View.OnClickListener musicClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            musicPlayAndPause();
        }
    };

    private final Runnable circleOutRunnable = new Runnable() {
        @Override
        public void run() {
            imgCircleOut.startAnimation(scaleAnimationOut);
            if (isPlay) {
                HomeUIThread.execute(3000, circleOutRunnable);
            }
        }
    };

    private final Runnable circleInRunnable = new Runnable() {
        @Override
        public void run() {
            imgCircleIn.startAnimation(scaleAnimationIn);
            if (isPlay) {
                HomeUIThread.execute(3000, circleInRunnable);
            }
        }
    };
}
