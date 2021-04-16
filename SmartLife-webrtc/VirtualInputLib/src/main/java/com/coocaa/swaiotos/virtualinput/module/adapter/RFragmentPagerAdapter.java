package com.coocaa.swaiotos.virtualinput.module.adapter;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * @ClassName RFragmentPagerAdapter
 * @Description TODO (write something)
 * @User heni
 * @Date 2020/12/17
 */
public class RFragmentPagerAdapter extends FragmentStateAdapter {

    private SparseArray<Fragment> mFragments;

    public RFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                 SparseArray<Fragment> fragments) {
        super(fragmentActivity);
        mFragments = fragments;
    }

    public RFragmentPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public RFragmentPagerAdapter(@NonNull FragmentManager fragmentManager,
                                 @NonNull Lifecycle lifecycle, SparseArray<Fragment> fragments) {
        super(fragmentManager, lifecycle);
        mFragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.valueAt(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }
}
