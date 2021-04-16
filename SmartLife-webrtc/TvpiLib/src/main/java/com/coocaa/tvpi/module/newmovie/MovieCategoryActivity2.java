package com.coocaa.tvpi.module.newmovie;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.data.movie.LongVideoListModel;
import com.coocaa.tvpi.base.mvvm.BaseViewModelAppletActivity;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.newmovie.adapter.MovieListAdapter;
import com.coocaa.tvpi.module.newmovie.viewmodel.MovieCategoryViewModel;
import com.coocaa.tvpi.util.IntentUtils;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.SmartRefreshFooter;
import com.coocaa.tvpi.view.SmartRefreshHeader;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

import swaiotos.runtime.base.FloatAppletLayoutBuilder;

/**
 * 影视具体分类类型界面
 * Created by songxing on 2020/10/23
 */
public class MovieCategoryActivity2 extends BaseViewModelAppletActivity<MovieCategoryViewModel> {
    private static final String TAG = MovieCategoryActivity2.class.getSimpleName();
    public static final String COMIC_CLASSIFY_ID = "_oqy_4";
    public static final String SPORT_CLASSIFY_ID = "_ct_3987";

    private CommonTitleBar titleBar;
    private SmartRefreshLayout smartRefreshLayout;
    private RecyclerView recyclerView;
    private NestedScrollView nestedScrollView;
    private DefaultLoadStateView loadStateView;
    private MovieListAdapter movieListAdapter;
    private ImageView imgMovieCategoryBackground;

    private String source;
    private String classifyId;
    //分类Id

    private int pageIndex = 0;
    private int pageSize = 18;
    private boolean isHasMoreData;
    private int scrollY;

    public static void start(Context context,String source,String classifyId) {
        Intent starter = new Intent(context, MovieCategoryActivity2.class);
        starter.putExtra("source",source);
        starter.putExtra("classify_id",classifyId);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_category2);
        parseIntent();
        initView();
        getMovieList(true);
    }

    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    private void parseIntent() {
        source = IntentUtils.INSTANCE.getStringExtra(getIntent(), "source");
        classifyId = IntentUtils.INSTANCE.getStringExtra(getIntent(), "classify_id");
    }


    private void initView() {
        setTitleAlpha(0);
        setBackgroundColor(Utils.changeAlpha(getResources().getColor(R.color.c_6), 0));
        titleBar = findViewById(R.id.titleBar);
        smartRefreshLayout = findViewById(R.id.smartRefreshLayout);
        loadStateView = findViewById(R.id.loadStateView);
        recyclerView = findViewById(R.id.recyclerview);
        nestedScrollView = findViewById(R.id.nsv_movie_category);
        imgMovieCategoryBackground = findViewById(R.id.movie_back_img);
        smartRefreshLayout.setRefreshHeader(new SmartRefreshHeader(this,false));
        smartRefreshLayout.setRefreshFooter(new SmartRefreshFooter(this));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setNestedScrollingEnabled(false);
        movieListAdapter = new MovieListAdapter();
        PictureItemDecoration decoration = new PictureItemDecoration(3,
                DimensUtils.dp2Px(this, 10),
                DimensUtils.dp2Px(this, 15));
        recyclerView.setAdapter(movieListAdapter);
        recyclerView.addItemDecoration(decoration);
        Log.d(TAG, "initView: "+classifyId);
        if(COMIC_CLASSIFY_ID.equals(classifyId)) {
            titleBar.setText(CommonTitleBar.TextPosition.TITLE, "动漫");
//            GlideApp.with(this).load(R.drawable.bg_cartoon).into(imgMovieCategoryBackground);
        }else if(SPORT_CLASSIFY_ID.equals(classifyId)){
            titleBar.setText(CommonTitleBar.TextPosition.TITLE, "体育");
//            GlideApp.with(this).load(R.drawable.bg_sport).into(imgMovieCategoryBackground);
        }
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }
            }
        });
        if(mNPAppletInfo != null) {
            Log.d(TAG, "initView: title Gone");
            titleBar.setVisibility(View.GONE);
        }

        smartRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                pageIndex = 0;
                getMovieList(false);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (isHasMoreData) {
                    pageIndex++;
                    getMovieList(false);
                } else {
                    smartRefreshLayout.finishLoadMore();
                    ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.pull_no_more_msg));
                }
            }
        });

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                float fraction = scrollY >= DimensUtils.dp2Px(MovieCategoryActivity2.this, 120) ? 1f
                        : scrollY / (float) DimensUtils.dp2Px(MovieCategoryActivity2.this, 120);
//                if(1 - fraction <= 0.01){
//                    setTitle();
//                }else {
//                    setTitle("");
//                }
                setBackgroundColor(Utils.changeAlpha(getResources().getColor(R.color.c_6), fraction));
                imgMovieCategoryBackground.setAlpha(1f-fraction);
                setTitleAlpha(fraction);
            }
        });

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMovieList(true);
            }
        });
    }



  /*  private void getCategoryList(){
        viewModel.getMainCategoryList(source).observe(this, new Observer<List<CategoryMainModel>>() {
            @Override
            public void onChanged(List<CategoryMainModel> categoryMainModels) {
                for (CategoryMainModel categoryMainModel : categoryMainModels) {
                    if(classifyName.equals(categoryMainModel.classify_name)){
                        classifyId = categoryMainModel.classify_id;
                        getMovieList(true);
                    }
                }
            }
        });
    }*/

    private void getMovieList(boolean showLoading){
        if(!TextUtils.isEmpty(classifyId)){
            viewModel.getMovieList(showLoading,source, classifyId, pageIndex, pageSize)
                    .observe(this, movieListObserver);
        }
    }

    private Observer<List<LongVideoListModel>> movieListObserver = new Observer<List<LongVideoListModel>>() {
        @Override
        public void onChanged(List<LongVideoListModel> longVideoListModels) {
            if (pageIndex == 0) {     //下拉刷新
                movieListAdapter.setList(longVideoListModels);
                smartRefreshLayout.finishRefresh();
                Log.d(TAG, "onChanged: cc");
            } else {
                movieListAdapter.addData(longVideoListModels);
            }
            smartRefreshLayout.finishLoadMore();
            isHasMoreData = longVideoListModels.size() == pageSize;
        }
    };

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        if(isFloatHeader()) {
            return new FloatAppletLayoutBuilder(this);
        } else {
            return super.createLayoutBuilder();
        }
    }

    @Override
    protected boolean isFloatHeader() {
        return true;
    }

    public void setBackgroundColor(int color){
        if (mHeaderHandler != null) {
            mHeaderHandler.setBackgroundColor(color);
        }
    }

    public void setTitleAlpha(float alpha){
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitleAlpha(alpha);
        }
    }

    private void setTitle() {
        if(COMIC_CLASSIFY_ID.equals(classifyId)) {
            setTitle("看动画");
        }else if(SPORT_CLASSIFY_ID.equals(classifyId)){
            setTitle("看球赛");
        }
    }
}
