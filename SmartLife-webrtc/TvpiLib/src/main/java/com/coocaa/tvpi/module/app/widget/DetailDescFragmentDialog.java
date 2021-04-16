package com.coocaa.tvpi.module.app.widget;

import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

/**
 * @ClassName DetailDescFragmentDialog
 * @Description TODO (write something)
 * @User heni
 * @Date 18-7-26
 */
public class DetailDescFragmentDialog extends DialogFragment {

    private static final String TAG = DetailDescFragmentDialog.class.getSimpleName();
    public final static String DIALOG_FRAGMENT_TAG = DetailDescFragmentDialog.class.getSimpleName();

    private View mLayout;
    private TextView appDetailSize;
    private TextView appDetailDesc;

    private String strVersion,strDesc;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null) {
            strVersion = (String) bundle.get("version");
            strDesc = (String) bundle.get("desc");
        }
        this.setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
//        layoutParams.dimAmount = 0.0f;//去掉半透明阴影
        layoutParams.width = dm.widthPixels;
        layoutParams.height = layoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置窗体的背景色为透明的

        mLayout = inflater.inflate(R.layout.fragment_app_detail_desc_dialog, container, false);
        initViews();
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void dismissDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (fragment != null) {
            DialogFragment df = (DialogFragment) fragment;
            df.dismiss();
        }
    }

    private void initViews() {
        appDetailSize = mLayout.findViewById(R.id.app_detail_fragment_version_size);
        appDetailDesc = mLayout.findViewById(R.id.app_detail_fragment_summary_desc);
        appDetailSize.setText(strVersion);
        appDetailDesc.setText(strDesc);
    }
}
