package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.PushHistoryModel;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.MovieRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.newmovie.adapter.PushHistoryActivityAdapter;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * @ClassName PushHistoryActivity
 * @Description
 * @User heni
 * @Date 18-8-23
 */
public class PushHistoryActivity extends BaseActionBarActivity {

    private static final String TAG = "PushHistoryActivity";

    private SpringView springView;
    private RecyclerView recyclerView;
    private PushHistoryActivityAdapter adapter;

    private LoadTipsView mLoadTipsView;
    private RelativeLayout rlEditPanel;
    private TextView tvSelectAll;
    private TextView tvSelectNumber;

    private boolean isInEditMode = false;
    private boolean isSelectAll = false;

    // 删除完，判断是否还有数据没有请求下来，没有则展示无数据提示
    private int totalDataNumber = 0;

    private PushHistoryModel pushHistoryModel;


    public static void start(Context context) {
        Intent starter = new Intent(context, PushHistoryActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_history);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        setTitle("历史共享");
        setRightButton("编辑");
        initViews();

        mLoadTipsView.setVisibility(View.VISIBLE);
        mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
        queryData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initViews() {
        mLoadTipsView = (LoadTipsView) findViewById(R.id.push_history_loadtipsview);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

                queryData();
            }
        });

        rlEditPanel = findViewById(R.id.push_history_rl_edit);
        tvSelectAll = findViewById(R.id.push_history_tv_select_all);
        tvSelectNumber = findViewById(R.id.push_history_tv_number);

        tvSelectNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 删除操作
                if (isInEditMode) {
//                    MobclickAgent.onEvent(PushHistoryActivity.this, CLICK_push_push_history_DELETE);
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

        adapter = new PushHistoryActivityAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.push_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new CommonVerticalItemDecoration(0, 0, DimensUtils
                .dp2Px(this, 50f)));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickSelectListener(new PushHistoryActivityAdapter.OnItemClickSelectListener() {

            @Override
            public void onEditModeClickItemSelect(PushHistoryModel.PushHistoryVideoModel mHistoryData) {
                if (isInEditMode) {
                    // 进行选中状态反转操作,由hodler中的view自己控制
                    // 获取选中的数量并展示出来
                    updateEditModelSelectNumber();
                } else {
                    try {
                        if (mHistoryData != null) {
//                            UIHelper.startActivityByURL(PushHistoryActivity.this, video.router);
//                            MobclickAgent.onEvent(PushHistoryActivity.this, CLICK_push_push_history_ITEM);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        springView = (SpringView) findViewById(R.id.push_history_spring_view);
//        springView.setType(SpringView.Type.FOLLOW);
//        if (springView.getHeader() == null) {
//            springView.setHeader(new CustomHeader(this));
//        }
    }

    @Override
    public void onRightButtonClicked(View view) {
        super.onRightButtonClicked(view);
        if (pushHistoryModel == null ||
                ((pushHistoryModel.movies_within_serven_days == null || pushHistoryModel.movies_within_serven_days.isEmpty())
                && (pushHistoryModel.movies_over_serven_days == null || pushHistoryModel.movies_over_serven_days.isEmpty()))) {
            ToastUtils.getInstance().showGlobalShort("没有数据可编辑嘞");
            return;
        }

        isInEditMode = !isInEditMode;
        adapter.setMode(isInEditMode);
        // 进行数据的处理，编辑模式下则将所有数据标记为编辑模式
        if (isInEditMode) {
            tvSelectAll.setText("全选");
            tvSelectNumber.setText("删除");
            tvSelectNumber.setTextColor(getResources().getColor(R.color.colorText_9d9d9d));
            rlEditPanel.setVisibility(View.VISIBLE);
            setRightButton("取消");
        } else {
            // 非编辑模式下，则将所有数据编辑为非编辑模式并且选中状态为no
            rlEditPanel.setVisibility(View.GONE);
            setRightButton("编辑");
        }
    }

    private void deleteData() {
        // 不在编辑模式下
        if (!isInEditMode) {
            return;
        }
        // 返回的选中数量为0,给提示
        final List<String> selectedList = adapter.getSelectedVideoIdList();
        if (selectedList.size() == 0) {
            ToastUtils.getInstance().showGlobalShort(getString(R.string.collect_history_no_selected_data));
            return;
        }

        Repository.get(MovieRepository.class)
                .deletePushHistory(selectedList)
                .setCallback(new BaseRepositoryCallback<Void>(){
                    @Override
                    public void onSuccess(Void aVoid) {
                        super.onSuccess(aVoid);
                        if (PushHistoryActivity.this == null) {
                            return;
                        }

                        ToastUtils.getInstance().showGlobalShort("删除成功");
                        // 获取服务器剩余的数据
                        totalDataNumber -= selectedList.size();
                        Log.d(TAG, "totalDataNumber: " + totalDataNumber);
                        // 删除成功，通知adapter去删除数据
                        if (totalDataNumber <= 0) {
                            adapter.updateDataAfterDeleteSuccess(selectedList);
                            rlEditPanel.setVisibility(View.GONE);
                            setRightButton("编辑");
                            isInEditMode = false;

                            mLoadTipsView.setVisibility(View.VISIBLE);
                            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
                        } else {
                            adapter.updateDataAfterDeleteSuccess(selectedList);
                            mLoadTipsView.setVisibility(View.GONE);
                        }
                        tvSelectAll.setText("全选");
                        updateEditModelSelectNumber();

                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (PushHistoryActivity.this == null) {
                            return;
                        }
                        // 取消mLoadTipsView进度条
                        mLoadTipsView.setVisibility(View.GONE);
                        ToastUtils.getInstance().showGlobalShort(getString(R.string.collect_history_delete_data_fail));
                    }
                });
    }

    private void queryData() {
        Repository.get(MovieRepository.class)
                .getPushHistoryList()
                .setCallback(new BaseRepositoryCallback<PushHistoryModel>() {
                    @Override
                    public void onSuccess(PushHistoryModel pushHistoryModel) {
                        if (null == this) {
                            return;
                        }
                        PushHistoryActivity.this.pushHistoryModel = pushHistoryModel;
                        springView.onFinishFreshAndLoad();

                        if (pushHistoryModel != null &&
                                (pushHistoryModel.movies_over_serven_days != null && !pushHistoryModel.movies_over_serven_days.isEmpty()
                                || pushHistoryModel.movies_within_serven_days != null && !pushHistoryModel.movies_within_serven_days.isEmpty())){
                            mRightButton.setVisibility(View.VISIBLE);
                            updateViews();
                        } else {
                            mRightButton.setVisibility(View.GONE);
                            notHaveData();
                        }

                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        if (null == this) {
                            return;
                        }
                        springView.onFinishFreshAndLoad();
                        mLoadTipsView.setVisibility(View.VISIBLE);
                    }
                });

    }

    private void updateViews() {
        int count1 = 0, count2 = 0;
        if (pushHistoryModel.movies_within_serven_days != null) {
            adapter.addWithinServenData(pushHistoryModel.movies_within_serven_days);
            count1 = pushHistoryModel.movies_within_serven_days.size();
            if (isInEditMode) {
                for (PushHistoryModel.PushHistoryVideoModel temp : pushHistoryModel.movies_within_serven_days) {
                    temp.isInEditMode = true;
                    temp.isSelected = false;
                }
            }
        }
        if (pushHistoryModel.movies_over_serven_days != null) {
            adapter.addOverServenData(pushHistoryModel.movies_over_serven_days);
            count2 = pushHistoryModel.movies_over_serven_days.size();
            if (isInEditMode) {
                for (PushHistoryModel.PushHistoryVideoModel temp : pushHistoryModel.movies_over_serven_days) {
                    temp.isInEditMode = true;
                    temp.isSelected = false;
                }
            }
        }
        Log.d(TAG, "updateViews: count1 & count2: " + count1 + "," + count2);
        totalDataNumber = count1 + count2;
        // 下拉刷新，编辑模式下，选中状态和数量都恢复至默认
        if (isInEditMode) {
            updateEditModelSelectNumber();
            tvSelectAll.setText("全选");
        }
        mLoadTipsView.setVisibility(View.GONE);
    }

    /**
     * 显示暂无记录,或者是没有更多
     */
    private void notHaveData() {
        //第一次进来初始化数据
        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
    }

    /**
     * 加载数据失败
     */
    private void loadDataFail() {
        mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
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
}
