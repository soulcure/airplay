package com.coocaa.tvpi.test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.data.channel.AppStoreParams;
import com.coocaa.smartscreen.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName AppTestActivity
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/9
 * @Version TODO (write something)
 */
public class AppTestActivity extends AppCompatActivity {

    String TAG = AppTestActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private AppTestAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_test);

        List<String> list = new ArrayList<>();
        for (AppStoreParams.CMD cmd :
                AppStoreParams.CMD.values()) {
            Log.d(TAG, "CMD: " + cmd.toString());
            list.add(cmd.toString());
        }

        recyclerView = findViewById(R.id.test_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppTestAdapter();
        recyclerView.setAdapter(adapter);

        adapter.addAll(list);
    }


}
