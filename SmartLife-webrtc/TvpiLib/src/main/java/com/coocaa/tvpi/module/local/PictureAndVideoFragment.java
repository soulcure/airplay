package com.coocaa.tvpi.module.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.coocaa.publib.data.local.ImageData;
import com.coocaa.publib.data.local.MediaData;
import com.coocaa.publib.data.local.VideoData;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.local.adapter.VideoAndPicPreviewAdapter;
import com.coocaa.tvpi.module.local.album.AlbumViewPager;
import com.coocaa.tvpi.module.local.album.OnPullProgressListener;
import com.coocaa.tvpi.module.local.utils.LocalMediaHelp;
import com.coocaa.tvpilib.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static com.coocaa.tvpi.module.local.utils.MediaStoreHelper.MAIN_ALBUM_NAME;

/**
 * @author chenaojun
 */
public class PictureAndVideoFragment extends Fragment {

    private final String TAG = PictureAndVideoFragment.class.getSimpleName();

    private static final String INTENT_INDEX = "extra_index";
    private static final String INTENT_IMAGE = "extra_album";
    private static final int GARY = 0xFFF4F4F4;
    private static final int PAGE_MARGIN = 30;
    private static final String SHOW_IMAGE = "SHOW_IMAGE";
    private static final String SHOW_VIDEO = "SHOW_VIDEO";
    private static final String SHOW_ALL = "SHOW_ALL";
    public static String KEY_SHOW_TYPE = "KEY_SHOW_TYPE";

    private static Context mContext;
    private VideoAndPicPreviewAdapter mAdapter;
    private OnAlbumEventListener mListener;

    private MediaPlayer mediaPlayer;
    private AlbumViewPager mAlbumView;
    private SurfaceView surfaceView;
    private LinearLayout surfaceLayout;

    private String mShowType;
    private String mAlbumName;
    private int mCurrIndex = 0;

    public static PictureAndVideoFragment newInstance(int index, Context context, String showType, String albumName) {
        mContext = context;
        PictureAndVideoFragment fragment = new PictureAndVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_INDEX, index);
        bundle.putString(KEY_SHOW_TYPE, showType);
        bundle.putString(INTENT_IMAGE, albumName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseBundle();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(mContext);
        LinearLayout.LayoutParams albumParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        albumParams.gravity = Gravity.CENTER;
        mAlbumView = new AlbumViewPager(getContext());
        mAlbumView.setBackgroundColor(GARY);
        mAlbumView.getBackground().setAlpha(255);
        frameLayout.addView(mAlbumView, albumParams);

        surfaceLayout = new LinearLayout(mContext);
        surfaceLayout.setBackgroundColor(getResources().getColor(R.color.color_black_a10));
        FrameLayout.LayoutParams linerParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        frameLayout.addView(surfaceLayout, linerParams);
        surfaceLayout.setOnTouchListener((v, event) -> true);

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

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new VideoAndPicPreviewAdapter(getContext());
        mAlbumView.setPageMargin(PAGE_MARGIN);
        mAlbumView.setAdapter(mAdapter);
        mAlbumView.setOffscreenPageLimit(1);
        mediaPlayer = new MediaPlayer();
        initData();
        mAdapter.setVideoPlayListener(position -> {
            try {
                VideoData videoData = (VideoData) mAdapter.getMediaDataList().get(position);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(videoData.url);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                //切换视频源的时候会闪烁上一个视频的最后显示帧，暂时让view延迟显示
                surfaceLayout.postDelayed(() -> surfaceLayout.setVisibility(View.VISIBLE), 500);
                mAlbumView.postDelayed(()->mAlbumView.setVisibility(View.INVISIBLE),500);
            }
        });

        surfaceView.setOnClickListener(v -> {
            if(mediaPlayer == null) {
                return;
            }
            try {
                mediaPlayer.stop();
            } catch (Exception e) {
            }
            surfaceLayout.setVisibility(View.INVISIBLE);
            mAlbumView.setVisibility(View.VISIBLE);
        });


        mediaPlayer.setOnCompletionListener(mp -> {
            surfaceLayout.setVisibility(View.INVISIBLE);
            mAlbumView.setVisibility(View.VISIBLE);
        });

        mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> changeVideoSize());

        mAlbumView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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

        mAlbumView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick();
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
                mAlbumView.setBackgroundColor(GARY);
                mAlbumView.getBackground().setAlpha((int) (progress * 255));
            }
            @Override
            public void stopPull(boolean isFinish) {
                if (mListener != null) {
                    mListener.stopPull(isFinish);
                }
                if (!isFinish) {
                    mAlbumView.setBackgroundColor(GARY);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    private void parseBundle() {
        Bundle extras = getArguments();
        if (extras != null) {
            mCurrIndex = extras.getInt(INTENT_INDEX, 0);
            Log.d(TAG, "onCreate: mCurrIndex" + mCurrIndex);
            //initImageData(extras);
            mShowType = extras.getString(KEY_SHOW_TYPE, SHOW_ALL);
            mAlbumName = extras.getString(INTENT_IMAGE, MAIN_ALBUM_NAME);
        }
    }

    private void initData() {
        if (SHOW_ALL.equals(mShowType)) {
            List<MediaData> mediaDataList = LocalMediaHelp.getMediaDataList();
            if (mediaDataList != null) {
                mAdapter.setMediaDataList(mediaDataList);
                mAlbumView.setCurrentItem(mCurrIndex < mAdapter.getMediaDataList().size() ? mCurrIndex : 0, false);
            }
        }

        if (SHOW_IMAGE.equals(mShowType)) {
            List<ImageData> imageDataList = LocalMediaHelp.getImageCacheMap().get(mAlbumName);
            if (imageDataList != null) {
                mAdapter.setImageData(imageDataList);
            }
        }

        if (SHOW_VIDEO.equals(mShowType)) {
            List<VideoData> videoDataList = LocalMediaHelp.getVideoList();
            if (videoDataList != null) {
                mAdapter.setVideoDataList(videoDataList);
            }
        }
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
        mAlbumView.setVisibility(View.VISIBLE);
    }

    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
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
        layoutParams.gravity = Gravity.CENTER;
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
