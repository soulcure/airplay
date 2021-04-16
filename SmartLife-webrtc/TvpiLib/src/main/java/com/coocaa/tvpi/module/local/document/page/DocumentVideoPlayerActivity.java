package com.coocaa.tvpi.module.local.document.page;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.tvpi.module.local.view.LocalVideoPlayerView;
import com.coocaa.tvpilib.R;


/**
 * @Description: 文档播放页面
 * @Author: luoxi
 * @CreateDate: 2020/12/11
 */
public class DocumentVideoPlayerActivity extends BaseAppletActivity {
    private LocalVideoPlayerView local_video_view;
    private final static String KEY_VIDEO_SOURCE = "video_source";

    public static void start(Context context, int rawId) {
        Intent intent = new Intent(context, DocumentVideoPlayerActivity.class);
        intent.putExtra(KEY_VIDEO_SOURCE, rawId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_video_player);
        if (mHeaderHandler != null) {
            mHeaderHandler.setHeaderVisible(false);
        }
        initViews();
    }

    protected void initViews() {
        local_video_view = findViewById(R.id.local_video_view);
        initPlayer(getIntent());
    }
    private void initPlayer(Intent intent){
        int videoSource = intent.getIntExtra(KEY_VIDEO_SOURCE, -1);
        if (videoSource == -1) return;
        String uri = "android.resource://" + getPackageName() + "/" + videoSource;
        local_video_view.setVideoURI(Uri.parse(uri));
        local_video_view.startPlay();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initPlayer(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        local_video_view.pausePlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        local_video_view.stopPlay();
    }
}
