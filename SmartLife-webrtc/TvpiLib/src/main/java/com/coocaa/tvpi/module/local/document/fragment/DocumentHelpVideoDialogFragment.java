package com.coocaa.tvpi.module.local.document.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.utils.SpUtil;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.local.document.DocumentConfig;
import com.coocaa.tvpi.module.local.document.DocumentResManager;
import com.coocaa.tvpi.module.local.view.LocalVideoPlayerView;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.ThirdAppLaunch;
import com.coocaa.tvpilib.R;


/**
 * @ClassName ConnectDialogFragment
 * @Description 文档帮助视频弹框
 * @User luoxi
 * @Date 2020-12-2
 * @Version TODO (write something)
 */
public class DocumentHelpVideoDialogFragment extends BottomBaseDialogFragment {
    private final static String TAG = "DocumentHelpVideo";
    private final static String SP_KEY_FIRST_PLAY = "doc_video_first_paly";
    private LocalVideoPlayerView videoPlayerView;
    private ImageView iv_loading;
    private TextView title;
    private TextView jumpBtn;
    private DocumentConfig.Source mSource;

    public DocumentHelpVideoDialogFragment(AppCompatActivity mActivity, DocumentConfig.Source source) {
        super(mActivity);
        mSource = source;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.source_help_video_dialog_layout, container, false);
    }

    @Override
    protected void initViews(View view) {
        view.findViewById(R.id.main_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
        View item_parent = view.findViewById(R.id.item_parent);
        item_parent.setPadding(0, 0, 0, getNavigationBarHeight(mActivity) + item_parent.getPaddingBottom());
        iv_loading = view.findViewById(R.id.iv_loading);
        title = view.findViewById(R.id.title);
        jumpBtn = view.findViewById(R.id.btn_jump);
        jumpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DocumentConfig.Source.WEIXIN == mSource) {
                    ThirdAppLaunch.startWechat(mActivity);
                } else if (DocumentConfig.Source.QQ == mSource) {
                    ThirdAppLaunch.startQQ(mActivity);
                }
                dismissDialog();
            }
        });
        refreshText();
        videoPlayerView = view.findViewById(R.id.local_video_view);
//        String uri = "android.resource://" + getPackageName() + "/" + R.raw.document_help_video2;
        loadUrl();
    }

    private void loadUrl() {
        String url = "";
        if (DocumentConfig.Source.WEIXIN == mSource) {
            url = DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_WECHAT);
        } else if (DocumentConfig.Source.QQ == mSource) {
            url = DocumentResManager.getInstance().getVideoPath(DocumentResManager.VIDEO_TYPE_QQ);
        }
        Log.i(TAG, "loadUrl --> getVideoPath:" + url);
        if (TextUtils.isEmpty(url) || url.startsWith("http") || url.startsWith("https")) {
//            boolean isFirst = SpUtil.getBoolean(mActivity, SP_KEY_FIRST_PLAY, true);
//            if (isFirst) {
//                jumpBtn.setVisibility(View.INVISIBLE);
//            } else {
//                jumpBtn.setVisibility(View.VISIBLE);
//            }
            startLoading();
            videoPlayerView.setVisibility(View.INVISIBLE);
            if (NetworkUtil.isConnected(mActivity)) {
                DocumentResManager.getInstance().setVideoResDownloadListener(new DocumentResManager.VideoResDownloadListener() {
                    @Override
                    public void onSuccess(final String url) {
                        Log.i(TAG, "onSuccess: " + url);
                        HomeUIThread.execute(new Runnable() {
                            @Override
                            public void run() {
                                play(url);
                            }
                        });
                    }

                    @Override
                    public void onFailed(String msg) {
                        Log.i(TAG, "onFailed: " + msg);
                    }

                    @Override
                    public String getVideoType() {
                        if (DocumentConfig.Source.WEIXIN == mSource) {
                            return DocumentResManager.VIDEO_TYPE_WECHAT;
                        } else if (DocumentConfig.Source.QQ == mSource) {
                            return DocumentResManager.VIDEO_TYPE_QQ;
                        }
                        return "";
                    }
                });
                DocumentResManager.getInstance().init(mActivity.getApplicationContext());
            } else {
                ToastUtils.getInstance().showGlobalShort("网络未连接");
            }
        } else {
            play(url);
        }
    }

    private void play(String url) {
        stopLoading();
        jumpBtn.setVisibility(View.VISIBLE);
        videoPlayerView.setVisibility(View.VISIBLE);
        videoPlayerView.setVideoURI(Uri.parse(url));
        videoPlayerView.startPlay();
        videoPlayerView.setPlayerListener(new LocalVideoPlayerView.PlayerListener() {
            @Override
            public void onFinish() {
                SpUtil.putBoolean(mActivity, SP_KEY_FIRST_PLAY, false);
//                jumpBtn.setClickable(true);
//                jumpBtn.getBackground().setAlpha(255);
            }
        });
    }

    private void refreshText() {
        if (mSource == null) return;
        title.setText(mSource.text + "文档共享教程");
        jumpBtn.setText("跳转至「" + mSource.text + "」选择文档");
//        boolean isFirst = SpUtil.getBoolean(mActivity, SP_KEY_FIRST_PLAY, true);
//        if (isFirst) {
//            jumpBtn.setClickable(false);
//            jumpBtn.getBackground().setAlpha(125);
//        } else {
//            jumpBtn.setClickable(true);
//            jumpBtn.getBackground().setAlpha(255);
//        }
    }

    private void startLoading() {
        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(2000);//设置动画持续周期
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间
        iv_loading.setAnimation(rotate);
    }

    private void stopLoading() {
        iv_loading.clearAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        videoPlayerView.stopPlay();
        dismissDialog();
    }

    @Override
    public void onStop() {
        super.onStop();
        videoPlayerView.stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLoading();
    }
}
