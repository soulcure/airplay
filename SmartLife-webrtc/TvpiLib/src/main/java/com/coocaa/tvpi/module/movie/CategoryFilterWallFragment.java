package com.coocaa.tvpi.module.movie;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.data.BaseData;
import com.coocaa.publib.data.category.CategoryFilterTypeListResp;
import com.coocaa.publib.network.NetWorkManager;
import com.coocaa.publib.network.util.ParamsUtil;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.IRLog;
import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.smartscreen.data.movie.LongVideoListResp;
import com.coocaa.tvpi.module.movie.adapter.CategoryFilterTypeAdapter;
import com.coocaa.tvpi.module.movie.util.CategoryFilterDataHelper;
import com.coocaa.tvpi.view.CustomFooter;
import com.coocaa.tvpi.view.CustomHeader;
import com.coocaa.tvpi.view.CustomViewPager;
import com.coocaa.tvpi.view.LoadTipsView;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.widget.SpringView;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import me.drakeet.multitype.MultiTypeAdapter;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import static com.coocaa.tvpi.common.UMEventId.CLICK_CATEGORY_FILTER_CONDITION;

/**
 * Created by IceStorm on 2017/12/19.
 */

public class CategoryFilterWallFragment extends Fragment {

    private static final String TAG = CategoryFilterWallFragment.class.getSimpleName();

    private View mLayout;

    private RelativeLayout rlFiltersBackground;
    private LinearLayout llFilters;
    private RelativeLayout rlSelectedFilters;

    private ImageView imgFilterArrow;
    private TextView tvFilterTypes;

    private SpringView mSpringView;
    private LoadTipsView mLoadTipsView;

    private RecyclerView mRecyclerView;
    private MultiTypeAdapter adapter;
    private CategoryFilterDataHelper helper;
    List<Object> items = new ArrayList<>();

    private int pageSize = 12;
    private int pageIndex = 0;

    private String classifyId;
    private String currentSortValue;
    private String currentFilterValue;
    private String currentExtraCondition;

    private boolean isRefresh = false;
    private boolean isAddMore = false;
    private boolean isHasMore = false;

    private boolean isFilterFold = true;

    private LongVideoListResp videoListResp;
    private CategoryFilterTypeListResp categoryFilterTypeListResp;

    private List<List<CategoryFilterModel>> filterTypesListContainer = new ArrayList<List<CategoryFilterModel>>();

    private List<String> filter_values = new ArrayList<>();
    private List<String> sort_values = new ArrayList<>();
    private List<String> extra_conditions = new ArrayList<>();
    private List<String> selectedFilterTypes = new ArrayList<>();

    private List<CategoryFilterTypeAdapter> adapterList = new ArrayList<CategoryFilterTypeAdapter>();

    public WeakReference<CustomViewPager> mCustomViewPager;

    // ??????????????????????????????key???
    private List<String> keyList = new ArrayList<>();
    private String parentClassifyTitle; // ??????????????????  ???"??????"
    private String selfClassifyTitle; // ?????????????????? ???"??????"??????"????????????"

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        mLayout = inflater.inflate(R.layout.fragment_category_filter_wall, container, false);

//        EventBus.getDefault().register(this);//??????????????????FragmentPagerAdapter??????????????????onDestroy ??????onDestroyView?????????
        initViews();

        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated: ");
        super.onActivityCreated(savedInstanceState);

        if (null != savedInstanceState) {
            classifyId = savedInstanceState.getString("classifyId");
        }

