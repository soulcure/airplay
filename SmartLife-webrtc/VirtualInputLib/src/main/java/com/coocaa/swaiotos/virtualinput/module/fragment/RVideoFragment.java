package com.coocaa.swaiotos.virtualinput.module.fragment;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cocaa.swaiotos.virtualinput.R;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.smartscreen.businessstate.object.BusinessState;
import com.coocaa.smartscreen.businessstate.object.User;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.businessstate.SceneConfigBean;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.swaiotos.virtualinput.iot.VideoStateData;
import com.coocaa.swaiotos.virtualinput.utils.MediaTimeUtils;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.google.gson.Gson;

import java.util.concurrent.atomic.AtomicInteger;

public class RVideoFragment extends BaseLazyFragment {

    private static final long VIBRATE_DURATION = 100L;

    private View mView;
    private SeekBar videoSeekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalTime;
    private ImageView imgVideoBackWard;
    private ImageView imgVideoForward;
    private ImageView imgVideoPlay;
    private RelativeLayout videoLayout;
    private ImageView volumeAdd;
    private ImageView volumeReduce;
    private TextView mute;

    private long mCurrentMusicTime;
    private long mTotalTime = 23000000;
    private long mSeekMusicTime;

    private boolean isPlay = true;
    private boolean vibrate;
    private String owner; //业务发起者
    private AtomicInteger waitCount = new AtomicInteger(0);

    @Override
    protected int getContentViewId() {
        return R.layout.vi_video_scene_layout2;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        mView = view;
        videoLayout = view.findViewById(R.id.remote_video_layout);
        videoSeekBar = view.findViewById(R.id.video_seek_bar);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvTotalTime = view.findViewById(R.id.tv_total_time);
        imgVideoBackWard = view.findViewById(R.id.video_backward_img);
        imgVideoForward = view.findViewById(R.id.video_forward_img);
        imgVideoPlay = view.findViewById(R.id.video_play_pause_img);

        volumeAdd = view.findViewById(R.id.volume_add_img);
        volumeReduce = view.findViewById(R.id.volume_subtract_img);
        mute = view.findViewById(R.id.mute_img);
        vibrate = SpUtil.getBoolean(getContext(), SpUtil.Keys.REMOTE_VIBRATE, true);
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        imgVideoPlay.setOnClickListener(clickListener);
        imgVideoForward.setOnClickListener(clickListener);
        imgVideoBackWard.setOnClickListener(clickListener);
        volumeAdd.setOnClickListener(volumeClickListener);
        volumeReduce.setOnClickListener(volumeClickListener);
        mute.setOnClickListener(volumeClickListener);

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mSeekMusicTime = mTotalTime * progress / 100;
                sendCmd("seek", String.valueOf(mSeekMusicTime));
                HomeIOThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        waitCount.set(3);
                    }
                });
            }
        });
    }

    @Override
    public void setFragmentData(BusinessState stateBean, SceneConfigBean sceneConfigBean) {
        super.setFragmentData(stateBean, sceneConfigBean);
        if (stateBean == null || mView == null) {
            return;
        }
        if (!"VIDEO".equals(sceneConfigBean.contentUrl.toUpperCase())) {
            videoLayout.setVisibility(View.GONE);
            return;
        }
        if (stateBean.owner != null) {
            owner = User.encode(stateBean.owner);
        }
        VideoStateData mediaState = null;
        try {
            Log.d("hh", "setFragmentData: " + stateBean.values);
            mediaState = new Gson().fromJson(stateBean.values, VideoStateData.class);
            Log.d("hh", "setFragmentData: " + mediaState.toString());
        } catch (Exception ignore) {
        }


        if (mediaState != null) {
            if (mediaState.getMediaCurrent() != null) {
                mCurrentMusicTime = Long.parseLong(mediaState.getMediaCurrent());
            }
            if (mediaState.getMediaTime() != null) {
                mTotalTime = Long.parseLong(mediaState.getMediaTime());
            }
            if ("play".equals(mediaState.getPlayCmd())) {
                isPlay = true;
                imgVideoPlay.setImageResource(R.drawable.vi_video_play_selector);
            } else {
                isPlay = false;
                imgVideoPlay.setImageResource(R.drawable.vi_video_pause_selector);
            }
            tvCurrentTime.setText(MediaTimeUtils.stringForTime(mCurrentMusicTime));
            tvTotalTime.setText(MediaTimeUtils.stringForTime(mTotalTime));
            if (waitCount.get() > 0) {
                waitCount.getAndDecrement();
                return;
            }
            int progress = (int) (mCurrentMusicTime / (double) mTotalTime * 100);
            videoSeekBar.setProgress(progress);
        }
    }

    private void videoForward() {
        mCurrentMusicTime = mCurrentMusicTime + 10 * 1000;
        if (mCurrentMusicTime > mTotalTime) {
            mCurrentMusicTime = mTotalTime;
        }
        sendCmd("seek", mCurrentMusicTime + "");
    }

    private void videoBackWard() {
        mCurrentMusicTime = mCurrentMusicTime - 10 * 1000;
        if (mCurrentMusicTime < 0) {
            mCurrentMusicTime = 0;
        }
        sendCmd("seek", mCurrentMusicTime + "");
    }

    private void videoPlayAndPause() {
        if (isPlay) {
            isPlay = false;
            sendCmd("pause", "");
        } else {
            isPlay = true;
            sendCmd("play", "");
        }
    }

    private void playVibrate() {
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    protected void sendCmd(String cmd, String param) {
        playVibrate();
        GlobalIOT.iot.sendCmd(cmd, "video", param, targetClientId(), owner);
    }

    protected String targetClientId() {
        return SSConnectManager.TARGET_CLIENT_MEDIA_PLAYER;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.video_play_pause_img) {
                videoPlayAndPause();
            } else if (v.getId() == R.id.video_backward_img) {
                videoBackWard();
            } else if (v.getId() == R.id.video_forward_img) {
                videoForward();
            }
        }
    };

    private View.OnClickListener volumeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == volumeReduce) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_DOWN);
            } else if (v == volumeAdd) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_UP);
            } else if (v == mute) {
                sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
            }
        }
    };

    private void sendKeyEvent(int keyCode) {
        playVibrate();
        GlobalIOT.iot.sendKeyEvent(keyCode, KeyEvent.ACTION_DOWN);
    }

}
