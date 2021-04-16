package com.coocaa.tvpi.module.newmovie.adapter;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class MoviePagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments;

    public MoviePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public MoviePagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}