        // viewpager?????????fragment???onActivityCreated????????????????????????????????????????????????????????????
        if(categoryFilterTypeListResp == null) {
            mLoadTipsView.setVisibility(View.VISIBLE);
            mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

            queryFilterListData(classifyId);
        } else {
            if(items.size() == 0) {
                // items??????????????????????????????????????????
                Log.d(TAG, "onActivityCreated: " + "  classifyId:" + classifyId + " pageIndex:" + pageIndex);

                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);
                pageIndex = 0;
                queryFilterListData(classifyId);
                queryData(classifyId, pageIndex, pageSize);
            } else {
                // items?????????????????????????????????
                mLoadTipsView.setVisibility(View.GONE);
                helper.addAll(items);
                updateFilterViews();
                updateSelectedFilter();
                Log.d(TAG, "onActivityCreated: items size:" + items.size() + "  classifyId:" + classifyId );
            }
        }
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

        // ?????????????????????????????????????????????????????????
        if(!isFilterFold) {
            isFilterFold = true;
            setFilterUI(isFilterFold);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("classifyId", classifyId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setClassifyInfos(String id, String sortValue, String filterValue, String extraCondition, String parentTitle, String selfTitle) {
        classifyId = id;
        currentSortValue = sortValue;
        currentFilterValue = filterValue;
        currentExtraCondition = extraCondition;

        this.parentClassifyTitle = parentTitle;
        this.selfClassifyTitle = selfTitle;
    }

    private void initViews(){
        imgFilterArrow = mLayout.findViewById(R.id.fragment_category_filter_wall_img_arrow);

        // ??????????????????
        rlFiltersBackground = mLayout.findViewById(R.id.fragment_category_filter_wall_background);
        rlFiltersBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ????????????????????????????????????????????????
                if(!isFilterFold) {
                    isFilterFold = true;

                    setFilterUI(isFilterFold);
                }
            }
        });

        // ???????????????
        rlSelectedFilters = mLayout.findViewById(R.id.fragment_category_filter_wall_rl_types);
        rlSelectedFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ????????????
                isFilterFold = !isFilterFold;
                setFilterUI(isFilterFold);
            }
        });

        tvFilterTypes = mLayout.findViewById(R.id.fragment_category_filter_wall_tv_types);

        mLoadTipsView = (LoadTipsView) mLayout.findViewById(R.id.fragment_category_filter_wall_loadtipsview);
        mLoadTipsView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ????????????????????????????????????????????????????????????????????????????????????
                mLoadTipsView.setVisibility(View.VISIBLE);
                mLoadTipsView.setLoadTipsIV(LoadTipsView.TYPE_LOADING);

                if(categoryFilterTypeListResp == null) {
                    queryFilterListData(classifyId);
                } else {
                    pageIndex = 0;
                    queryData(classifyId, pageIndex, pageSize);
                }
            }
        });

        mRecyclerView = (RecyclerView) mLayout.findViewById(R.id.fragment_category_filter_wall_recyclerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new MultiTypeAdapter();
        helper = new CategoryFilterDataHelper(adapter, "??????_" + classifyId);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //??????RecyclerView??????????????????????????????item???position
                int lastPosition = -1;

                //?????????????????????????????????SCROLL_STATE_IDLE???
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    if (layoutManager instanceof GridLayoutManager) {
                        //??????LayoutManager??????????????????????????????item???position
                        lastPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }
                    //??????????????????????????????item???position????????????itemCount??????-1?????????????????????item???position
                    //?????????????????????????????????????????????
                    if (lastPosition == recyclerView.getLayoutManager().getItemCount() - 1) {
//                        Toast.makeText(getActivity(), "???????????????", Toast.LENGTH_SHORT).show();
                        if (null != adapter && !isAddMore && isHasMore) {
                            isAddMore = true;
                            queryData(classifyId, pageIndex+1, pageSize);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                Log.d(TAG, "onScrolled: " + dy);
            }
        });

        mSpringView = (SpringView) mLayout.findViewById(R.id.fragment_category_filter_wall_springview);
        mSpringView.setType(SpringView.Type.FOLLOW);
        if(mSpringView.getHeader() == null) {
            mSpringView.setHeader(new CustomHeader(getActivity()));
        }
