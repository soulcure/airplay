package com.coocaa.tvpi.module.mine.about;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.coocaa.tvpilib.R;

/**
 * @ClassName SelectAvatarDFragment
 * @Description TODO (write something)
 * @User caj
 * @Date 2020/12/10
 */
public class SaveCodeConfirmDFragment extends DialogFragment {

    private String TAG = SaveCodeConfirmDFragment.class.getSimpleName();
    public static final String DIALOG_FRAGMENT_TAG = SaveCodeConfirmDFragment.class.getSimpleName();

    private View mView;
    private TextView tvConfirm;
    private TextView tvCancel;

    private OnConfirmListener mListener;

    public interface OnConfirmListener {

        void onConfirmOK();

        void onConfirmCancel();
    }

    public void setConfirmListener(OnConfirmListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        if (getDialog() == null || getDialog().getWindow() == null || getActivity() == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mView = inflater.inflate(R.layout.fragment_save_confirm, container);
        initView();
        initListener();
        return mView;
    }

    private void initView() {
        tvConfirm = mView.findViewById(R.id.tv_confirm);
        tvCancel = mView.findViewById(R.id.cancel);
    }

    private void initListener() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onConfirmCancel();
                dismissDialog();
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onConfirmOK();
                dismissDialog();
            }
        });
    }

    public void dismissDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (fragment != null) {
            DialogFragment df = (DialogFragment) fragment;
            df.dismiss();
        }
    }

}
