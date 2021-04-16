package com.coocaa.tvpi.module.movie;

import android.app.DialogFragment;
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

import com.coocaa.smartscreen.data.movie.LongVideoDetailModel;
import com.coocaa.tvpi.module.movie.widget.LongVideoDescView;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wuhaiyuan on 2018/3/30.
 */

public class DescDialogFragment extends DialogFragment {

    public final static String DIALOG_FRAGMENT_TAG = DescDialogFragment.class.getSimpleName();

    private static final String TAG = DescDialogFragment.class.getSimpleName();
    private final static String COMMON_DIALOG_SERIALIZE_KEY = "COMMON_DIALOG_SERIALIZE_KEY";

    private View mLayout;
    private LongVideoDescView longVideoDescView;
    private LongVideoDetailModel longVideoDetail;

    public void setLongVideoDetial(LongVideoDetailModel longVideoDetail) {
        this.longVideoDetail = longVideoDetail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置窗体的背景色为透明的

        mLayout = inflater.inflate(R.layout.desc_dialog_layout, container);
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
        android.app.Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    private void initViews() {
        longVideoDescView = mLayout.findViewById(R.id.desc_body_view);
        if (null != longVideoDetail)
            longVideoDescView.updateViews(longVideoDetail.director,longVideoDetail.actor,longVideoDetail.description);

        mLayout.findViewById(R.id.desc_close_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });
    }

}
