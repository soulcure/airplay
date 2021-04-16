package com.coocaa.tvpi.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentsList = new ArrayList<>();

    public MainAdapter(@NonNull FragmentManager fragmentManager,
                     @NonNull Lifecycle lifecycle,List<Fragment> fragmentsList) {
        super(fragmentManager,lifecycle);
        if(fragmentsList != null) {
            this.fragmentsList = fragmentsList;
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentsList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentsList.size();
    }
}
