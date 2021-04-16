package com.coocaa.tvpi.module.newmovie.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.UIHelper;
import com.coocaa.smartscreen.data.movie.CollectionModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseFragment;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.newmovie.adapter.CollectListAdapter;
import com.coocaa.tvpi.module.newmovie.widget.ListFooterView;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.coocaa.tvpi.common.UMEventId.CLICK_COLLECT_DELETE;
import static com.coocaa.tvpi.common.UMEventId.CLICK_COLLECT_ITEM;

/**
 * Created by IceStorm on 2018/3/2.
 */

public class CollectWallFragment extends BaseFragment {

    private static final String TAG = "CollectWallFragment";

    private View mLayout;

    private SpringView springView;

    private ListView lvCollect;
    private CollectListAdapter adapter;


    private LoadTipsView mLoadTipsView;
    private RelativeLayout rlEditPanel;

    private TextView tvSelectAll;
    private TextView tvSelectNumber;

    private int pageSize = 15;
    private int pageIndex = 0;

    // isAddMore判断adapter是调用addmore还是addall
    private boolean isAddMore = false;
    private boolean isHasMore = true;

    private boolean isInEditMode = false;
    private boolean isSelectAll = false;

    // 删除完，判断是否还有数据没有请求下来，没有则展示无数据提示
    private int totalDataNumber = 0;

    // 视频类型,0:短片,1:正片
    private int videoType;

