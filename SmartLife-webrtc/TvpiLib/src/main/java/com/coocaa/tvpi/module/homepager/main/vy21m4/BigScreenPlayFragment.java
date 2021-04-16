package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageBlock;
import com.coocaa.smartscreen.data.function.homepage.SSHomePageData;
import com.coocaa.smartscreen.repository.http.home.HomeHttpMethod;
import com.coocaa.tvpi.base.BaseFragment;
import com.coocaa.tvpi.module.homepager.adapter.BigScreenPlayAdapter;
import com.coocaa.tvpi.module.homepager.adapter.BigScreenPlayFunctionAdapter;
import com.coocaa.tvpi.module.homepager.adapter.InteractiveGamesAdapter;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.List;

/**
 * 大屏娱乐
 */
public class BigScreenPlayFragment extends BaseFragment {
    private static final String TAG = BigScreenPlayFragment.class.getSimpleName();
    private BigScreenPlayAdapter bigScreenPlayAdapter;
    private SSHomePageData ssHomePageData;

    public void setSSHomePageData(SSHomePageData ssHomePageData) {
        this.ssHomePageData = ssHomePageData;
        updateView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_big_screen_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        updateView();
    }


    private void initView() {
        if (getView() == null) return;
        RecyclerView rvInteractiveGames = getView().findViewById(R.id.rv_big_screen_play);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rvInteractiveGames.setLayoutManager(layoutManager);
        int dp16 = DimensUtils.dp2Px(getContext(), 16);
        int dp35 = DimensUtils.dp2Px(getContext(), 35);
        CommonVerticalItemDecoration decoration = new CommonVerticalItemDecoration(dp16, dp16, dp35);
        rvInteractiveGames.addItemDecoration(decoration);
        bigScreenPlayAdapter = new BigScreenPlayAdapter();
        rvInteractiveGames.setAdapter(bigScreenPlayAdapter);
    }

    private void updateView() {
        if (ssHomePageData == null) {
            Log.d(TAG, "updateView: 可能被回收了，从全局cache获取数据");
            try {
                ssHomePageData = HomeHttpMethod.getInstance().getSSHomePageDataList().get(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (ssHomePageData != null) {
            List<SSHomePageBlock> blocks = ssHomePageData.blocks;
            if (blocks != null && !blocks.isEmpty()) {
                if (bigScreenPlayAdapter != null) {
                    bigScreenPlayAdapter.setList(blocks);
                }
            }
        }
    }
}
