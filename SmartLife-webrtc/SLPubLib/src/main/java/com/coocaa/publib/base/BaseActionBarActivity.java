package com.coocaa.publib.base;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.R;
import com.coocaa.publib.utils.DimensUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;


public class BaseActionBarActivity extends BaseActivity {

    protected View actionBarLayout;
    protected TextView mTitle = null, mRightButton = null;
    private View mBackView = null, mLLActionBar;
    private ImageView mIvLeft;
    protected ActionBar mActBar = null;
    private boolean mEnableFitSystemWindow = false;

    // 设置顶部actionBar的底部分割线
    protected View actBarBottomSeparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mEnableFitSystemWindow) {
            setCompatibleFitSystemWindow();
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (mTitle == null) {
            super.setTitle(titleId);
        } else {
            mTitle.setText(titleId);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mTitle == null) {
            super.setTitle(title);
        } else {
            mTitle.setText(title);
        }
    }

    @Override
    public boolean onBackClicked() {
        /*if (!AppManager.isExsitActivity(this, HomeActivity.class)) {
            Intent i = new Intent(this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }*/
        finish();
        return true;
    }


    // 左边键被点击
    public void onLeftClicked(View v) {
        //如果是未登录，点击左上角返回
        /*if (!AppManager.isExsitActivity(this, HomeActivity.class)) {
            Intent i = new Intent(this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }*/
        finish();
    }


    // 右边键被点击
    public void onRightButtonClicked(View v) {
    }

    // 设置ActionBar
    private void setupActionBar() {
        mActBar = getSupportActionBar();
        if (mActBar != null) {
            if(Build.VERSION.SDK_INT>=21){
                mActBar.setElevation(0);
            }
            mActBar.setDisplayHomeAsUpEnabled(false); // 返回图标
            mActBar.setDisplayShowTitleEnabled(false); // 文字
            mActBar.setDisplayShowHomeEnabled(false); // app图标。
            mActBar.setDisplayShowCustomEnabled(true);
            //mActBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.fff2f2f2)));//默认ActionBar颜色偏灰

            ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
            View v = LayoutInflater.from(this).inflate(R.layout.view_normal_actionbar, null);
            mActBar.setCustomView(v, params);
            //getSupportActionBar()左右两边会有留白 所以必须添加改设置
            Toolbar parent =(Toolbar) v.getParent();
            parent.setContentInsetsAbsolute(0,0);

            actBarBottomSeparator = v.findViewById(R.id.view2);
            actionBarLayout = v.findViewById(R.id.normal_actionbar_layout);
            mTitle = (TextView) v.findViewById(R.id.title_tv_title);
            mLLActionBar = v.findViewById(R.id.title_layout);
            mIvLeft = (ImageView) v.findViewById(R.id.iv_left);
            mTitle.setSelected(true);
            mBackView = v.findViewById(R.id.title_btn_left);
            mRightButton = (TextView) v.findViewById(R.id.title_btn_right);
            mBackView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onLeftClicked(view);
                }
            });
            mRightButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRightButtonClicked(view);
                }
            });
            mRightButton.setSelected(true);
        }
    }

    public void setActionBarBackgroundColor(int color){
//        actionBarLayout.setBackgroundDrawable(new ColorDrawable(color));
        actionBarLayout.setBackgroundResource(color);
    }

    // 设置右键文本
    public void setRightButton(String text) {
        mRightButton.setText(text);
        if (TextUtils.isEmpty(text)) {
            mRightButton.setVisibility(View.INVISIBLE);
            mRightButton.setEnabled(false);
        } else {
            mRightButton.setVisibility(View.VISIBLE);
            mRightButton.setEnabled(true);
        }
    }

    // 设置右键文本
    public void setRightButton(String text, int textColor) {
        mRightButton.setText(text);
        mRightButton.setTextColor(textColor);
        if (TextUtils.isEmpty(text)) {
            mRightButton.setVisibility(View.INVISIBLE);
            mRightButton.setEnabled(false);
        } else {
            mRightButton.setVisibility(View.VISIBLE);
            mRightButton.setEnabled(true);
        }
    }

    public void setRightIcon(int resId) {
        if (resId > 0) {
            mRightButton.setVisibility(View.VISIBLE);
            mRightButton.setEnabled(true);
            mRightButton.setBackgroundResource(resId);
        } else {
            mRightButton.setVisibility(View.INVISIBLE);
            mRightButton.setEnabled(false);
        }
    }

    public void setLeftIcon(int resId) {
        if (resId > 0) {
            mIvLeft.setVisibility(View.VISIBLE);
            mIvLeft.setEnabled(true);
            mIvLeft.setBackgroundResource(resId);
        } else {
            mIvLeft.setVisibility(View.INVISIBLE);
            mIvLeft.setEnabled(false);
        }
    }

    public View getRightButton() {
        return mRightButton;
    }

    // 设置actBar底部分割线是否隐藏
    public void setActBarBottomSeparatorVisibility(int visibility) {
        if(actBarBottomSeparator != null)
            actBarBottomSeparator.setVisibility(visibility);
    }

    /**
     * 这个函数必须在onCreate函数中调用。
     */
    protected void disableCompatibleFitSystemWindow() {
        mEnableFitSystemWindow = false;
    }

    protected void setCompatibleFitSystemWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Meizu手机下，没有设置fitsSystemWindows为true。内容是以屏幕对齐的。
            int padding = DimensUtils.getActionBarHeight(this) + DimensUtils.getStatusBarHeight(this);
            View view = findViewById(android.R.id.content);
            if (view.getPaddingTop() != padding) {
                findViewById(android.R.id.content).setPadding(0, padding, 0, 0);
            }
        }
    }

}
