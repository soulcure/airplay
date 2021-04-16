package com.coocaa.tvpi.module.mall;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.BannerResult;
import com.coocaa.smartmall.data.mobile.data.ProductRecommendResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.base.BaseFragment;
import com.coocaa.tvpi.module.mall.adapter.MallBannerAdapter;
import com.coocaa.tvpi.module.mall.adapter.MallMainAdapter;
import com.coocaa.tvpi.module.mall.dialog.MallQuickStartDialog;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;


public class MallMainFragment extends BaseFragment{
    private RecyclerView storeRecyclerView;
    private MallMainAdapter storeMainAdapter;
    private Banner banner;
    private SpringView mSpringView;
    private LoadTipsView loadTipsView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mall_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }


    private void initView(View view) {
        storeRecyclerView = view.findViewById(R.id.recyclerview);
        storeMainAdapter = new MallMainAdapter();
        storeRecyclerView.setAdapter(storeMainAdapter);
        RecyclerView.LayoutManager layoutManager = new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL);
        storeRecyclerView.addItemDecoration(new MallMainAdapter.StaggeredGridDivider());
        storeRecyclerView.setLayoutManager(layoutManager);
        storeMainAdapter.setHeaderView(getHeaderView());
        List<ProductRecommendResult.DataBean> data = new ArrayList<>();
        storeMainAdapter.setList(data);
        storeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                String TAG="recycler";
                if(storeRecyclerView.canScrollVertically(1)){
//                    Log.i(TAG, "direction 1: true");//滑动到顶部
                }else {
//                    Log.i(TAG, "direction 1: false");
                }
                if(storeRecyclerView.canScrollVertically(-1)){
//                    Log.i(TAG, "direction -1: true");//滑动到底部
                    loadMore();
                }else {
//                    Log.i(TAG, "direction -1: false");
                }
            }
        });
        view.findViewById(R.id.mall_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MallQuickStartDialog().with((AppCompatActivity) getActivity()).show();
            }
        });
        storeMainAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                List<ProductRecommendResult.DataBean> datas= (List<ProductRecommendResult.DataBean>) adapter.getData();
                if(datas!=null&&datas.size()>0&&datas.size()>position){
                    //跳转详情
                    ProductRecommendResult.DataBean bean=datas.get(position);
                    if(bean!=null){
                        MallDetailActivity.start(getContext(),bean.getProduct_id());
                    }
                }
            }
        });
        mSpringView = view.findViewById(R.id.springView);
        mSpringView.setType(SpringView.Type.FOLLOW);
        if (mSpringView.getHeader() == null) {
            mSpringView.setHeader(new CustomHeader(getContext()));
        }
        if (mSpringView.getFooter() == null) {
            mSpringView.setFooter(new CustomFooter(getContext()));
        }
        mSpringView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadmore() {
//                loadMore();
            }
        });

        loadTipsView = view.findViewById(R.id.my_order_loadtipsview);
        loadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTipsView.setVisibility(View.VISIBLE);
                loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
                initBanner();
                initRecommend(pageIndex);
            }
        });
        loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        initBanner();
        initRecommend(pageIndex);
    }

    private void refresh(){
        initBanner();
        initRecommend(1);
    }
    private void loadMore(){
        int childCount=storeMainAdapter.getItemCount();
        if(childCount<totalSize){
             targetPageIndex= childCount/pageSize+1;
            if(targetPageIndex==pageIndex){
                return;
            }
            pageIndex=targetPageIndex;
            initRecommend(pageIndex);
        }else{
            mSpringView.onFinishFreshAndLoad();
        }
    }

    private View getHeaderView() {
       final List<BannerResult.DataBean> datas = new ArrayList<>();
        View view = getLayoutInflater().inflate(R.layout.header_mall_main, storeRecyclerView, false);
        MallBannerAdapter mallBannerAdapter=new MallBannerAdapter(getContext(),datas);
        banner = view.findViewById(R.id.banner);
        banner.setAdapter(mallBannerAdapter).addBannerLifecycleObserver(this).setIndicator(new CircleIndicator(getContext()));
        banner.setLoopTime(5*1000);
        return view;
    }
    int pageIndex=1;
    int pageSize=20;
    int totalSize=0;
    int targetPageIndex;
    private void initRecommend(final int page){

        MobileRequestService.getInstance().getRecommend(new HttpSubscribe<ProductRecommendResult>() {
            @Override
            public void onSuccess(ProductRecommendResult result) {
                mSpringView.onFinishFreshAndLoad();
                if(result!=null){
                    totalSize=result.getTotal();
                    List<ProductRecommendResult.DataBean> datas=  storeMainAdapter.getData();
                    if(result.getData()!=null&&result.getData().size()>0){
                        if(page==1){
                            datas.clear();
                            pageIndex=page;
                        }

                        datas.addAll(result.getData());
                        storeMainAdapter.notifyDataSetChanged();
                        loadTipsView.setVisibility(View.GONE);
                        return;
                    }
                }
                if(pageIndex==1){
                    loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_NODATA);
                }

            }

            @Override
            public void onError(HttpThrowable error) {
                loadTipsView.setLoadTipsIV(LoadTipsView.TYPE_FAILED);
                mSpringView.onFinishFreshAndLoad();
            }
        },page,pageSize);
    }
    private void initBanner(){
        MobileRequestService.getInstance().getBanner(new HttpSubscribe<BannerResult>() {
            @Override
            public void onSuccess(BannerResult result) {
                MallBannerAdapter adapter= (MallBannerAdapter) banner.getAdapter();
                adapter.setDatas(result.getData());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(HttpThrowable error) {

            }
        });
    }
}
