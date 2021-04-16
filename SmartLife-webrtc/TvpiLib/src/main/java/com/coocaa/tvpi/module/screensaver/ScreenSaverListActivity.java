package com.coocaa.tvpi.module.screensaver;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.screensaver.adapter.ScreenSaverListAdapter;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;

import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * 壁纸
 */
public class ScreenSaverListActivity extends BaseActionBarActivity {

    private LoadTipsView loadTipsView;
    private RecyclerView listScreenSaver;
    private ScreenSaverListAdapter screenSaverListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver_list);

        initView();
        getScreenSaverList();
    }

    private void initView() {
        setTitle("开机屏保");
        listScreenSaver = findViewById(R.id.screen_saver_list);
        screenSaverListAdapter = new ScreenSaverListAdapter(ScreenSaverListActivity.this);
        listScreenSaver.setLayoutManager(new LinearLayoutManager(ScreenSaverListActivity.this,
                LinearLayoutManager.VERTICAL, false));
        listScreenSaver.addItemDecoration(new CommonVerticalItemDecoration(DimensUtils.dp2Px(
                this, getResources().getDimension(R.dimen.global_horizontal_margin_5))
                , DimensUtils.dp2Px(this, 20),
                DimensUtils.dp2Px(this, 50f)));
        listScreenSaver.setAdapter(screenSaverListAdapter);
        loadTipsView = findViewById(R.id.load_tips_view);
        loadTipsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getScreenSaverList();
            }
        });
    }

    private void getScreenSaverList() {
        NetWorkManager.getInstance()
                .getApiService()
                .getThemelist()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {

                    @Override
                    protected void onStart() {
                        loadTipsView.showLoading();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        if (isFinishing()) {
                            return;
                        }
                        Gson gson = new Gson();
//                        ScreenSaverListBean screenSaverListBean;
//                        try {
//                            screenSaverListBean = gson.fromJson(responseBody.string(), ScreenSaverListBean.class);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            screenSaverListBean = new ScreenSaverListBean();
//                        }
//                        screenSaverListAdapter.setList(screenSaverListBean.getRecommendInfoBeans());
                    }

                    @Override
                    public void onError(Throwable e) {
                        loadTipsView.showLoadingFailed();
                    }

                    @Override
                    public void onComplete() {
                        loadTipsView.showLoadingComplete();
                    }
                });
    }
}
