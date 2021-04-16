package com.coocaa.tvpi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.tvpilib.R;
import com.scwang.smart.refresh.layout.api.RefreshFooter;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;
import com.scwang.smart.refresh.layout.util.SmartUtil;

import static com.airbnb.lottie.LottieDrawable.INFINITE;

public class SmartRefreshFooter extends LinearLayout implements RefreshFooter {
    private TextView footerTitle;
    private ProgressBar footerProgressbar;

    public SmartRefreshFooter(Context context) {
        this(context,null);
    }

    public SmartRefreshFooter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.custom_footer, this, true);
        footerTitle = (TextView) view.findViewById(R.id.custom_footer_title);
        footerProgressbar = (ProgressBar) view.findViewById(R.id.custom_footer_progressbar);
        footerProgressbar.setIndeterminateDrawable(ContextCompat.getDrawable(context, R.drawable.progressbar_yellow_circle));
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {
        footerTitle.setVisibility(View.INVISIBLE);
        footerProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public int onFinish(@NonNull RefreshLayout refreshLayout, boolean success) {
        footerTitle.setText("查看更多");
        footerTitle.setVisibility(View.VISIBLE);
        footerProgressbar.setVisibility(View.INVISIBLE);
        return 100;//延迟100毫秒之后再弹回
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        switch (newState) {
            case None:
            case PullDownToRefresh:
                footerTitle.setText("松开载入更多");
                break;
            case Refreshing:
                break;
            case ReleaseToRefresh:
//                footerTitle.setText("查看更多");
                break;
        }
    }

    @Override
    public boolean setNoMoreData(boolean noMoreData) {
        return false;
    }
}