    private List<CollectionModel> collectionModelList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_collect_wall, container, false);

        EventBus.getDefault().register(this);

        return mLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        initViews();

        mLoadTipsView.setVisibility(View.VISIBLE);
        mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        queryData(pageIndex, pageSize);
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
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UserLoginEvent userLoginEvent) {
        if (userLoginEvent.isLogin) {
            if (mLoadTipsView != null) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
            }

            pageIndex = 0;
            isAddMore = false;
            queryData(pageIndex, pageSize);
        }
    }

    private void initViews() {
        mLoadTipsView = (LoadTipsView) mLayout.findViewById(R.id.load_tips_view_collect);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

                pageIndex = 0;
                isAddMore = false;
                queryData(pageIndex, pageSize);
            }
        });

        rlEditPanel = mLayout.findViewById(R.id.collect_rl_edit);

        tvSelectAll = mLayout.findViewById(R.id.collect_tv_select_all);
        tvSelectNumber = mLayout.findViewById(R.id.collect_tv_number);

        tvSelectNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除操作
                if (isInEditMode) {
                    MobclickAgent.onEvent(getActivity(), CLICK_COLLECT_DELETE);

                    if (adapter.getSelectedVideoIdList().size() > 0) {
                        // 网络请求，成功之后，判断请求回来时，activity是否还存在。如果是全部删除了，则需要给无数据提示
                        deleteData();
                    } else {

                        ToastUtils.getInstance().showGlobalShort(getString(R.string.collect_history_no_selected_data));
                    }
                }
            }
        });

        tvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInEditMode) {
                    isSelectAll = !isSelectAll;

                    // 如果是全选，更新标题并改变数据状态为选中
                    if (isSelectAll) {
                        tvSelectAll.setText("取消全选");
                        adapter.updateEditModeSelectAllStatus();

                        updateEditModelSelectNumber();
                    } else {
                        // 如果是全反选，更新标题并改变数据状态为未选中
                        tvSelectAll.setText("全选");
                        adapter.updateEditModeCancelSelectAllStatus();

                        updateEditModelSelectNumber();
                    }
                }
            }
        });

        lvCollect = (ListView) mLayout.findViewById(R.id.lv_collect);
        lvCollect.addFooterView(new ListFooterView(getActivity()));
        adapter = new CollectListAdapter(getActivity());

        lvCollect.setAdapter(adapter);
        lvCollect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isInEditMode) {
                    // 进行选中状态反转操作
                    adapter.updateEditModeSelectStatus(position);

                    // 获取选中的数量并展示出来
                    updateEditModelSelectNumber();
                } else {
                    try {
                        CollectionModel video = (CollectionModel) adapter.getItemAtIndex(position);
                        if (video != null) {
                            UIHelper.startActivityByURL(getActivity(), video.router);

                            MobclickAgent.onEvent(getActivity(), CLICK_COLLECT_ITEM);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        springView = (SpringView) mLayout.findViewById(R.id.collect_spring_view);
        springView.setType(SpringView.Type.FOLLOW);
        if (springView.getHeader() == null) {
            springView.setHeader(new CustomHeader(getActivity()));
        }
//        springView.setFooter(new DefaultFooter(this));
        if (springView.getFooter() == null) {
            springView.setFooter(new CustomFooter(getActivity()));
        }

        //下拉刷新/上拉加载监听
        springView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                isAddMore = false;
                pageIndex = 0;
                queryData(pageIndex, pageSize);
            }

            @Override
            public void onLoadmore() {
                if (isHasMore) {
                    isAddMore = true;
                    queryData(pageIndex, pageSize);
                } else {
                    springView.onFinishFreshAndLoad();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });
    }

    public void setVideoType(int type) {
        videoType = type;
    }

    public void setEditMode(boolean isInEdit) {
        isInEditMode = isInEdit;

        adapter.setMode(isInEditMode);
        // 进行数据的处理，编辑模式下则将所有数据标记为编辑模式
        if (isInEditMode) {
            tvSelectAll.setText("全选");
            tvSelectNumber.setText("删除");
            tvSelectNumber.setTextColor(getResources().getColor(R.color.colorText_9d9d9d));

            rlEditPanel.setVisibility(View.VISIBLE);

//            setRightButton("取消");
        } else {
            // 非编辑模式下，则将所有数据编辑为非编辑模式并且选中状态为no
            rlEditPanel.setVisibility(View.GONE);

//            setRightButton("编辑");
        }
    }

    private void deleteData() {
        // 不在编辑模式下
        if (!isInEditMode) {
            return;
        }

        // 返回的选中数量为0,给提示
        final List<Integer> selectedList = adapter.getSelectedVideoIdList();
        if (selectedList.size() == 0) {
            ToastUtils.getInstance().showGlobalShort(getString(R.string.collect_history_no_selected_data));
            return;
        }

        Repository.get(MovieRepository.class)
                .deleteCollectionList(selectedList)
                .setCallback(new BaseRepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (getActivity() == null) {
                            return;
                        }

                        // 获取服务器剩余的数据
                        totalDataNumber -= selectedList.size();
                        Log.d(TAG, "totalDataNumber: " + totalDataNumber);
                        // 删除成功，通知adapter去删除数据
                        if (totalDataNumber <= 0) {
                            adapter.updateDataAfterDeleteSuccess(selectedList);
                            rlEditPanel.setVisibility(View.GONE);

                            // 通知activity设置右键文字
//                            setRightButton("编辑");

                            isInEditMode = false;

                            mLoadTipsView.setVisibility(View.VISIBLE);
//                            mLoadTipsView.setLoadTips(getResources().getString(R.string.title_loading_data_empty), LoadTipsView.TYPE_FAILED);
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);

                            // 更新总数
                            if (mDataNumCallback != null) {
                                mDataNumCallback.onCollectDataNumber(0);
                            }
                        } else {
                            adapter.updateDataAfterDeleteSuccess(selectedList);
                            mLoadTipsView.setVisibility(View.GONE);
                            // 更新总数
                            if (mDataNumCallback != null) {
                                mDataNumCallback.onCollectDataNumber(totalDataNumber);
                            }

                            if (adapter.getCount() == 0) {
                                Log.d(TAG, "adapter data empty, to require more data");
                                mLoadTipsView.setVisibility(View.VISIBLE);
                                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

                                isAddMore = false;
                                pageIndex = 0;
                                queryData(pageIndex, pageSize);
                            }
                        }

                        tvSelectAll.setText("全选");
                        updateEditModelSelectNumber();
                        updateTitle(totalDataNumber);

                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (getActivity() == null) {
                            return;
                        }
                        // 取消mLoadTipsView进度条
                        mLoadTipsView.setVisibility(View.GONE);
                        ToastUtils.getInstance().showGlobalShort(getString(R.string.collect_history_delete_data_fail));
                    }
                });
    }

    private void queryData(int page, int pageSize) {
        Repository.get(MovieRepository.class)
                .getCollectionList(videoType, page, pageSize)
                .setCallback(new BaseRepositoryCallback<List<CollectionModel>>() {
                    @Override
                    public void onSuccess(List<CollectionModel> collectionModels) {
                        super.onSuccess(collectionModels);
                        if (getActivity() == null) {
                            return;
                        }
                        collectionModelList = collectionModels;
                        springView.onFinishFreshAndLoad();

                        if (collectionModels != null && collectionModels.size() > 0) {
                            if (isInEditMode) {
                                for (CollectionModel temp : collectionModels) {
                                    temp.isInEditMode = true;
                                    temp.isSelected = false;
                                }
                            }

                            updateViews();


                        } else {
                            notHaveData();
                            // 更新总数
                            if (mDataNumCallback != null) {
                                mDataNumCallback.onCollectDataNumber(0);
                            }
                        }
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (getActivity() == null) {
                            return;
                        }

                        springView.onFinishFreshAndLoad();
                        if (isAddMore) {
                            // 如果在加载下一页时网络出错，下一次加载数据还是该index
                            /*isAddMore = false;*/
                            ToastUtils.getInstance().showGlobalShort(getString(R.string.loading_tip_net_error));
                        } else {
                            mLoadTipsView.setVisibility(View.VISIBLE);
//                    mLoadTipsView.setLoadTips(getResources().getString(R.string.pull_to_refresh_network_error), LoadTipsView.TYPE_FAILED);
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                        }
                    }
                });
    }

    private void updateViews() {
        if (isAddMore) {
            /*isAddMore = false;*/
            adapter.addMore(collectionModelList);
        } else {
            adapter.addAll(collectionModelList);
            totalDataNumber = collectionModelList.size();

            // 下拉刷新，编辑模式下，选中状态和数量都恢复至默认
            if (isInEditMode) {
                updateEditModelSelectNumber();
                tvSelectAll.setText("全选");
            }
        }

        pageIndex++;
        isHasMore = (collectionModelList.size() % pageSize == 0);
        // 更新总数
        if (mDataNumCallback != null) {
            mDataNumCallback.onCollectDataNumber(adapter.getCount());
        }
        mLoadTipsView.setVisibility(View.GONE);
    }

    /**
     * 显示暂无记录,或者是没有更多
     */
    private void notHaveData() {
        if (!isAddMore) {
            //第一次进来初始化数据
//            mLoadTipsView.setLoadTips(getString(R.string.collect_no_data),LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
            return;
        }

        if (isAddMore) {
            /*isAddMore = false;*/
            ToastUtils.getInstance().showGlobalShort(getString(R.string.loading_tip_no_more_data));
        } else {
//            mLoadTipsView.setLoadTips(getString(R.string.collect_no_data),LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载数据失败
     */
    private void loadDataFail() {
        if (isAddMore) {
            /*isAddMore=false;*/
            springView.onFinishFreshAndLoad();
            ToastUtils.getInstance().showGlobalShort(getString(R.string.loading_tip_server_busy));
        } else {
//            mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
        }
    }

    private void updateEditModelSelectNumber() {
        int num = adapter.getSelectedVideoIdList().size();

        if (isInEditMode) {
            if (num > 0) {
                tvSelectNumber.setText("删除" + "（" + num + "）");
                tvSelectNumber.setTextColor(getResources().getColor(R.color.colorText_ff6686));
            } else {
                tvSelectNumber.setText("删除");
                tvSelectNumber.setTextColor(getResources().getColor(R.color.colorText_9d9d9d));
            }
        }
    }

    private void updateTitle(int count) {
//        if(count <= 0) {
//            setTitle("收藏");
//        } else {
//            setTitle("收藏" + count);
//        }
        // 代理通知
    }

    public interface CollectDataNumberCallback {
        void onCollectDataNumber(int dataNumber);
    }

    private CollectDataNumberCallback mDataNumCallback;

    public void setOnCollectDataNumberCallback(CollectDataNumberCallback callback) {
        mDataNumCallback = callback;
    }
}
