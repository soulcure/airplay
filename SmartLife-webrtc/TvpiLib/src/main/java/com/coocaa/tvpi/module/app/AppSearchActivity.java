package com.coocaa.tvpi.module.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.base.mvvm.BaseViewModelProvideActivity;
import com.coocaa.tvpi.module.app.fragment.AppSearchBeforeFragment;
import com.coocaa.tvpi.module.app.fragment.AppSearchResultFragment;
import com.coocaa.tvpi.module.app.viewmodel.share.AppSearchShareViewModel;
import com.coocaa.tvpi.view.DeletableEditText;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;
import static com.coocaa.tvpi.common.UMengEventId.APP_SEARCH_BTN_CLICK;

/**
 * 电视应用搜索界面
 * Created by songxing on 2020/8/10
 */
public class AppSearchActivity extends BaseViewModelProvideActivity {
    private static final String TAG = AppSearchActivity.class.getSimpleName();

    private DeletableEditText etSearch;
    private AppSearchBeforeFragment searchBeforeFragment;
    private AppSearchResultFragment searchResultFragment;
    private AppSearchShareViewModel searchShareViewModel;
    private Fragment currentFragment;

    public static void start(Context context) {
        Intent starter = new Intent(context, AppSearchActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_search);
        initView();
        setEditTextText();
        setFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }


    private void initView(){
        etSearch = findViewById(R.id.etSearch);
        TextView tvCancel = findViewById(R.id.tvCancel);
        searchBeforeFragment = new AppSearchBeforeFragment();
        searchResultFragment = new AppSearchResultFragment();
        searchShareViewModel = ViewModelProviders.of(this).get(AppSearchShareViewModel.class);
        searchShareViewModel.setShowSearchBefore(true);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_SEARCH) {
                    MobclickAgent.onEvent(AppSearchActivity.this, APP_SEARCH_BTN_CLICK);
                    Editable text = etSearch.getText();
                    if (text != null) {
                        searchShareViewModel.setSearchKeyword(text.toString());
                        searchShareViewModel.setShowSearchBefore(false);
                    } else {
                        ToastUtils.getInstance().showGlobalLong("请输入搜索内容");
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void setEditTextText() {
        searchShareViewModel.getKeywordLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String keyword) {
                Log.d(TAG, "onReceived: keyword" + keyword);
                etSearch.setText(keyword);
                etSearch.setSelection(keyword.length());
            }
        });
    }

    private void setFragment() {
        searchShareViewModel.isShowSearchBeforeLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isShowSearchBefore) {
                Log.d(TAG, "onReceived: isShowSearchBefore" + isShowSearchBefore);
                if (isShowSearchBefore) {
                    switchFragment(searchBeforeFragment);
                } else {
                    switchFragment(searchResultFragment);
                }
            }
        });
    }

    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment == null) {
            transaction.add(R.id.containerLayout, targetFragment)
                    .commit();
        } else {
            if (!targetFragment.isAdded()) {
                transaction.hide(currentFragment)
                        .add(R.id.containerLayout, targetFragment)
                        .commit();
            } else {
                transaction.hide(currentFragment)
                        .show(targetFragment)
                        .commit();
            }
        }
        currentFragment = targetFragment;
    }
}
