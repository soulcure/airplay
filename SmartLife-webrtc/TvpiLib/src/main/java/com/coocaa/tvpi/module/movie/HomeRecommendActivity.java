package com.coocaa.tvpi.module.movie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.category.MultiTypeEnum;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.data.movie.LongVideoListResp;
import com.coocaa.tvpi.module.movie.util.CategoryFilterDataHelper;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.MultiTypeAdapter;
import okhttp3.ResponseBody;

public class HomeRecommendActivity extends BaseActionBarActivity {

    private static final String TAG = HomeRecommendActivity.class.getSimpleName();

    private static final String KEY_TARGET_ID = "target_id";
    private static final String KEY_TITLE = "title";

    private LoadTipsView mLoadTipsView;
    private SpringView mSpringView;
    private RecyclerView mRecyclerView;

    private String target_id;//新增的类型有字母，以后就用String类型请求数据
    private String title;

    private LongVideoListResp longVideoListResp;
    private MultiTypeAdapter adapter;
    private CategoryFilterDataHelper helper;
    List<Object> items = new ArrayList<>();

    public static void start(Context context,String targetId,String title) {
        Intent starter = new Intent(context, HomeRecommendActivity.class);
        starter.putExtra(KEY_TARGET_ID,targetId);
        starter.putExtra(KEY_TITLE,title);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_recommend);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        Intent intent = getIntent();
        if (intent != null) {
            target_id = intent.getStringExtra(KEY_TARGET_ID);
            title = intent.getStringExtra(KEY_TITLE);
            if(TextUtils.isEmpty(title)) {
                title = "";
            }
            setTitle(title);
            /*try {
                String extra = intent.getStringExtra(KEY_TARGET_ID);
                title = intent.getStringExtra(KEY_TITLE);
                if (TextUtils.isEmpty(extra)) {
                    extra = "0";
                }
                target_id = Integer.parseInt(extra);

                if(TextUtils.isEmpty(title)) {
                    title = "";
                }
                setTitle(title);
            } catch (Exception e) {
                target_id = 0;
            }*/
        }

        initViews();
        getRecommendData();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    private void initViews() {
        mLoadTipsView = findViewById(R.id.activity_home_recommend_load_tips_view);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecommendData();
            }
        });
        mSpringView = findViewById(R.id.activity_home_recommend_spring_view);
        mRecyclerView = findViewById(R.id.activity_home_recommend_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new MultiTypeAdapter();
        helper = new CategoryFilterDataHelper(adapter, "首页_" + title);
        mRecyclerView.setAdapter(adapter);
    }

    private void getRecommendData() {
        mLoadTipsView.setVisibility(View.VISIBLE);
        mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

        HashMap<String, Object> params = new HashMap<>();
        params.put("target_id",target_id);
        NetWorkManager.getInstance()
                .getApiService()
                .getRecommendMoreList(ParamsUtil.getQueryMap(params))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        IRLog.d(TAG, "getRecommendData onSuccess. response = " + response);

                        if (HomeRecommendActivity.this == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (!TextUtils.isEmpty(response)) {
                            longVideoListResp = BaseData.load(response, LongVideoListResp.class);
                            if (longVideoListResp != null
                                    && longVideoListResp.data != null
                                    && longVideoListResp.data.size() > 0) {
                                updateViews();
                            } else {
                                notHaveData();
                            }
                        } else {
                            loadDataFail();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        IRLog.d(TAG, "getRecommendData onError: ");
                        if (null != e)
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());

                        if (HomeRecommendActivity.this == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }
                        mLoadTipsView.setVisibility(View.VISIBLE);
                        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateViews(){
        for(LongVideoListModel temp : longVideoListResp.data) {
//            temp.container_type = MultiTypeEnum.COLUMS_3;
        }
        items.clear();
        items.addAll(longVideoListResp.data);
        helper.addAll(items);

        mSpringView.onFinishFreshAndLoad();
        mLoadTipsView.setVisibility(View.GONE);
    }

    /**
     * 显示暂无记录,或者是没有更多
     */
    private void notHaveData() {
        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
        mSpringView.onFinishFreshAndLoad();
    }

    /**
     * 加载数据失败
     */
    private void loadDataFail() {
        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
        mSpringView.onFinishFreshAndLoad();
    }

}
