package com.coocaa.tvpi.module.local.album;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.publib.data.local.ImageData;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class AlbumFragment extends Fragment {
    private String TAG = AlbumFragment.class.getSimpleName();

    private static final String INTENT_IMAGE = "extra_image";
    private static final String INTENT_INDEX = "extra_index";
    private static final int WHITE = 0xFFFFFFFF;
    private static final int PAGE_MARGIN = 30;

    private static Context mContext;
    private AlbumViewPager mAlbumView;
    private AlbumPreviewAdapter mAdapter;

    private ArrayList<Uri> mImageUris = new ArrayList<>();
    private int mCurrIndex = 0;
    private boolean fromShare = false;

    private OnAlbumEventListener mListener;

    public static AlbumFragment newInstance(ArrayList<ImageData> imageDatas, int index,
                                            Context context, boolean fromShare) {
        mContext = context;
        AlbumFragment fragment = new AlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(INTENT_IMAGE, imageDatas);
        bundle.putInt(INTENT_INDEX, index);
        bundle.putBoolean("fromShare", fromShare);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if (extras != null) {
            ArrayList<ImageData> images = extras.getParcelableArrayList(INTENT_IMAGE);
            mCurrIndex = extras.getInt(INTENT_INDEX, 0);
            fromShare = extras.getBoolean("fromShare", false);
            mImageUris.clear();
            int imageSize;
            if (images != null && (imageSize = images.size()) > 0) {
                Uri imgUri = null;
                for (int i = 0; i < imageSize; i++) {
                    imgUri = Uri.fromFile(new File(images.get(i).url));
                    mImageUris.add(imgUri);
                }
                mCurrIndex = (mCurrIndex >= 0 && mCurrIndex < imageSize) ? mCurrIndex : 0;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAlbumView = new AlbumViewPager(getContext());
        mAlbumView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        mAlbumView.setBackgroundColor(WHITE);
        mAlbumView.getBackground().setAlpha(255);
        return mAlbumView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new AlbumPreviewAdapter(getContext(), mImageUris);
        mAlbumView.setPageMargin(PAGE_MARGIN);
        mAlbumView.setAdapter(mAdapter);
        mAlbumView.setOffscreenPageLimit(1);

        if (mCurrIndex < mImageUris.size()) {
            mAlbumView.setCurrentItem(mCurrIndex, false);
        }

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
    }

    public void setOnAlbumEventListener(OnAlbumEventListener l) {
        this.mListener = l;
    }

    public void showSelectPicture(int pos){
        Log.d(TAG, "showSelectPicture: ");
        mAlbumView.setCurrentItem(pos,false);

    }

    public interface OnAlbumEventListener {
        void onClick();

        void onPageChanged(int page);

        void onStartPull();

        void onPullProgress(float progress);

        void stopPull(boolean isFinish);
    }
}
