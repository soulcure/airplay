package swaiotos.channel.iot.tv;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import swaiotos.channel.iot.tv.init.InitFragment;
import swaiotos.channel.iot.tv.pad.PadInitFragment;

public class MainActivity extends AppCompatActivity {

    public static String module_type = "tv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(MainActivity.class.getName(), "" + Thread.currentThread().getId());

        findViewById(R.id.id_bindcode_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                module_type = "tv";
                findViewById(R.id.id_bindcode_btn).setVisibility(View.GONE);
                findViewById(R.id.id_input_bindcode_btn).setVisibility(View.GONE);
                addFragmentTV();
            }
        });

        findViewById(R.id.id_input_bindcode_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                module_type = "pad";
                findViewById(R.id.id_bindcode_btn).setVisibility(View.GONE);
                findViewById(R.id.id_input_bindcode_btn).setVisibility(View.GONE);
                addFragmentPAD();
            }
        });

//        addFragmentTV();
    }

    private void addFragmentTV() {
        Log.d("tag","addFragment1:"+System.currentTimeMillis());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        InitFragment initFragment = InitFragment.newInstance("key1","key2");
        ft.replace(R.id.init_aiot_content, initFragment);
        ft.commit();
        Log.d("tag","addFragment2:"+System.currentTimeMillis());
    }

    private void addFragmentPAD(){
        Log.d("tag","addFragment1:"+System.currentTimeMillis());
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        PadInitFragment initFragment = PadInitFragment.newInstance("key1","key2");
        ft.replace(R.id.init_aiot_content, initFragment);
        ft.commit();
        Log.d("tag","addFragment2:"+System.currentTimeMillis());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
