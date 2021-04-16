package com.coocaa.tvpi.test;

import android.os.Bundle;

import com.coocaa.smartscreen.R;
import com.coocaa.tvpi.base.BaseTitleBarActivity;
import com.coocaa.tvpi.module.homepager.main.vy21m4.SharedSpaceFragment;

import androidx.fragment.app.FragmentTransaction;

/**
 * @ClassName MainTestActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/7/21
 * @Version TODO (write something)
 */
public class MainTestActivity extends BaseTitleBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.add(new LinearLayout(this), new SharedSpaceFragment(), "tag").commit();
        transaction
                .setReorderingAllowed(true)
                .add(R.id.main_test_root, new SharedSpaceFragment(), "tag")
                .commitNowAllowingStateLoss();
    }
}