package com.coocaa.tvpi.module.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.local.album.AlbumViewPager;
import com.coocaa.tvpi.module.local.album.OnPullProgressListener;
import com.coocaa.tvpi.module.local.album.VideoPreviewAdapter;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
import com.coocaa.tvpi.module.local.utils.VideoBrowseAsyncTask;
import com.coocaa.tvpilib.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoFragment extends Fragment {
    private String TAG = VideoFragment.class.getSimpleName();

    private static final String INTENT_INDEX = "extra_index";
    private static final int WHITE = 0xFFFFFFFF;
    private static final int PAGE_MARGIN = 30;

    private static Context mContext;
    private AlbumViewPager mAlbumView;
    private VideoPreviewAdapter mAdapter;

    private List<VideoData> videoDataList = new ArrayList<>();
    private int mCurrIndex = 0;

    private OnAlbumEventListener mListener;
    private VideoBrowseAsyncTask mVideoBrowseAsyncTask;

    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private LinearLayout surfaceLayout;
    private boolean fromShare = false;

    public static VideoFragment newInstance(int index, Context context) {
        mContext = context;
        VideoFragment fragment = new VideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_INDEX, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setShareVideoList(List<VideoData> videoList) {
        if(videoList != null && !videoList.isEmpty()) {
            videoDataList.addAll(videoList);
            fromShare = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            mCurrIndex = extras.getInt(INTENT_INDEX, 0);
            Log.d(TAG, "onCreate: mCurrIndex" + mCurrIndex);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(mContext);
        LinearLayout.LayoutParams albumParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        albumParams.gravity = Gravity.CENTER;
        mAlbumView = new AlbumViewPager(getContext());
        mAlbumView.setBackgroundColor(WHITE);
        mAlbumView.getBackground().setAlpha(255);
        frameLayout.addView(mAlbumView, albumParams);

        surfaceLayout = new LinearLayout(mContext);
        surfaceLayout.setBackgroundColor(getResources().getColor(R.color.color_black_a10));
        FrameLayout.LayoutParams linerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        frameLayout.addView(surfaceLayout, linerParams);
        surfaceLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        surfaceView = new SurfaceView(mContext);
        LinearLayout.LayoutParams surfaceParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        surfaceParams.gravity = Gravity.CENTER;
        surfaceLayout.addView(surfaceView, surfaceParams);

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
        surfaceLayout.setVisibility(View.INVISIBLE);

        return frameLayout;
    }

    private void initData() {
//        mVideoBrowseAsyncTask = new VideoBrowseAsyncTask(mContext, new VideoBrowseAsyncTask.VideoBrowseCallback() {
//            @Override
//            public void onResult(List<VideoData> result) {
//                if (result != null) {
//                    videoDataList = result;
//                    mAdapter.setDataList(videoDataList);
//                    mAlbumView.setCurrentItem(mCurrIndex < videoDataList.size() ? mCurrIndex : 0, false);
//
//                }
//            }
//        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            mVideoBrowseAsyncTask.executeOnExecutor(Executors.newCachedThreadPool());
//        } else {
//            mVideoBrowseAsyncTask.execute();
//        }
        if(!fromShare) {
            videoDataList = LocalMediaHelp.getVideoList();
        }
        if(videoDataList != null){
            mAdapter.setDataList(videoDataList);
            mAlbumView.setCurrentItem(mCurrIndex < videoDataList.size() ? mCurrIndex : 0, false);
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new VideoPreviewAdapter(getContext());
        mAlbumView.setPageMargin(PAGE_MARGIN);
        mAlbumView.setAdapter(mAdapter);
        mAlbumView.setOffscreenPageLimit(1);
        mediaPlayer = new MediaPlayer();
        initData();
        mAdapter.setVideoPlayListener(new VideoPreviewAdapter.VideoPlayListener() {
            @Override
            public void onStartPlayClick(int position) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(videoDataList.get(position).url);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    //切换视频源的时候会闪烁上一个视频的最后显示帧，暂时让view延迟显示
                    surfaceLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            surfaceLayout.setVisibility(View.VISIBLE);
                        }
                    },100);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                surfaceLayout.setVisibility(View.INVISIBLE);
            }
        });

        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                changeVideoSize();
            }
        });

        mAlbumView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrIndex = position;
                if (mListener != null) {
                    mListener.onPageChanged(mCurrIndex);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAlbumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick();
                }
            }
        });

        mAlbumView.setOnPullProgressListener(new OnPullProgressListener() {
            @Override
            public void startPull() {
                if (mListener != null) {
                    mListener.onStartPull();
                }
            }

            @Override
            public void onProgress(float progress) {
                if (mListener != null) {
                    mListener.onPullProgress(progress);
                }
                mAlbumView.setBackgroundColor(WHITE);
                mAlbumView.getBackground().setAlpha((int) (progress * 255));
            }

            @Override
            public void stopPull(boolean isFinish) {
                if (mListener != null) {
                    mListener.stopPull(isFinish);
                }
                if (!isFinish) {
                    mAlbumView.setBackgroundColor(WHITE);
                    mAlbumView.getBackground().setAlpha(255);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mListener = null;
        mediaPlayer = null;
    }

    public void setOnAlbumEventListener(OnAlbumEventListener l) {
        this.mListener = l;
    }

    public void showSelectPicture(int pos) {
        Log.d(TAG, "showSelectPicture: " + pos);
        mAlbumView.setCurrentItem(pos, false);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        surfaceLayout.setVisibility(View.INVISIBLE);
    }

    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceCreated");
            mediaPlayer.setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            Log.d(TAG, "surfaceChanged: " + surfaceHolder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "surfaceDestroyed: " + surfaceHolder);
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    };

    private void changeVideoSize() {
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();

        int surfaceWidth = DimensUtils.getDeviceWidth(mContext);
        int surfaceHeight = DimensUtils.getDeviceHeight(mContext);

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max = 1;
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) surfaceView.getLayoutParams();
        layoutParams.width = videoWidth;
        layoutParams.height = videoHeight;
        surfaceView.setLayoutParams(layoutParams);
    }



    public interface OnAlbumEventListener {
        void onClick();

        void onPageChanged(int page);

        void onStartPull();

        void onPullProgress(float progress);

        void stopPull(boolean isFinish);
    }
}
