package swaiotos.channel.iot.tv.init.devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.init.InitFragment;
import swaiotos.channel.iot.tv.init.SmallFragment;

public class DevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(DevicesActivity.class.getName(), "" + Thread.currentThread().getId());

        addFragment();
    }

    private void addFragment() {
        Intent deviceIntent = getIntent();
        Bundle bundle = deviceIntent.getExtras();
//        ArrayList<Device> devices = bundle.getParcelableArrayList("device");
        Log.d("tag","addFragment1:"+System.currentTimeMillis());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DevicesFragment devicesFragment = DevicesFragment.newInstance();
        ft.replace(R.id.init_aiot_content, devicesFragment);
        ft.commit();
        Log.d("tag","addFragment2:"+System.currentTimeMillis());
    }

}
