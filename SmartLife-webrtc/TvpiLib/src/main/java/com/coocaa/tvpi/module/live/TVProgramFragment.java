package com.coocaa.tvpi.module.live;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.tvlive.TVLiveChannelListData;
import com.coocaa.publib.data.tvlive.TVLiveChannelsData;
import com.coocaa.publib.data.tvlive.TVLiveProgramResp;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.publib.utils.SpUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.tvpi.module.live.adapter.TVLiveProgramAdapter;
import com.coocaa.tvpi.module.live.listener.LiveItemTouchHelperCallback;
import com.coocaa.tvpi.view.LiveHeader;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * @ClassName TVProgramFragment
 * @Description TODO (write something)
 * @User WHY
 * @Date 2019/1/17
 * @Version TODO (write something)
 */
public class TVProgramFragment extends Fragment implements TVLiveProgramAdapter.OnDeleteAllItemListener {

    private static final String TAG = TVProgramFragment.class.getSimpleName();

    private View mLayout;
    private RecyclerView mProgramRecyclerView;
    private TVLiveProgramAdapter mProgramAdapter;
    private SpringView mSpringView;
    private LoadTipsView mLoadTipsView;
    private ImageView mImgNoCollectDataTipsView;
    private ProgressBar mProgressBar;

    private boolean isRefresh;
    private boolean isAddMore;

