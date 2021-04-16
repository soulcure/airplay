package swaiotos.channel.iot.tv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.umeng.analytics.MobclickAgent;

import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.tv.init.InitFragment;
import swaiotos.channel.iot.tv.init.SmallFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(MainActivity.class.getName(), "" + Thread.currentThread().getId());

        Intent mIntent = getIntent();
        if (mIntent != null) {
            Bundle mBundle = mIntent.getExtras();
            if (mBundle != null) {
                String startType = mBundle.getString(Constants.COOCAA_START_TYPE, Constants.COOCAA_START_SWAIOTOS);
                if (!TextUtils.isEmpty(startType) && startType.equals(Constants.COOCAA_START_SMALL)) {
                    addSmallFragment();
                    return;
                }
            }

        }
        addFragment();
    }

    private void addFragment() {
        Log.d("tag", "addFragment1:" + System.currentTimeMillis());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        InitFragment initFragment = InitFragment.newInstance("key1", "key2");
        ft.replace(R.id.init_aiot_content, initFragment);
        ft.commit();
        Log.d("tag", "addFragment2:" + System.currentTimeMillis());
    }

    private void addSmallFragment() {
        Log.d("tag", "addFragment1:" + System.currentTimeMillis());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SmallFragment smallFragment = SmallFragment.newInstance("key1", "key2");
        ft.replace(R.id.init_aiot_content, smallFragment);
        ft.commit();
        Log.d("tag", "addFragment2:" + System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


}
