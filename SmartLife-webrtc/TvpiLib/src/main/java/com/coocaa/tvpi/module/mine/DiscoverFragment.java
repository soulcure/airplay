package com.coocaa.tvpi.module.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.connect.callback.ConnectCallbackImpl;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.tvpi.base.mvvm.BaseViewModelFragment;
import com.coocaa.tvpi.base.mvvm.view.DefaultLoadStateView;
import com.coocaa.tvpi.base.mvvm.view.LoadStateViewProvide;
import com.coocaa.tvpi.module.feedback.FeedbackActivity;
import com.coocaa.tvpi.module.homepager.adapter.bean.PlayMethodBean;
import com.coocaa.tvpi.module.homepager.main.UIConnectCallbackAdapter;
import com.coocaa.tvpi.module.login.LoginActivity;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.mine.adapter.DiscoverBannerAdapter;
import com.coocaa.tvpi.module.mine.adapter.PlayMethodAdapter;
import com.coocaa.tvpi.module.mine.lab.SmartLabActivity2;
import com.coocaa.tvpi.module.mine.userinfo.UserInfoActivity;
import com.coocaa.tvpi.module.mine.viewmodel.MineFragmentViewModel;
import com.coocaa.tvpi.module.onlineservice.OnlineServiceHelp;
import com.coocaa.tvpi.util.OnDebouncedClick;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.decoration.CommonVerticalItemDecoration;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;
import com.youth.banner.Banner;
import com.youth.banner.indicator.CircleIndicator;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DiscoverFragment extends BaseViewModelFragment<MineFragmentViewModel> {
    private static final String TAG = DiscoverFragment.class.getSimpleName();
    private DefaultLoadStateView loadStateView;
    private RelativeLayout titleLayout;
    private Button btSetting;
    private RecyclerView playRecyclerView;
    private PlayMethodAdapter playMethodAdapter;

    private ImageView ivCover;
    private RelativeLayout loginLayout;
    private RelativeLayout unLoginLayout;
    private TextView tvPhoneNum;
    private View feedbackLayout;
    private View labLayout;
    private View bannerLayout;
    private ImageView imgFeedBack;
    private ImageView imgCallUs;
    private ImageView imgNewStudy;

    private int scrollY;

    private volatile String curDeviceType = null;
    private volatile String lastDeviceType = null;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SSConnectManager.getInstance().addConnectCallback(connectCallback);
        initView();
        getPlayMethodList(true);
    }


    @Override
    protected LoadStateViewProvide createLoadStateViewProvide() {
        return loadStateView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLoginView();
        StatusBarHelper.setStatusBarLightMode(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SSConnectManager.getInstance().removeConnectCallback(connectCallback);
    }

    private final ConnectCallbackImpl connectCallback = new UIConnectCallbackAdapter(new UIConnectCallbackAdapter.SimpleUIConnectCallback() {
        @Override
        public void onDeviceTypeChange(@org.jetbrains.annotations.Nullable String deviceType) {
            Log.d(TAG, "onDeviceTypeChange: deviceType" + deviceType);

            if (getActivity() == null) {
                Log.d(TAG, "onDeviceTypeChange: activity is null");
                return;
            }

            curDeviceType = deviceType;
            getPlayMethodList(false);
        }
    });


    private void initView() {
        if (getView() == null) return;
        loadStateView = getView().findViewById(R.id.load_state_view);
        titleLayout = getView().findViewById(R.id.titleLayout);
        btSetting = getView().findViewById(R.id.btSetting);
        playRecyclerView = getView().findViewById(R.id.recyclerview);
        playMethodAdapter = new PlayMethodAdapter();
        playRecyclerView.setAdapter(playMethodAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        playRecyclerView.setLayoutManager(layoutManager);
        CommonVerticalItemDecoration itemDecoration = new CommonVerticalItemDecoration(0,
                DimensUtils.dp2Px(getContext(), 20), DimensUtils.dp2Px(getContext(), 60));
        playRecyclerView.addItemDecoration(itemDecoration);
        View headerView = getLayoutInflater().inflate(R.layout.header_discovery, playRecyclerView, false);
        playMethodAdapter.addHeaderView(headerView);
        ivCover = headerView.findViewById(R.id.ivCover);
        loginLayout = headerView.findViewById(R.id.loginLayout);
        tvPhoneNum = headerView.findViewById(R.id.tvPhoneNum);
        unLoginLayout = headerView.findViewById(R.id.unLoginLayout);
        feedbackLayout = headerView.findViewById(R.id.feedbackLayout);
        labLayout = headerView.findViewById(R.id.labLayout);
        bannerLayout = headerView.findViewById(R.id.banner_layout);

        imgCallUs = headerView.findViewById(R.id.call_us_img);
        imgFeedBack = headerView.findViewById(R.id.feed_back_img);
        imgNewStudy = headerView.findViewById(R.id.new_study_img);

        loadStateView.setLoadTipsOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.getPlayList(true,curDeviceType)
                        .observe(getViewLifecycleOwner(), playMethodListObserver);
                viewModel.getDiscoverBanner()
                        .observe(getViewLifecycleOwner(),bannerListObserver);
            }
        });

        playRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                scrollY += dy;
                float maxHeight = DimensUtils.dp2Px(getContext(), 50);
                float fraction = scrollY < maxHeight ? scrollY / maxHeight : 1f;
                titleLayout.setAlpha(fraction);
            }
        });

        btSetting.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        }));


        ivCover.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                    startActivity(intent);
                }
            }
        }));

        loginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                startActivity(intent);
            }
        });

        unLoginLayout.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserInfoCenter.getInstance().isLogin()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        }));

        feedbackLayout.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfoCenter.getInstance().isLogin()) {
                    Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        }));

        labLayout.setOnClickListener(new OnDebouncedClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.setClass(Objects.requireNonNull(getContext()), SmartLabActivity2.class);
                startActivity(intent);
            }
        }));

        imgFeedBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfoCenter.getInstance().isLogin()) {
                    Intent intent = new Intent(getActivity(), FeedbackActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        imgCallUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnlineServiceHelp.getInstance().openOnlinServieActivity();
            }
        });


        imgNewStudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserInfoCenter.getInstance().isLogin()) {
                    SimpleWebViewActivity.startAsApplet(getContext(), "https://webapp.skysrt.com/swaiot/novice-guide/index.html");
                } else {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void getPlayMethodList(boolean showLoading) {
        Log.d(TAG, "getPlayMethodList, curDeviceType=" + curDeviceType + ", lastDeviceType=" + lastDeviceType);
        if (TextUtils.isEmpty(curDeviceType)) {
            curDeviceType = "default";//默认未连接设备
        }
        if (!TextUtils.equals(lastDeviceType, curDeviceType)) {
            //考虑改成类型变化了，且没获取到数据才需要刷新数据
            viewModel.getPlayList(showLoading,curDeviceType)
                    .observe(getViewLifecycleOwner(), playMethodListObserver);

            viewModel.getDiscoverBanner()
                    .observe(getViewLifecycleOwner(), bannerListObserver);
        }
    }

    private final Observer<List<FunctionBean>> bannerListObserver = new Observer<List<FunctionBean>>() {
        @Override
        public void onChanged(List<FunctionBean> functionBeans) {
            if (functionBeans.isEmpty()) {
                bannerLayout.setVisibility(View.GONE);
            } else {
                bannerLayout.setVisibility(View.VISIBLE);
                DiscoverBannerAdapter discoverBannerAdapter = new DiscoverBannerAdapter(getContext(),functionBeans);
                Banner<FunctionBean, DiscoverBannerAdapter> banner = bannerLayout.findViewById(R.id.banner);
                if(banner == null){
                    return;
                }
                banner.setAdapter(discoverBannerAdapter);
                if (functionBeans != null && functionBeans.size() > 1) {
                    CircleIndicator circleIndicator = new CircleIndicator(getContext());
                    banner.setIndicator(circleIndicator);
                    int dp4 = DimensUtils.dp2Px(getContext(), 4);
                    banner.setIndicatorNormalWidth(dp4);
                    banner.setIndicatorSelectedWidth(dp4);
                    banner.setIndicatorSelectedColorRes(R.color.white);
                    banner.setIndicatorNormalColorRes(R.color.color_white_60);
                } else {
                    banner.removeIndicator();
                }
            }

        }
    };

    private final Observer<List<PlayMethodBean>> playMethodListObserver = new Observer<List<PlayMethodBean>>() {
        @Override
        public void onChanged(List<PlayMethodBean> playMethodBeans) {
            lastDeviceType = curDeviceType;
            playMethodAdapter.setList(playMethodBeans);
        }
    };

    private void updateLoginView() {
        boolean login = UserInfoCenter.getInstance().isLogin();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        if (login && coocaaUserInfo != null) {
            loginLayout.setVisibility(View.VISIBLE);
            unLoginLayout.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(coocaaUserInfo.nick_name)) {
                tvPhoneNum.setText(coocaaUserInfo.nick_name);
            } else {
                if (!TextUtils.isEmpty(coocaaUserInfo.getMobile())) {
                    String mobile = coocaaUserInfo.getMobile().substring(0, 3)
                            + "****" + coocaaUserInfo.getMobile().substring(7);
                    tvPhoneNum.setText(mobile);
                }
            }

            String avatar = coocaaUserInfo.getAvatar();
            if (TextUtils.isEmpty(avatar)) {
                Glide.with(this)
                        .load(R.drawable.icon_mine_default_unhead)
                        .into(ivCover);
            } else if (avatar.startsWith("https")) {
                Glide.with(this)
                        .load(avatar)
                        .error(R.drawable.icon_mine_default_unhead)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(ivCover);
            } else if (avatar.startsWith("http")) {
                String avatar2 = avatar.replaceFirst("http", "https");
                Glide.with(this)
                        .load(avatar2)
                        .error(R.drawable.icon_mine_default_unhead)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE).into(ivCover);
            }
        } else {
            loginLayout.setVisibility(View.GONE);
            unLoginLayout.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(R.drawable.icon_mine_default_head)
                    .into(ivCover);
        }
    }
}