    private String mClassName;
    private TVLiveProgramResp programResp;
    private String networkForceKey;


    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        Log.d(TAG, "onCreateView: " + mClassName);
        mLayout = inflater.inflate(R.layout.fragment_tv_program, container, false);
        initViews();
        // 已经初始化
        isPrepared = true;
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: " + mClassName);
        // 配置setUserVisibleHint（）方法
        setUserVisibleHint(true);
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

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: " + mClassName);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser);
        //可见的并且是初始化之后才加载
        if (isPrepared && isVisibleToUser && !TextUtils.isEmpty(mClassName)) {
            if (mClassName.equals(TVLiveFragment.MY_LOCAL_COLLECT)) {
                Log.d(TAG, "setUserVisibleHint: ------queryLocalCollectData-----" + mClassName);
                queryLocalCollectData();
            } else {
                Log.d(TAG, "setUserVisibleHint: ------queryProgramData-----" + mClassName);
                queryProgramData(mClassName);
            }
        }
    }

    @Override
    public void onDeleteAllItem() {
        mImgNoCollectDataTipsView.setVisibility(View.VISIBLE);
    }

    @Override public void onHiddenChanged(boolean hidden) {
        Log.d(TAG, "onHiddenChanged: " +  hidden + "  name：" + mClassName);
        super.onHiddenChanged(hidden);
        if (hidden) {
            //Fragment隐藏时调用

        }else {
            //Fragment显示时调用
        }
        if (isPrepared && !hidden && !TextUtils.isEmpty(mClassName)) {
            if (mClassName.equals(TVLiveFragment.MY_LOCAL_COLLECT)) {
                Log.d(TAG, "onHiddenChanged: ------queryLocalCollectData-----" + mClassName);
                queryLocalCollectData();
            } else {
                Log.d(TAG, "onHiddenChanged: ------queryProgramData-----" + mClassName);
                queryProgramData(mClassName);
            }
        }
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public void setNetworkForceKey(String networkForceKey) {
        this.networkForceKey = networkForceKey;
    }

    private void initViews() {
        mImgNoCollectDataTipsView = mLayout.findViewById(R.id.tv_program_no_collect_data_tips);
        mProgressBar = mLayout.findViewById(R.id.load_progress);
        mLoadTipsView = mLayout.findViewById(R.id.tv_program_loadtipview);
        mLoadTipsView.setRootBackground(R.color.white);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
                queryProgramData(mClassName);
            }
        });
        mSpringView = mLayout.findViewById(R.id.tv_program_springview);
        mSpringView.setType(SpringView.Type.FOLLOW);
        if (mSpringView.getHeader() == null) {
            mSpringView.setHeader(new LiveHeader(getActivity()));
        }
        /*if (mSpringView.getFooter() == null) {
            mSpringView.setFooter(new CustomFooter(getActivity()));//禁止下拉刷新 直接不设置footer
        }*/

        //下拉刷新/上拉加载监听
        mSpringView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                queryProgramData(mClassName);
            }

            @Override
            public void onLoadmore() {
                isAddMore = true;
            }
        });

        mProgramRecyclerView = mLayout.findViewById(R.id.tv_program_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        mProgramRecyclerView.setLayoutManager(layoutManager);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(0, 0,
                DimensUtils.dp2Px(getActivity(), 70f));
        mProgramRecyclerView.addItemDecoration(decoration);
        mProgramAdapter = new TVLiveProgramAdapter(getActivity(), this);
        mProgramAdapter.setNetworkForceKey(networkForceKey);
        mProgramRecyclerView.setAdapter(mProgramAdapter);

        if(!TextUtils.isEmpty(mClassName) && mClassName.equals(TVLiveFragment.MY_LOCAL_COLLECT)) {
            ItemTouchHelper.Callback callback = new LiveItemTouchHelperCallback(mProgramAdapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(mProgramRecyclerView);
        }
    }

    private void queryProgramData(String class_name) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("class", class_name);
        NetWorkManager.getInstance()
                .getApiService()
                .getTVLiveChannelList(ParamsUtil.getQueryMap(params))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DefaultObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        // Log.d(TAG, "URL_TVLIVE_CHANNEL_CLASS onResponse: " + response);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        String response = "";
                        try {
                            response = responseBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (TVProgramFragment.this == null || getActivity() == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (response != null) {
                            programResp = BaseData.load(response, TVLiveProgramResp.class);
                            if (programResp != null && programResp.data != null && programResp.data
                                    .channels != null) {
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
                        if (e != null)
                            IRLog.e(TAG, "tvlive channel list onError: " + e.getMessage());

                        if (TVProgramFragment.this == null || getActivity() == null) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        mProgressBar.setVisibility(View.INVISIBLE);

                        if (isRefresh || isAddMore) {
                            isRefresh = false;
                            isAddMore = false;
                            mSpringView.onFinishFreshAndLoad();
                            ToastUtils.getInstance().showGlobalShort(R.string.loading_tip_net_error);
                        } else {
                            mLoadTipsView.setVisibility(View.VISIBLE);
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateViews() {
        isRefresh = false;
        isAddMore = false;
        mProgramAdapter.addAll(programResp.data);
        mSpringView.onFinishFreshAndLoad();
        mLoadTipsView.setVisibility(View.GONE);
    }

    /**
     * 显示暂无记录,或者是没有更多
     */
    private void notHaveData() {
        if (!isRefresh && !isAddMore) {
            //第一次进来初始化数据
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
            return;
        }

        if (isAddMore) {
            isAddMore = false;
            ToastUtils.getInstance().showGlobalShort(R.string.loading_tip_no_more_data);
        }

        mSpringView.onFinishFreshAndLoad();
    }

    /**
     * 加载数据失败
     */
    private void loadDataFail() {
        if (isRefresh || isAddMore) {
            isRefresh = false;
            isAddMore = false;
            mSpringView.onFinishFreshAndLoad();
            ToastUtils.getInstance().showGlobalShort(R.string.loading_tip_server_busy);
        } else {
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 查询本地sp存储的已收藏文件
     */
    private void queryLocalCollectData() {
        mLoadTipsView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.INVISIBLE);
        List<TVLiveChannelsData> tvLiveChannelsDataList = SpUtil.getList(getActivity(), SpUtil.Keys
                .TVLIVE_COLLECT_PROGRAMS);

        if (null != tvLiveChannelsDataList && tvLiveChannelsDataList.size() > 0) {
            mImgNoCollectDataTipsView.setVisibility(View.GONE);
            TVLiveChannelsData data = new TVLiveChannelsData();
            data.channel_name = "EDIT_BTN";
            tvLiveChannelsDataList.add(data);

            TVLiveChannelListData dataList = new TVLiveChannelListData();
            dataList.channels_class = TVLiveFragment.MY_LOCAL_COLLECT;
            dataList.channels = tvLiveChannelsDataList;
            mProgramAdapter.addAll(dataList);
        } else {
            //当sp存储的已收藏item为null，或者不为null但是size为0，都显示提示语
            mImgNoCollectDataTipsView.setVisibility(View.VISIBLE);
        }
    }

}
