package com.coocaa.tvpi.module.newmovie.fragment;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.data.BaseData;
import com.coocaa.smartscreen.data.movie.Episode;
import com.coocaa.smartscreen.data.movie.EpisodeListResp;
import com.coocaa.tvpi.module.movie.decoration.EpisodesItemDecoration;
import com.coocaa.tvpi.module.newmovie.adapter.EpisodesGridRecyclerAdapter;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by wuhaiyuan on 2018/3/30.
 */

public class EpisodeDialogFragment extends DialogFragment {

    public final static String DIALOG_FRAGMENT_TAG = EpisodeDialogFragment.class.getSimpleName();

    private static final String TAG = EpisodeDialogFragment.class.getSimpleName();
    private final static String COMMON_DIALOG_SERIALIZE_KEY = "COMMON_DIALOG_SERIALIZE_KEY";

    private View mLayout;
    private RecyclerView recyclerView;
    private EpisodesGridRecyclerAdapter adapter;
    private TextView promptInfoTV;
    private List<Episode> episodeList;
    private int selectedIndex = -1;
    private String promptInfo;

    private String third_album_id;
    private int page_size;
    private String video_type;
    private OnEpisodesCallback onEpisodesCallback;

    public void setLongVideoList(List<Episode> episodeList) {
        this.episodeList = episodeList;
        for (int i = 0; i < episodeList.size(); i++) {
            if(episodeList.get(i).isSelected){
                selectedIndex = i;
                break;
            }
        }
    }

    public void setLongVideoList(List<Episode> episodeList, int selectedIndex) {
        this.episodeList = episodeList;
        this.selectedIndex = selectedIndex;
    }

    public void setLongVideoData(String third_album_id, int page_size, int selectedIndex) {
        this.third_album_id = third_album_id;
        this.page_size = page_size;
        this.selectedIndex = selectedIndex;
    }

    public void setPromptInfo(String promptInfo) {
        this.promptInfo = promptInfo;
    }


    public void setVideoType(String type){
        video_type = type;
    }

    public void setOnEpisodesCallback(OnEpisodesCallback onEpisodesCallback) {
        this.onEpisodesCallback = onEpisodesCallback;
    }
    public interface OnEpisodesCallback {
        void onSelected(Episode episode, int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setCancelable(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置dialog的layout
        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
//        layoutParams.dimAmount = 0.0f;//去掉半透明阴影
        layoutParams.width = dm.widthPixels;
        layoutParams.height = layoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        getDialog().getWindow().setAttributes(layoutParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setWindowAnimations(R.style.animate_dialog);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置窗体的背景色为透明的

        mLayout = inflater.inflate(R.layout.episode_dialog_layout, container);
        initViews();
        if (!TextUtils.isEmpty(third_album_id) && page_size > 0)
            getVideoEpisodes(third_album_id, 0, page_size);
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void dismissDialog() {
        android.app.Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismissAllowingStateLoss();
        }
    }

    private void initViews() {
        recyclerView = mLayout.findViewById(R.id.episodes_grid_recycler);
        adapter = new EpisodesGridRecyclerAdapter(getActivity());
        adapter.setVideoType(video_type);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.addItemDecoration(new EpisodesItemDecoration(2, DimensUtils.dp2Px(getActivity(), 10f), DimensUtils.dp2Px(getActivity(), 10f)));
        adapter.addAll(episodeList);
        adapter.setSelectedIndex(selectedIndex);
        adapter.setOnItemClickListener(new EpisodesGridRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, Episode data) {
                selectedIndex = position;
                adapter.setSelectedIndex(position);
                if (null != onEpisodesCallback)
                    onEpisodesCallback.onSelected(data, position);
            }
        });

        mLayout.findViewById(R.id.episodes_close_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });

        if (!TextUtils.isEmpty(promptInfo)) {
            promptInfoTV = mLayout.findViewById(R.id.prompt_info_tv);
            promptInfoTV.setVisibility(View.VISIBLE);
            promptInfoTV.setText(promptInfo);
        }
    }

    private void getVideoEpisodes(String third_album_id, int page_index, int page_size) {
        HashMap<String,Object> params = new HashMap<>();
        params.put("third_album_id", third_album_id);
        params.put("page_index", page_index);
        params.put("page_size", page_size);
        NetWorkManager.getInstance()
                .getApiService()
                .getVideoEpisodesList(params)
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
                        IRLog.d(TAG, "getVideoEpisodes,onSuccess. response = " + response);
                        if(!TextUtils.isEmpty(response)){
                            EpisodeListResp episodeListResp = BaseData.load(response, EpisodeListResp.class);
                            if(episodeListResp != null && episodeListResp.data != null){
                                episodeList = episodeListResp.data;
                                adapter.addAll(episodeList);
                                adapter.setSelectedIndex(selectedIndex);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e)
                            IRLog.d(TAG, "getVideoEpisodes,onFailure,statusCode:" + e.toString());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
