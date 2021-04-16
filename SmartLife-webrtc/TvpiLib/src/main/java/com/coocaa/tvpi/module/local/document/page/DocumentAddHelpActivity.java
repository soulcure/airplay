package com.coocaa.tvpi.module.local.document.page;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.local.adapter.DocumentHelpTitleAdapter;
import com.coocaa.tvpi.module.local.document.DocumentResManager;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.view.LocalVideoPlayerView;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 文档帮助-如何添加文档
 * @Author: wzh
 * @CreateDate: 3/30/21
 */
public class DocumentAddHelpActivity extends BaseActivity {

    private ScrollView mScrollView;
    private ImageView mIvIntro;
    private RecyclerView mRecyclerView;
    private DocumentHelpTitleAdapter mAdapter;
    private LocalVideoPlayerView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_add_help);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        mIvIntro = findViewById(R.id.iv_intro);
        mScrollView = findViewById(R.id.scroll_view);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (DocumentUtil.isAndroidR()) {
            mIvIntro.setImageResource(R.drawable.doc_add_help_intro2);
        } else {
            mIvIntro.setImageResource(R.drawable.doc_add_help_intro1);
        }
        initVideoView();
        initRecyclerView();
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.scrollTo(0, 0);
            }
        }, 60);
    }

    private void initVideoView() {
        mVideoView = findViewById(R.id.local_video_view);
        String url = DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_WECHAT);
        if (TextUtils.isEmpty(url)) {
            DocumentResManager.getInstance().setVideoResDownloadListener(new DocumentResManager.VideoResDownloadListener() {
                @Override
                public void onSuccess(String url) {
                    playVideo(url);
                }

                @Override
                public void onFailed(String msg) {

                }

                @Override
                public String getVideoType() {
                    return DocumentResManager.VIDEO_TYPE_WECHAT;
                }
            });
            DocumentResManager.getInstance().init(this);
        } else {
            playVideo(url);
        }
    }

    private void initImage(String url) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(url);
        Bitmap bitmap = mmr.getFrameAtTime();//获取第一帧图片
        mVideoView.setPreviewImg(bitmap);
        mmr.release();//释放资源
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new CommonHorizontalItemDecoration(DimensUtils.dp2Px(this, 18f)));
        mAdapter = new DocumentHelpTitleAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setList(getData());
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                mAdapter.notifyDataSetChanged();
                switch (position) {
                    case 0:
                        playVideo(DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_WECHAT));
                        break;
                    case 1:
                        playVideo(DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_WEIXINWORK));
                        break;
                    case 2:
                        playVideo(DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_DINGDING));
                        break;
                    case 3:
                        playVideo(DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_QQ));
                        break;
                }
            }
        });
    }

    private void playVideo(String url) {
        if (mVideoView.isPlaying()) {
            mVideoView.stopPlay();
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mVideoView.pausePlay();
        mVideoView.setVideoURI(Uri.parse(url));
        initImage(url);
//        mVideoView.startPlay();
    }

    private List<String> getData() {
        List<String> datas = new ArrayList<>();
        datas.add("微信");
        datas.add("企业微信");
        datas.add("钉钉");
        datas.add("QQ");
        return datas;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pausePlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlay();
    }
}
