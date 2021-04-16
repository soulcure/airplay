package com.coocaa.tvpi.view;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.swaiotos.virtualinput.VirtualInputStarter;
import com.coocaa.tvpilib.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class PushProgressDialogFragment extends DialogFragment {

    private static final String DIALOG_FRAGMENT_TAG = PushProgressDialogFragment.class.getSimpleName();
    private static final String TAG = PushProgressDialogFragment.class.getSimpleName();

    private ImageView pushStateImg;
    private TextView tvPushState;
    private CirclePercentView circlePercentView;
    private AppCompatActivity mActivity;

    private PushProgressDialogFragment.PushProgressDialogFragmentListener listener;
    private ObjectAnimator animator;
    private boolean autoShowVirtualInput = false; //是否投屏成功后自动显示遥控页面
    private boolean isLocalPush = true; //是否是本地投屏
    private String toast;

    public interface PushProgressDialogFragmentListener {
        void onDialogDismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.push_progress_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().getAttributes().windowAnimations = R.style.top_dialog_anim;
    }

    public PushProgressDialogFragment setAutoShowVirtualInput(boolean b) {
        this.autoShowVirtualInput = b;
        return this;
    }

    public PushProgressDialogFragment setToastOnSuccess(String toast) {
        this.toast = toast;
        return this;
    }

    public PushProgressDialogFragment setIsLocalPush(boolean b) {
        this.isLocalPush = b;
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        if (getDialog() == null || getDialog().getWindow() == null || getActivity() == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER | Gravity.TOP;
        layoutParams.y = DimensUtils.dp2Px(getContext(), 54);
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Override
    public void dismiss() {
        Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismissAllowingStateLoss();
        }
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        if (listener != null) {
            listener.onDialogDismiss();
        }
    }

    public PushProgressDialogFragment with(AppCompatActivity appCompatActivity) {
        mActivity = appCompatActivity;
        return this;
    }

    private void initView(View view) {
        pushStateImg = view.findViewById(R.id.push_state_img);
        tvPushState = view.findViewById(R.id.tv_push_state);
        circlePercentView = view.findViewById(R.id.pushing_state_loading);
        Log.d(TAG, "initView: ");
    }

    public void showPushing() {
        if (!mActivity.getSupportFragmentManager().isDestroyed()) {
            this.show(mActivity.getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);
            Log.d(TAG, "showPushing: ");
        }
    }

    public void showPushSuccess() {
        if (pushStateImg != null && tvPushState != null && circlePercentView != null) {
            circlePercentView.setVisibility(View.INVISIBLE);
            pushStateImg.setVisibility(View.VISIBLE);
            tvPushState.setText("共享成功");
            GlideApp.with(getActivity()).load(R.drawable.icon_push_success).into(pushStateImg);
            if(!TextUtils.isEmpty(toast)) {
                ToastUtils.getInstance().showGlobalShort(toast);
            }
            if(isLocalPush) { //目前只有本地投屏支持遥控场景
                VirtualInputStarter.show(getContext(), autoShowVirtualInput);
            }
        }
    }

    public void showPushTimeout() {
        if (pushStateImg != null && tvPushState != null && circlePercentView != null) {
            circlePercentView.setVisibility(View.INVISIBLE);
            pushStateImg.setVisibility(View.VISIBLE);
            tvPushState.setText("共享超时");
            GlideApp.with(getActivity()).load(R.drawable.icon_push_error).into(pushStateImg);
        }
    }

    public void showPushError() {
        if (pushStateImg != null && tvPushState != null && circlePercentView != null) {
            circlePercentView.setVisibility(View.INVISIBLE);
            pushStateImg.setVisibility(View.VISIBLE);
            tvPushState.setText("共享失败");
            GlideApp.with(getActivity()).load(R.drawable.icon_push_error).into(pushStateImg);
        }
    }

    public void setProgress(float percentage) {
        if (animator != null) {
            if (animator.isRunning() || circlePercentView.getPercentage() == percentage) {
                return;
            }
        }
        if (circlePercentView != null) {
            animator = ObjectAnimator.ofFloat(circlePercentView, "percentage", 0, percentage);
            animator.setDuration(7000);
            animator.setRepeatCount(0);
            animator.start();
        }
    }

    //无动画
    public void setNoAniProgress(float percentage) {
        if (circlePercentView != null) {
            circlePercentView.setPercentage(percentage);
        }
    }

    public void setListener(PushProgressDialogFragmentListener listener) {
        this.listener = listener;
    }
}
