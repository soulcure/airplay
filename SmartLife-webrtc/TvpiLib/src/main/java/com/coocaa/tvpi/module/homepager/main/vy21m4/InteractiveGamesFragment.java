package com.coocaa.tvpi.module.homepager.main.vy21m4;

import android.os.Bundle;
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
import com.coocaa.tvpi.base.BaseFragment;
import com.coocaa.tvpi.module.homepager.adapter.InteractiveGamesAdapter;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.List;

/**
 * 互动游戏
 */
public class InteractiveGamesFragment extends BaseFragment {
    private static final String TAG = InteractiveGamesFragment.class.getSimpleName();
    private InteractiveGamesAdapter interactiveGamesAdapter;
    private SSHomePageData ssHomePageData;

    public void setSSHomePageData(SSHomePageData ssHomePageData) {
        this.ssHomePageData = ssHomePageData;
        updateView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interactive_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        updateView();
    }


    private void initView() {
        if (getView() == null) return;
        RecyclerView rvInteractiveGames = getView().findViewById(R.id.rv_interactive_games);
        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        rvInteractiveGames.setLayoutManager(layoutManager);
        int dp25 = DimensUtils.dp2Px(getContext(), 25);
        PictureItemDecoration decoration = new PictureItemDecoration(3, dp25, 0);
        rvInteractiveGames.addItemDecoration(decoration);
        interactiveGamesAdapter = new InteractiveGamesAdapter();
        rvInteractiveGames.setAdapter(interactiveGamesAdapter);
    }

    private void updateView() {
        if (ssHomePageData != null) {
            List<SSHomePageBlock> blocks = ssHomePageData.blocks;
            if (blocks != null && !blocks.isEmpty()) {
                SSHomePageBlock pageBlock = blocks.get(0);
                if (pageBlock != null)
                    if (interactiveGamesAdapter != null) {
                        interactiveGamesAdapter.setList(pageBlock.contents);
                    }
            }
        }
    }
}
