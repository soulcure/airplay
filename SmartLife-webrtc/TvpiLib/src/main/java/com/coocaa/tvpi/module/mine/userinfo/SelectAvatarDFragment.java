package com.coocaa.tvpi.module.mine.userinfo;

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

import com.coocaa.tvpilib.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * @ClassName SelectAvatarDFragment
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/10
 */
public class SelectAvatarDFragment extends DialogFragment {

    private String TAG = SelectAvatarDFragment.class.getSimpleName();
    public static final String DIALOG_FRAGMENT_TAG = SelectAvatarDFragment.class.getSimpleName();

    private View mView;
    private TextView tvFromCamera;
    private TextView tvFromAlbum;
    private TextView tvCancel;

    private OnAvatarSelectListener mListener;

    public interface OnAvatarSelectListener {

        /**
         * @param select : 1 Camera, 2 Album
         */
        void onAvatarSelect(int select);
    }

    public void setOnAvatarSelectListener(OnAvatarSelectListener listener) {
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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getDialog().getWindow().setNavigationBarColor(Color.RED);
//        }

//        View decorView = getDialog().getWindow().getDecorView();
//        int systemUiVisibility = decorView.getSystemUiVisibility();
//        decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mView = inflater.inflate(R.layout.fragment_select_avatar, container);
        initView();
        initListener();
        return mView;
    }

    private void initView() {
        tvFromCamera = mView.findViewById(R.id.from_camera);
        tvFromAlbum = mView.findViewById(R.id.from_album);
        tvCancel = mView.findViewById(R.id.cancel);
    }

    private void initListener() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });

        tvFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAvatarSelect(1);
                }
            }
        });

        tvFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onAvatarSelect(2);
                }
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
