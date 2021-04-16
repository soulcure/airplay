package com.coocaa.tvpi.module.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.coocaa.tvpi.base.mvvm.BaseViewModelProvideActivity;
import com.coocaa.tvpi.module.app.fragment.AppTabStoreFragment;
import com.coocaa.tvpi.module.app.fragment.AppTabTvFragment;
import com.coocaa.tvpi.module.app.viewmodel.share.AppHomeShareViewModel;
import com.coocaa.tvpi.module.viewmodel.ApplicationShareViewModel;
import com.coocaa.tvpilib.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.umeng.analytics.MobclickAgent;

/**
 * 应用首页
 * Created by songxing on 2020/7/15
 */
public class AppHomeActivity extends BaseViewModelProvideActivity {
    private static final String TAG = AppHomeActivity.class.getSimpleName();

    private BottomNavigationView bottomNavigationView;
    private ApplicationShareViewModel appShareViewModel;
    private AppHomeShareViewModel homeShareViewModel;
    private Fragment currentFragment;
    private Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean isLooping = false;

    public static void start(Context context) {
        Intent starter = new Intent(context, AppHomeActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_home);
        initView();
        observerNavVisible();
        observerInstallingAppState();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //这里调用的是observeForever需要手动反注册
        appShareViewModel.isLoopInstallingAppState().removeObserver(loopObserver);
        handler.removeCallbacks(getInstallingAppStateRunnable);
        getInstallingAppStateRunnable = null;
        handler = null;
    }

    private void initView() {
        appShareViewModel = getAppViewModelProvider().get(ApplicationShareViewModel.class);
        homeShareViewModel = ViewModelProviders.of(this).get(AppHomeShareViewModel.class);

        AppTabTvFragment appTabListFragment = new AppTabTvFragment();
        AppTabStoreFragment appTabStoreFragment = new AppTabStoreFragment();
        bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setItemIconTintList(null);//除去自带效果
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.navigation_tvapp) {
                    switchFragment(appTabListFragment);
                } else {
                    switchFragment(appTabStoreFragment);
                }
                return true;
            }
        });
        switchFragment(appTabListFragment);
    }

    //根据电视应用是否是编辑状态显示或隐藏BottomNavigationView
    private void observerNavVisible() {
        homeShareViewModel.isEditState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEdit) {
                Log.d(TAG, "isEditState onChanged: " + isEdit);
                bottomNavigationView.setVisibility(isEdit ? View.GONE : View.VISIBLE);
            }
        });
    }

    //轮询下载中apk的安装状态
    private void observerInstallingAppState() {
        Log.d(TAG, "observerInstallingAppState");
        appShareViewModel.isLoopInstallingAppState().observeForever(loopObserver);
    }

    private Observer<Boolean> loopObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean loop) {
            Log.d(TAG, "loopObserver onChanged: " + (loop ? "start loop" : "stop loop")
                    + "\n" + getInstallingAppStateRunnable);
            if (loop) {
                if (!isLooping) {
                    isLooping = true;
                    handler.post(getInstallingAppStateRunnable);
                    Log.d(TAG, "loopObserver onChanged: post loop runnable");
                }
            } else {
                if (isLooping) {
                    isLooping = false;
                    handler.removeCallbacks(getInstallingAppStateRunnable);
                    Log.d(TAG, "loopObserver onChanged: remove loop runnable");
                }
            }
        }
    };

    private Runnable getInstallingAppStateRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "loop runnable running...\n" + getInstallingAppStateRunnable);
            appShareViewModel.getInstallingAppState();
            handler.postDelayed(this, 10 * 1000);
        }
    };

    private void switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (currentFragment == null) {
            transaction.add(R.id.fragmentHost, targetFragment)
                    .commit();
        } else {
            if (!targetFragment.isAdded()) {
                transaction.hide(currentFragment)
                        .add(R.id.fragmentHost, targetFragment)
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
