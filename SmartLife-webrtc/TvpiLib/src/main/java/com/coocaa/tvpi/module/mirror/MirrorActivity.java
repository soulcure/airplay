package com.coocaa.tvpi.module.mirror;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.module.homepager.newmainpage.ShortcutCommandNewLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * @Author: yuzhan
 */
public class MirrorActivity extends BaseActivity {
    private final String TAG = "SmartMirror";

    private RelativeLayout layout;
    private ShortcutCommandNewLayout shortcutCommandLayout;
    private FragmentManager manager;
    private MirrorFragment fragment;
    private boolean openMirror = false;
    private static boolean isOpening = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setId(10086);
        setContentView(layout);

        shortcutCommandLayout = new ShortcutCommandNewLayout(this);
        layout.addView(shortcutCommandLayout);

        fragment = new MirrorFragment();
        manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(10086, fragment, "blankFragment");
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
//        super.onSaveInstanceState(outState, outPersistentState);
    }
}
