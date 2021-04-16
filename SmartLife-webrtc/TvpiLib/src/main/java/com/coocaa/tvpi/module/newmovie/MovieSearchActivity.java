package com.coocaa.tvpi.module.newmovie;

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
import com.coocaa.tvpi.base.mvvm.BaseViewModelProvideAppletActivity;
import com.coocaa.tvpi.module.newmovie.fragment.MovieSearchBeforeFragment;
import com.coocaa.tvpi.module.newmovie.fragment.MovieSearchResultFragment;
import com.coocaa.tvpi.module.newmovie.viewmodel.share.MovieSearchShareViewModel;
import com.coocaa.tvpi.view.DeletableEditText;
import com.coocaa.tvpilib.R;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

/**
 * 搜索界面
 * Created by songxing on 2020/7/14
 */
public class MovieSearchActivity extends BaseViewModelProvideAppletActivity {
    private static final String TAG = MovieSearchActivity.class.getSimpleName();

    private DeletableEditText etSearch;
    private MovieSearchBeforeFragment searchBeforeFragment;
    private MovieSearchResultFragment searchResultFragment;
    private MovieSearchShareViewModel shareViewModel;
    private Fragment currentFragment;

    public static void start(Context context) {
        Intent starter = new Intent(context, MovieSearchActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);
        initView();
        setFragment();
        setEditTextText();
    }


    private void initView() {
        etSearch = findViewById(R.id.etSearch);
        TextView tvCancel = findViewById(R.id.tvCancel);
        searchBeforeFragment = new MovieSearchBeforeFragment();
        searchResultFragment = new MovieSearchResultFragment();
        shareViewModel = ViewModelProviders.of(this).get(MovieSearchShareViewModel.class);
        shareViewModel.setShowSearchBefore(true);

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
                    Editable text = etSearch.getText();
                    if (text != null) {
                        shareViewModel.setSearchKeyword(text.toString());
                        shareViewModel.setShowSearchBefore(false);
                    } else {
                        ToastUtils.getInstance().showGlobalLong("请输入搜索内容");
                    }
                    return true;
                }
                return false;
            }
        });

        if(mHeaderHandler != null) {
            mHeaderHandler.setTitle("搜索");
        }
    }

    private void setEditTextText() {
        shareViewModel.getKeywordLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String keyword) {
                Log.d(TAG, "onChanged: keyword " + keyword);
                etSearch.setText(keyword);
                etSearch.setSelection(keyword.length());
            }
        });
    }

    private void setFragment() {
        shareViewModel.isShowSearchBeforeLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isShowSearchBefore) {
                Log.d(TAG, "onChanged: isShowSearchBefore " + isShowSearchBefore);
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
