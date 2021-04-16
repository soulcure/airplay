package com.coocaa.tvpi.module.local.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.cardview.widget.CardView;

import com.coocaa.tvpilib.R;

public class LocalVideoPlayerView extends RelativeLayout {
    private VideoView videoView;
    private View bg_play;
    private View pause_layout;
    private View replay_layout;
    private ImageView preview_img;
    private CardView cardView;
    private PlayerListener mPlayerListener;

    public LocalVideoPlayerView(Context context) {
        this(context, null, 0);
    }

    public LocalVideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocalVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void setFullScreen() {
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        videoView.setLayoutParams(layoutParams);
    }

    public void setPlayerListener(PlayerListener playerListener) {
        mPlayerListener = playerListener;
    }

    public void setVideoURI(Uri uri) {
        if (videoView != null) {
            videoView.setVideoURI(uri);
        }
    }

    public void setCornerRadius(int radius) {
        cardView.setRadius(radius);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_local_video_player_view, this);
        videoView = this.findViewById(R.id.video_view);
        cardView = this.findViewById(R.id.card_layout);
        pause_layout = this.findViewById(R.id.pause_layout);
        replay_layout = this.findViewById(R.id.replay_layout);
        preview_img = this.findViewById(R.id.preview_img);
        bg_play = this.findViewById(R.id.bg_play);
        setFullScreen();
        replay_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });
        pause_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoView.isPlaying()) {
                    startPlay();
                }
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setLooping(true);
                pausePlay();
                mp.setLooping(false);
                pause_layout.setVisibility(GONE);
                replay_layout.setVisibility(VISIBLE);
                if (mPlayerListener != null) {
                    mPlayerListener.onFinish();
                }
            }
        });
        bg_play.setVisibility(VISIBLE);
        pause_layout.setVisibility(VISIBLE);
        replay_layout.setVisibility(GONE);
        preview_img.setVisibility(GONE);
    }

    public void setPreviewImg(Bitmap bitmap) {
        preview_img.setVisibility(VISIBLE);
        preview_img.setImageBitmap(bitmap);
    }

    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    public void pausePlay() {
        videoView.pause();
        pause_layout.setVisibility(VISIBLE);
        bg_play.setVisibility(VISIBLE);
    }

    public void startPlay() {
        videoView.start();
        pause_layout.setVisibility(GONE);
        replay_layout.setVisibility(GONE);
        bg_play.setVisibility(GONE);
        preview_img.setVisibility(GONE);
    }

    public void stopPlay() {
        videoView.stopPlayback();
//        pause_layout.setVisibility(GONE);
//        replay_layout.setVisibility(VISIBLE);
//        bg_play.setVisibility(VISIBLE);
    }

    public interface PlayerListener {
        void onFinish();
    }
}