//        mSpringView.setFooter(new DefaultFooter(getActivity()));
        if(mSpringView.getFooter() == null) {
            mSpringView.setFooter(new CustomFooter(getActivity()));
        }

        //????????????/??????????????????
        mSpringView.setListener(new SpringView.OnFreshListener() {
            @Override
            public void onRefresh() {
                isRefresh = true;
                pageIndex = 0;
                queryData(classifyId, pageIndex, pageSize);
            }

            @Override
            public void onLoadmore() {
                if (isHasMore) {
                    isAddMore = true;
                    queryData(classifyId, pageIndex+1, pageSize);
                }else {
                    mSpringView.onFinishFreshAndLoad();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        llFilters = mLayout.findViewById(R.id.fragment_category_filter_wall_ll);
    }

    private void setFilterUI(boolean isFold) {
        if(isFold) {
            llFilters.setVisibility(View.GONE);
            imgFilterArrow.setImageResource(R.drawable.icon_filter_arrow_down);

            // ?????????????????????wrap content
            rlFiltersBackground.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            llFilters.setVisibility(View.VISIBLE);
            imgFilterArrow.setImageResource(R.drawable.icon_filter_arrow_up);

            // ?????????????????????match parent
            rlFiltersBackground.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void queryFilterListData(final String classifyId) {
        HashMap<String,Object> queryParams = new HashMap<>();
        queryParams.put("classify_id", classifyId);
        NetWorkManager.getInstance()
                .getApiService()
                .getFilterList(ParamsUtil.getQueryMap(queryParams))
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
                        IRLog.d(TAG, "onSuccess. response = " + response);

                        if (CategoryFilterWallFragment.this == null || null == getActivity()) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (!TextUtils.isEmpty(response)) {
                            categoryFilterTypeListResp = BaseData.load(response, CategoryFilterTypeListResp.class);
                            if (categoryFilterTypeListResp != null
                                    && categoryFilterTypeListResp.data != null
                                    && categoryFilterTypeListResp.data.tags != null
                                    && categoryFilterTypeListResp.data.tags.size() > 0) {
                                // ????????????????????????
                                Iterator kv = categoryFilterTypeListResp.data.tags.entrySet().iterator();
                                Iterator k = categoryFilterTypeListResp.data.tags.keySet().iterator();
                                Iterator v = categoryFilterTypeListResp.data.tags.values().iterator();

                                int mapsize = categoryFilterTypeListResp.data.tags.size();

                                // ??????filterTypesListContainer sort_values filter_values extra_conditions????????????????????????
                                if(filterTypesListContainer.size() > 0)
                                    filterTypesListContainer.clear();

                                if(selectedFilterTypes.size() > 0)
                                    selectedFilterTypes.clear();

                                if(sort_values.size() > 0)
                                    sort_values.clear();
                                if(filter_values.size() > 0)
                                    filter_values.clear();
                                if(extra_conditions.size() > 0)
                                    extra_conditions.clear();

                                // ????????????????????????
                                sort_values.add(currentSortValue);
                                filter_values.add(currentFilterValue);
                                extra_conditions.add(currentExtraCondition);

                                for(int i = 0;i<mapsize; i++)
                                {
                                    Map.Entry entry = (Map.Entry)kv.next();
                                    String key = (String) entry.getKey();
                                    Object value = entry.getValue();

                                    List<CategoryFilterModel> list = (List<CategoryFilterModel>)value;

                                    // ??????key???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                                    if(!key.equals("??????")) {
                                        filterTypesListContainer.add(list);

                                        // ???????????????key???????????????????????????
                                        keyList.add(key);

                                        if (list instanceof List && list.size() > 0) {
                                            CategoryFilterModel defaultModel = list.get(0);
                                            sort_values.add(defaultModel.sort_value);
                                            filter_values.add(defaultModel.filter_value);
                                            extra_conditions.add(defaultModel.extra_condition);

                                            selectedFilterTypes.add(defaultModel.title);
                                            updateSelectedFilter();
                                        }
                                    }
                                }
                                // ???"??????"??????????????????????????????
                                List<CategoryFilterModel> specialList = categoryFilterTypeListResp.data.tags.get("??????");
                                filterTypesListContainer.add(specialList);
                                if (specialList instanceof List
                                        && specialList.size() > 0) {
                                    CategoryFilterModel defaultModel = specialList.get(0);
                                    sort_values.add(defaultModel.sort_value);
                                    filter_values.add(defaultModel.filter_value);
                                    extra_conditions.add(defaultModel.extra_condition);

                                    selectedFilterTypes.add(defaultModel.title);
                                    updateSelectedFilter();

                                    // ???????????????key???????????????????????????
                                    keyList.add("??????");
                                }

                                updateFilterViews();
                                mLoadTipsView.setVisibility(View.GONE);

                                pageIndex = 0;
                                queryData(classifyId, pageIndex, pageSize);
                            } else {
                                notHaveFilterTypesData();
                            }
                        } else {
                            loadFilterTypesDataFail();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (null != e)
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());

                        if (CategoryFilterWallFragment.this == null || null == getActivity()) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        mLoadTipsView.setVisibility(View.VISIBLE);
//                mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_net_error), LoadTipsView.TYPE_FAILED);
                        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void reportUMFilters() {

        // ?????????????????????????????????????????????????????????????????????????????????"??????"??????"????????????"???????????????
        Map<String, String> map = new HashMap<>();
        if(!TextUtils.isEmpty(parentClassifyTitle)) {
            map.put("????????????", parentClassifyTitle);
        }
        if(!TextUtils.isEmpty(selfClassifyTitle)) {
            map.put("????????????", selfClassifyTitle);
        }

        if(adapterList.size() > 0) {
            for(int i=0; i<keyList.size(); i++) {
                String tempKey = keyList.get(i);
                if(i < adapterList.size()) {
                    CategoryFilterTypeAdapter tempAdapter = adapterList.get(i);
                    CategoryFilterModel selectedFilter = tempAdapter.getSelected();

                    if (null != selectedFilter)
                        map.put(tempKey, selectedFilter.title);
                }
            }
        }

        Log.d(TAG, "reportUMFilters: map:" + map);
        MobclickAgent.onEvent(getContext(), CLICK_CATEGORY_FILTER_CONDITION, map);
    }

    private void updateFilterViews() {

//        final List<CategoryFilterTypeAdapter> adapterList = new ArrayList<CategoryFilterTypeAdapter>();
        if(adapterList.size() > 0) {
            adapterList.clear();
        }
        if(llFilters.getChildCount() > 0) {
            llFilters.removeAllViews();
        }

        for(int i=0; i<filterTypesListContainer.size(); i++) {
            final CategoryFilterTypeAdapter adapter = new CategoryFilterTypeAdapter(20);
            adapterList.add(adapter);
            final int currentIndex = i;
            adapter.setOnItemClickListener(new CategoryFilterTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (adapter.getCurSelectedPosition() == position) {//????????????????????????
                        return;
                    }

                    adapter.setSelected(position);
                    CategoryFilterModel filterModel = adapter.getSelected();

                    // ?????????????????????????????????????????????????????????
                    try {
                        sort_values.set(currentIndex, filterModel.sort_value);
                        filter_values.set(currentIndex, filterModel.filter_value);
                        extra_conditions.set(currentIndex, filterModel.extra_condition);

                        selectedFilterTypes.set(currentIndex, filterModel.title);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    updateSelectedFilter();

                    // ????????????????????????
                    pageIndex = 0;
                    queryData(classifyId, pageIndex, pageSize);

                    reportUMFilters();
                }
            });

            RecyclerView recyclerView = new RecyclerView(getActivity());
            recyclerView.setHasFixedSize(true);
            CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(DimensUtils.dp2Px(getActivity(),20f), DimensUtils.dp2Px(getActivity(),15f));
            recyclerView.addItemDecoration(decoration);
            // ???????????????
            int spanCount = 1;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);

            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            recyclerView.setLayoutParams(lp);   //???????????????????????????

            llFilters.addView(recyclerView);

            /*recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "onTouch: " + event.getAction());
                    *//*
             * down 0
             * up 1
             * move 2
             * cancel 3
             * outside 4
             * ACTION_POINTER_DOWN 5
             * ACTION_POINTER_UP 6
             * ACTION_HOVER_MOVE  7
             * ACTION_SCROLL  8
             * ACTION_HOVER_ENTER 9
             * ACTION_HOVER_EXIT 10
             * ACTION_BUTTON_PRESS 11
             * ACTION_BUTTON_RELEASE 12
             * ACTION_POINTER_INDEX_MASK 0xff00
             * ACTION_POINTER_INDEX_SHIFT 8
             * *//*

                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(mCustomViewPager.get() != null) {
                            mCustomViewPager.get().setScroll(true);
                        }
                    } else {
//                        Log.d(TAG, "onTouch: ACTION_UP");
                        if(mCustomViewPager.get() != null) {
                            mCustomViewPager.get().setScroll(false);
                        }
                    }

                    return false;
                }
            });*/
        }

        // ?????????????????????
        if(isFilterFold) {

            llFilters.setVisibility(View.GONE);
            imgFilterArrow.setImageResource(R.drawable.icon_filter_arrow_down);
        } else {
            llFilters.setVisibility(View.VISIBLE);
            imgFilterArrow.setImageResource(R.drawable.icon_filter_arrow_up);
        }

        for(int i=0; i<adapterList.size(); i++) {
            adapterList.get(i).addAll(filterTypesListContainer.get(i));
            adapterList.get(i).setSelected(0);
        }
    }

    private void updateSelectedFilter() {
        // ?????????????????????0???????????????????????????????????????????????????????????????????????????????????????
        int selectAllTagCount = 0;
        // ???????????????"??????"??????
        List<String> notAllList = new ArrayList<>();

        for(int j=0; j<selectedFilterTypes.size()-1; j++) {
            String s = selectedFilterTypes.get(j);
            if(s.equals("??????")) {
                selectAllTagCount ++;
            } else {
                notAllList.add(s);
            }
        }

        if(selectAllTagCount == selectedFilterTypes.size()-1) {
            tvFilterTypes.setText("??????");
            return;
        }
//package com.coocaa.tvpi.module.movie.filter;

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<notAllList.size(); i++) {
            String s = notAllList.get(i);

            if(!s.equals("??????")) {
                stringBuilder.append(s);
            }

            if(i != notAllList.size() - 1) {
                stringBuilder.append(" ?? ");
            }
        }

        tvFilterTypes.setText(stringBuilder.toString());
    }

    public void queryData(String classifyId,int page,int pageSize){
        HashMap<String,Object> queryParams = new HashMap<>();
        queryParams.put("classify_id", classifyId);
        queryParams.put("page_index", page);
        queryParams.put("page_size", pageSize);

        Map<String, Object> fieldMap = new HashMap<>();
        if(filter_values.size() > 0)
            fieldMap.put("filter_values", ParamsUtil.getStringJsonArray(filter_values));
        if(sort_values.size() > 0)
            fieldMap.put("sort_values", ParamsUtil.getStringJsonArray(sort_values));
        if(extra_conditions.size() > 0)
            fieldMap.put("extra_conditions", ParamsUtil.getStringJsonArray(extra_conditions));

        Log.d(TAG, "queryData: :page" + page + " classifyId:" + classifyId + "\n" + " extra_conditions:" + extra_conditions.toString());
        String json = ParamsUtil.getJsonStringParams(fieldMap);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json;charset=UTF-8"),json);
        NetWorkManager.getInstance()
                .getApiService()
                .getVideoList(ParamsUtil.getQueryMap(queryParams),body)
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
                        IRLog.d(TAG, "onSuccess. response = " + response);

                        if (CategoryFilterWallFragment.this == null || null == getActivity()) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (!TextUtils.isEmpty(response)) {
                            videoListResp = BaseData.load(response, LongVideoListResp.class);
                            if (videoListResp != null
                                    && videoListResp.data != null
                                    && videoListResp.data.size() > 0) {
                                for(LongVideoListModel temp : videoListResp.data) {
//                                    temp.container_type = MultiTypeEnum.COLUMS_3;
                                    temp.container_name = "";
                                }

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
                        if (null != e)
                            IRLog.d(TAG, "onFailure,statusCode:" + e.toString());

                        if (CategoryFilterWallFragment.this == null || null == getActivity()) {
                            IRLog.e(TAG, "fragment or activity was destroyed");
                            return;
                        }

                        if (isRefresh || isAddMore) {
                            isRefresh = false;
                            isAddMore = false;
                            mSpringView.onFinishFreshAndLoad();
//                    ToastUtils.showShort(getActivity(),getString(R.string.title_loadtips_net_error), true);
                        }
//                else {
                        mLoadTipsView.setVisibility(View.VISIBLE);
//                    mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_net_error), LoadTipsView.TYPE_FAILED);
                        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_FAILED);
//                }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void updateViews(){

        if (isAddMore) {
            isAddMore = false;
            pageIndex++;
            items.clear();
            items.addAll(videoListResp.data);
            helper.addMore(items);
        } else {
            pageIndex = 0;
            isRefresh = false;
            items.clear();
            items.addAll(videoListResp.data);
            helper.addAll(items);
        }

        isHasMore = (videoListResp.has_more == 1);
        mSpringView.onFinishFreshAndLoad();
        mLoadTipsView.setVisibility(View.GONE);
    }

    private void notHaveFilterTypesData() {
//        mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
    }

    private void loadFilterTypesDataFail() {
//        mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
        mLoadTipsView.setVisibility(View.VISIBLE);
    }

    /**
     * ??????????????????,?????????????????????
     */
    private void notHaveData() {

        if (!isRefresh && !isAddMore) {
            //??????????????????????????????
//            String tip = getString(R.string.title_loadtips_no_data);
//            mLoadTipsView.setLoadTips(tip, LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);

            items.clear();
            helper.addAll(items);
            return;
        }

        if(isAddMore){
            isAddMore = false;
            ToastUtils.getInstance().showGlobalShort(getString(R.string.loading_tip_no_more_data));

            items.clear();
            helper.addMore(items);
        }

        isHasMore = (videoListResp.has_more == 1);
        mSpringView.onFinishFreshAndLoad();
    }

    /**
     * ??????????????????
     */
    private void loadDataFail() {
        if (isRefresh || isAddMore) {
            isRefresh = false;
            isAddMore=false;
            mSpringView.onFinishFreshAndLoad();
            ToastUtils.getInstance().showGlobalShort(getString(R.string.loading_tip_server_busy));
        }else {
//            mLoadTipsView.setLoadTips(getString(R.string.title_loadtips_no_data), LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setLoadTips("", LoadTipsView.TYPE_NODATA);
            mLoadTipsView.setVisibility(View.VISIBLE);
        }
    }
}
