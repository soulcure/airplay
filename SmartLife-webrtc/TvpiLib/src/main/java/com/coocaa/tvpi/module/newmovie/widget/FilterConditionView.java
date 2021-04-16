package com.coocaa.tvpi.module.newmovie.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.CategoryFilterModel;
import com.coocaa.tvpi.module.newmovie.adapter.FilterConditionAdapter;
import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
import com.coocaa.tvpilib.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 影视筛选条件View
 * Created by songxing on 2020/7/15
 */
public class FilterConditionView extends RelativeLayout {
    private Context context;
    private LinearLayout filterConditionLayout;
    private TextView tvSelectedFilterCondition;
    private List<List<CategoryFilterModel>> filterConditionList = new ArrayList<>();
    private List<FilterConditionAdapter> adapterList = new ArrayList<>();
    private List<String> filterValues = new ArrayList<>(10);
    private List<String> sortValues = new ArrayList<>(10);
    private List<String> extraConditions = new ArrayList<>(10);
    private List<String> selectedFilterTypes = new ArrayList<>(10);

    private FilterConditionListener filterConditionListener;

    public FilterConditionView(Context context) {
        this(context, null, 0);
    }

    public FilterConditionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterConditionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_category_view, this, true);
        filterConditionLayout = findViewById(R.id.ll_filter_condition);
        tvSelectedFilterCondition = findViewById(R.id.tv_selected_filter_condition);
    }

    public void setFilterConditionList(List<List<CategoryFilterModel>> filterConditionList) {
        if (filterConditionList != null) {
            this.filterConditionList = filterConditionList;
            updateFilterConditionList();

            selectedFilterTypes.clear();
            sortValues.clear();
            filterValues.clear();
            extraConditions.clear();
            for (List<CategoryFilterModel> list : filterConditionList) {
                if (list.size() > 0) {
                    CategoryFilterModel defaultModel = list.get(0);
                    sortValues.add(defaultModel.sort_value);
                    filterValues.add(defaultModel.filter_value);
                    extraConditions.add(defaultModel.extra_condition);
                    selectedFilterTypes.add(defaultModel.title);
                    updateSelectedFilterConditionText();
                }
            }
        }
    }


    private void updateFilterConditionList() {
        adapterList.clear();
        filterConditionLayout.removeAllViews();

        for (int i = 0; i < filterConditionList.size(); i++) {
            final FilterConditionAdapter adapter = new FilterConditionAdapter();
            adapterList.add(adapter);
            final int currentIndex = i;
            adapter.setOnItemClickListener(new FilterConditionAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (adapter.getCurSelectedPosition() == position) {//防止重复点击进入
                        return;
                    }

                    adapter.setSelected(position);
                    CategoryFilterModel filterModel = adapter.getSelected();

                    // 选中筛选标签后，更新选中的筛选标签列表
                    try {
                        sortValues.set(currentIndex, filterModel.sort_value);
                        filterValues.set(currentIndex, filterModel.filter_value);
                        extraConditions.set(currentIndex, filterModel.extra_condition);
                        selectedFilterTypes.set(currentIndex, filterModel.title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updateSelectedFilterConditionText();
                    if (filterConditionListener != null) {
                        filterConditionListener.onFilerConditionChange(filterValues, sortValues, extraConditions);
                    }
                }
            });

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setHasFixedSize(true);
            CommonHorizontalItemDecoration decoration = new CommonHorizontalItemDecoration(
                    DimensUtils.dp2Px(context, 15f), DimensUtils.dp2Px(context, 0f));
            recyclerView.addItemDecoration(decoration);
            // 只显示一行
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = DimensUtils.dp2Px(context, 10);
            recyclerView.setLayoutParams(lp);   //设置按钮的布局属性
            filterConditionLayout.addView(recyclerView);
        }

        for (int i = 0; i < adapterList.size(); i++) {
            adapterList.get(i).addAll(filterConditionList.get(i));
            adapterList.get(i).setSelected(0);
        }
    }


    private void updateSelectedFilterConditionText() {
        // 先判断是不是从0到倒数第二个选中项都是全部（最近热播和最近更新不展示进来）
        int selectAllTagCount = 0;
        // 取出所有非"全部"选项
        List<String> notAllList = new ArrayList<>();

        for (int j = 0; j < selectedFilterTypes.size() - 1; j++) {
            String s = selectedFilterTypes.get(j);
            if (s.equals("全部")) {
                selectAllTagCount++;
            } else {
                notAllList.add(s);
            }
        }

        if (selectAllTagCount == selectedFilterTypes.size() - 1) {
            tvSelectedFilterCondition.setText("全部");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < notAllList.size(); i++) {
            String s = notAllList.get(i);
            if (!s.equals("全部")) {
                stringBuilder.append(s);
            }
            if (i != notAllList.size() - 1) {
                stringBuilder.append(" · ");
            }
        }
        tvSelectedFilterCondition.setText(stringBuilder.toString());
    }

    public void setFilterConditionListener(FilterConditionListener filterConditionListener) {
        this.filterConditionListener = filterConditionListener;
    }

    public interface FilterConditionListener {
        void onFilerConditionChange(List<String> filterValueList, List<String> sortValueList,
                                    List<String> extraConditionList);
    }
}
