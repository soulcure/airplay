package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewParent;

import com.coocaa.publib.base.BaseAppletActivity;
import com.coocaa.tvpi.module.newmovie.fragment.MovieTabListFragment;
import com.coocaa.tvpi.module.newmovie.fragment.MovieTabMineFragment;
import com.coocaa.tvpilib.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

/**
 * 影视投屏首页
 * Created by songxing on 2020/7/9
 */
public class MovieHomeActivity extends BaseAppletActivity {
    private static final String TAG = MovieHomeActivity.class.getSimpleName();
    public String source;
    private final ArrayList<Fragment> fragmentsList = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, MovieHomeActivity.class);
        context.startActivity(starter);
    }

    //tvSource如果指定使用指定的，如果未指定获取当前连接设备的source，如果未连接默认使用爱奇艺source
    public static void start(Context context, @Nullable String source) {
        Intent starter = new Intent(context, MovieHomeActivity.class);
        starter.putExtra("source", source);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        parseIntent();
        initView();
    }

    private void parseIntent() {
        if (getIntent() != null) {
            source = getIntent().getStringExtra("source");
        }
    }

    private void initView() {
        MovieTabListFragment movieListFragment = new MovieTabListFragment();
        movieListFragment.setAppletInfo(mNPAppletInfo).setAppletHeaderHandler(mHeaderHandler);
        fragmentsList.add(movieListFragment);
        MovieTabMineFragment movieMineFragment = new MovieTabMineFragment();
        movieMineFragment.setAppletInfo(mNPAppletInfo).setAppletHeaderHandler(mHeaderHandler);
        fragmentsList.add(movieMineFragment);
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setCurrentItem(0);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navView);
        bottomNavigationView.setItemIconTintList(null);//除去自带效果
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.navigation_movie) {
                    if (mHeaderHandler != null) {
                        mHeaderHandler.setTitle("看影视");
                    }
                    viewPager.setCurrentItem(0);
                } else {
                    if (mHeaderHandler != null) {
                        mHeaderHandler.setTitle("我的");
                    }
                    viewPager.setCurrentItem(1);
                }
                return true;
            }
        });
    }


    private class MyAdapter extends FragmentPagerAdapter {
        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentsList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }
}
