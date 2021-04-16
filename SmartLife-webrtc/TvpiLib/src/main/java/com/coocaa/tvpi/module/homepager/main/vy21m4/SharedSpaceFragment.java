package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageData;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.homepager.main.vy21m4.adapter.SharedSpaceAdapter;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @ClassName ShareSpaceFragment
 * @Description 共享空间页面
 * @User wuhaiyuan
 * @Date 4/6/21
 * @Version TODO (write something)
 */
public class SharedSpaceFragment extends BaseViewModelFragment<SharedSpaceModel> {

    public static final String TAG = SharedSpaceFragment.class.getSimpleName();

    private DefaultLoadStateView loadStateView;
    private RecyclerView recyclerView;
    private SharedSpaceAdapter adapter;

    private SSHomePageData ssHomePageData;

    public void setSSHomePageData(SSHomePageData ssHomePageData) {
        Log.d(TAG, "setSSHomePageData: ");
        this.ssHomePageData = ssHomePageData;
        updateView();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shared_space, container, false);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        updateView();
    }

    boolean firstTime = true;
    @Override
    public void onResume() {
        Log.d(TAG, "ssFragment onResume......");
        super.onResume();
        if(firstTime) {
            firstTime = false;
        } else {
            //手动刷新一下数量
            Log.d(TAG, "ssFragment refresh onResume......");
            adapter.notifyDataSetChanged();
        }
    }

    private void initView() {
        if (getView() == null || getContext() == null) return;
        loadStateView = getView().findViewById(R.id.shared_space_load_state_view);
        loadStateView.showLoadFinishView();

        recyclerView = getView().findViewById(R.id.shared_space_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(
                new CommonVerticalItemDecoration(
                        DimensUtils.dp2Px(getContext(), 8f),
                        DimensUtils.dp2Px(getContext(), 16f),
                        DimensUtils.dp2Px(getContext(), 60f)
                )
        );
        adapter = new SharedSpaceAdapter(getContext());
        recyclerView.setAdapter(adapter);

    }

    private void updateView() {
        if (ssHomePageData == null) {
            Log.d(TAG, "updateView: 可能被回收了，从全局cache获取数据");
            try {
                ssHomePageData = HomeHttpMethod.getInstance().getSSHomePageDataList().get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (adapter != null
                && ssHomePageData != null
                && ssHomePageData.blocks != null
                && !ssHomePageData.blocks.isEmpty()) {
            adapter.addAll(ssHomePageData.blocks);
        }
    }

}
