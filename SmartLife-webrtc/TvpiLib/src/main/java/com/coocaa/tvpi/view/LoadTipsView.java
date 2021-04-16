package com.coocaa.tvpi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.tvpilib.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

/**
 * Created by IceStorm on 2017/12/14.
 */

public class LoadTipsView extends LinearLayout {
    public static final int TYPE_LOADING = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_NODATA = 2;//没有数据  added by fuchengming 2016.6.21
    public static final int TYPE_NOLOGIN = 3;//没有登录

    @IntDef({TYPE_LOADING, TYPE_FAILED, TYPE_NODATA, TYPE_NOLOGIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    private Context mContext;
    private LinearLayout mLoadTipsLayout;
    private RelativeLayout mLoadTipsRootLayout;
    private LinearLayout mLoadTipsNoLoginLayout;
    private ImageView mLoadTipsIV;
    private TextView mLoadTipsTV;
    private TextView mLoadTipsRefreshTV;
    private ProgressBar mProgressBar;

//    private LottieAnimationView mLottieAnimationView;

    public LoadTipsView(Context context) {
        super(context);
        mContext = context;
        initView();
    }


    public LoadTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public LoadTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    public LinearLayout getLoadTipsLayout() {
        return mLoadTipsLayout;
    }

    public void setLoadTipsOnClickListener(OnClickListener listener) {
        mLoadTipsRefreshTV.setOnClickListener(listener);
    }

    public void setLoadTips(String tip, int type) {
        setTipsText(tip);
        setLoadTipsIV(type);
    }

    public void setTipsText(String s) {
        if (null != mLoadTipsTV)
            mLoadTipsTV.setText(s);
    }

    public void setLoadTipsIV(@Type int type) {
        switch (type) {
            case TYPE_LOADING:
                mLoadTipsLayout.setVisibility(GONE);
                mProgressBar.setVisibility(VISIBLE);
                mLoadTipsNoLoginLayout.setVisibility(GONE);
                break;
            case TYPE_FAILED:
                mLoadTipsLayout.setVisibility(VISIBLE);
                mLoadTipsIV.setBackgroundResource(R.drawable.icon_loadtips_no_network);
                mLoadTipsTV.setText(R.string.loadtips_no_network);

                mProgressBar.setVisibility(GONE);
                mLoadTipsNoLoginLayout.setVisibility(GONE);
                break;
            case TYPE_NODATA:
                mLoadTipsLayout.setVisibility(VISIBLE);
                mLoadTipsIV.setBackgroundResource(R.drawable.icon_loadtips_no_data);
                mLoadTipsTV.setText(R.string.loadtips_no_data);

                mProgressBar.setVisibility(GONE);
                mLoadTipsNoLoginLayout.setVisibility(GONE);
                break;
            case TYPE_NOLOGIN:
                mLoadTipsLayout.setVisibility(GONE);
//                    mLoadTipsIV.setBackgroundResource(R.drawable.load_tips_no_login);

                mProgressBar.setVisibility(GONE);
                mLoadTipsNoLoginLayout.setVisibility(VISIBLE);
                break;

            default:
                break;
        }

    }

    public void showLoading() {
        setVisibility(VISIBLE);
        setLoadTipsIV(TYPE_LOADING);
    }

    public void showLoadingFailed(){
        setVisibility(VISIBLE);
        setLoadTipsIV(TYPE_FAILED);
    }

    public void showLoadingFailed(String errorMsg){
        setVisibility(VISIBLE);
        setLoadTipsIV(TYPE_FAILED);
        setTipsText(errorMsg);
    }

    public void showNoData(){
        setVisibility(VISIBLE);
        setLoadTipsIV(TYPE_NODATA);
    }

    /**
     *设置背景色
     * */
    public void setRootBackground(int resId){
        mLoadTipsRootLayout.setBackgroundResource(resId);
    }

    public void showLoadingComplete(){
        setVisibility(GONE);
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.load_tips_layout, this);
        mLoadTipsLayout = findViewById(R.id.load_tips_layout);
        mLoadTipsRootLayout = findViewById(R.id.load_tips_root_layout);
        mLoadTipsNoLoginLayout = findViewById(R.id.load_tips_no_login_layout);
        mLoadTipsIV = findViewById(R.id.load_tips_iv);
        mLoadTipsTV = findViewById(R.id.load_tips_tv);
        mLoadTipsRefreshTV = findViewById(R.id.load_tips_refresh_tv);
        mProgressBar = findViewById(R.id.load_tips_progressbar);

//        mLottieAnimationView = findViewById(R.id.load_tips_loading_iv);
//        mLottieAnimationView.setAnimation("loading.json");
//        mLottieAnimationView.setRepeatCount(INFINITE);
//        mLottieAnimationView.playAnimation();
//        findViewById(R.id.load_tips_root_layout).setOnClickListener(new OnClickListener() {//目的，消耗掉点击事件，不传到下面去
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        mLoadTipsNoLoginLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                UIHelper.toLogin(getContext());
            }
        });
    }
}
