package com.swaiotos.testdemo_pad;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @ProjectName: iot-channel-app
 * @Package: com.swaiotos.testdemo_pad
 * @ClassName: TestActivity
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/6/2 15:50
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/6/2 15:50
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
