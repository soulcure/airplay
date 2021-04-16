package com.coocaa.tvpi.module.homepager.adapter;

import android.os.Parcelable;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SmartScreenPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments = new ArrayList<>();

    public SmartScreenPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public SmartScreenPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragments) {
        super(fragmentManager);
        mFragments = fragments;
    }

    public void setData(List<Fragment> fragments){
        if(fragments != null){
            mFragments = fragments;
            notifyDataSetChanged();
        }
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
