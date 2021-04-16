package com.coocaa.tvpi.module.app.fragment;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.util.RotateTransformation;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;
import com.gyf.immersionbar.ImmersionBar;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ScreenshotFragment
 * @Description 全屏放大的screenshort页面
 * @User heni
 * @Date 18-8-3
 */
public class ScreenshotFragment extends DialogFragment {

    public static String TAG = ScreenshotFragment.class.getSimpleName();
    public final static String DIALOG_FRAGMENT_TAG = ScreenshotFragment.class.getSimpleName();

    private Context mContext;
    private View mLayout;
    private ViewPager viewPager;
    private LinearLayout indicatorLayout;

    int position;
    private List<String> dataLists;
    private List<ImageView> imageViewLists;

    private List<ImageView> indicatorImgLists;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        dataLists = new ArrayList<>();
        imageViewLists = new ArrayList<>();
        indicatorImgLists = new ArrayList<>();

        Bundle bundle = getArguments();
        if(bundle != null) {
            position = bundle.getInt("position");
            dataLists = bundle.getStringArrayList("datalist");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP;
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

        mLayout = inflater.inflate(R.layout.fragment_app_detail_screen_shot, container, false);
        initViews();

        StatusBarHelper.translucent(getDialog().getWindow());
        StatusBarHelper.setStatusBarLightMode(getDialog().getWindow());
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
        viewPager = mLayout.findViewById(R.id.app_detail_screenshot_viewpager);
        indicatorLayout = mLayout.findViewById(R.id.app_detail_screenshot_indicator_layout);

        imageViewLists.clear();
        indicatorImgLists.clear();
        indicatorLayout.removeAllViews();

        for (int i = 0; i < dataLists.size(); i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });

            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageViewLists.add(imageView);
            GlideApp.with(mContext).load(dataLists.get(i)).transform(new RotateTransformation(mContext, 90)).into(imageView);

            ImageView imgIndicator = new ImageView(mContext);
            imgIndicator.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DimensUtils.dp2Px(mContext, 10f), DimensUtils.dp2Px(mContext, 2f));
            params.leftMargin = DimensUtils.dp2Px(mContext, 2.5f);
            params.rightMargin = DimensUtils.dp2Px(mContext, 2.5f);
            if (i == position) {
                imgIndicator.setBackgroundResource(R.color.color_main_red);
            } else {
                imgIndicator.setBackgroundResource(R.color.b_3);
            }
            indicatorImgLists.add(imgIndicator);
            indicatorLayout.addView(imgIndicator, params);
        }

        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageMargin((int) getResources().getDimension(R.dimen.global_horizontal_margin_10));
        viewPager.setCurrentItem(position);
        viewPager.setOnPageChangeListener(onPageChangeListener);
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < indicatorImgLists.size(); i++) {
                if (i == position)
                    indicatorImgLists.get(i).setBackgroundResource(R.color.color_main_red);
                else
                    indicatorImgLists.get(i).setBackgroundResource(R.color.b_3);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    PagerAdapter pagerAdapter = new PagerAdapter() {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = imageViewLists.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return dataLists.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    };

}
