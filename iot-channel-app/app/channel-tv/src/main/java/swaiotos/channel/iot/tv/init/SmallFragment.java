package swaiotos.channel.iot.tv.init;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.adapter.AutoAdapter;
import swaiotos.channel.iot.tv.base.MvpFragment;
import swaiotos.channel.iot.tv.view.AutoScrollRecyclerView;
import swaiotos.channel.iot.utils.NetUtils;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class SmallFragment extends MvpFragment<SmallContract.Presenter> implements SmallContract.View {

    private final String TAG = SmallFragment.class.getSimpleName();
    private SmallContract.Presenter mPresenter;
    private ImageView mBindQRCodeImageView;
    private ScheduledExecutorService mFlushExecutorServices;
    private AutoScrollRecyclerView mAutoScrollRecyclerView;
    private LinearSmoothScroller mSmoothScroller;
    private int mCurrentPosition = 0 ;
    private int[] ids = {R.drawable.small_1,R.drawable.small_2,R.drawable.small_3,R.drawable.small_4,R.drawable.small_5};
    private RecyclerViewOnScroll recyclerViewOnScroll;
    private AtomicBoolean isSwaiotos = new AtomicBoolean(true);
    private ImageView mSmallOval1,mSmallOval2,mSmallOval3,mSmallOval4,mSmallOval5;

    public static SmallFragment newInstance(String param1, String param2) {
        SmallFragment fragment = new SmallFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_small, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBindQRCodeImageView = view.findViewById(R.id.small_bind_Qrcode);
        mAutoScrollRecyclerView = view.findViewById(R.id.autoScrollRecyclerView);
        mSmallOval1 = view.findViewById(R.id.small_oval_1);
        mSmallOval2 = view.findViewById(R.id.small_oval_2);
        mSmallOval3 = view.findViewById(R.id.small_oval_3);
        mSmallOval4 = view.findViewById(R.id.small_oval_4);
        mSmallOval5 = view.findViewById(R.id.small_oval_5);
        setupRecyclerView();
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG,"------------onActivityCreated-------------");
        mPresenter = new SmallPresenter(this);

        View decorView = getActivity().getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    return;
                }
                initData();
            }
        });

    }

    private void initData() {
        SmallContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.init(getActivity().getApplicationContext());
        }
    }


    @Override
    public void setPresenter(SmallContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return getActivity() != null && !getActivity().isFinishing() &&  isAdded();
    }

    @Override
    protected SmallContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onUnBindCallBack(String sid) {

    }

    @Override
    public void refushOrUpdateQRCode(String qrcodeInfo, String url,String qrcodeExpire) {

        if (StringUtils.isEmpty(qrcodeInfo)) return;

        if (TextUtils.isEmpty(url)) {
            Bitmap mBitmap = CodeUtils.createImage(PublicParametersUtils.getURLAndBindCode(qrcodeInfo),(int)getResources().getDimension(R.dimen.px_256), (int)getResources().getDimension(R.dimen.px_256), null);
            mBindQRCodeImageView.setImageBitmap(mBitmap);
        } else {
            Bitmap mBitmap = CodeUtils.createImage(url,(int)getResources().getDimension(R.dimen.px_336), (int)getResources().getDimension(R.dimen.px_336), null);
            mBindQRCodeImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void triggerQuerDevices() {
        startAuto();
    }

    @Override
    public void refreshTips() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(),getResources().getString(R.string.iot_channel_bind_success),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setupRecyclerView() {
        AutoAdapter adAdapter = new AutoAdapter(getContext(),ids);
        mAutoScrollRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mAutoScrollRecyclerView.setAdapter(adAdapter);
        recyclerViewOnScroll = new RecyclerViewOnScroll();
        mAutoScrollRecyclerView.setOnScrollListener(recyclerViewOnScroll);
        mSmoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return  0.3f;
            }
        };
    }

    private void startAuto() {

        if (mFlushExecutorServices != null && !mFlushExecutorServices.isShutdown()) {
            mFlushExecutorServices.shutdownNow();
        }
        mCurrentPosition = 0;
        mFlushExecutorServices = Executors.newSingleThreadScheduledExecutor();

        mFlushExecutorServices.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isActive()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isActive()) {
                                mCurrentPosition++;
                                mSmoothScroller.setTargetPosition(mCurrentPosition);
                                RecyclerView.LayoutManager layoutManager = mAutoScrollRecyclerView.getLayoutManager();
                                if (layoutManager!=null)
                                    layoutManager.startSmoothScroll(mSmoothScroller);
                            }
                        }
                    });
                }
            }
        },5,7,TimeUnit.SECONDS);
    }


    class RecyclerViewOnScroll extends RecyclerView.OnScrollListener {
        //用来标记是否正在向最后一个滑动
        boolean isSlidingToLast = false;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            // 当不滚动时
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                //获取最后一个完全显示的ItemPosition
                int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                int totalItemCount = manager.getItemCount();

                // 判断是否滚动到底部，并且是向右滚动
                if (lastVisibleItem == (totalItemCount - 1) && isSlidingToLast) {
                    //加载更多功能的代码
                }
            }

            switch (newState){
                /*正在拖拽*/
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    break;
                /*滑动停止*/
                case RecyclerView.SCROLL_STATE_IDLE:
                    isSwaiotos.compareAndSet(false,true);
                    break;
                /*惯性滑动中*/
                case RecyclerView.SCROLL_STATE_SETTLING:
                    if (isSwaiotos.get()) {
                        isSwaiotos.compareAndSet(true,false);
                        if (mCurrentPosition%ids.length == 0) {
                            mSmallOval1.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
                            mSmallOval2.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval3.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval4.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval5.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                        } else if (mCurrentPosition%ids.length == 1) {
                            mSmallOval1.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval2.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
                            mSmallOval3.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval4.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval5.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                        } else if (mCurrentPosition%ids.length == 2) {
                            mSmallOval1.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval2.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval3.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
                            mSmallOval4.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval5.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                        } else if (mCurrentPosition%ids.length == 3) {
                            mSmallOval1.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval2.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval3.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval4.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
                            mSmallOval5.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                        } else if (mCurrentPosition%ids.length == 4) {
                            mSmallOval1.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval2.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval3.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval4.setBackground(getResources().getDrawable(R.drawable.shape_val_white_40));
                            mSmallOval5.setBackground(getResources().getDrawable(R.drawable.shape_val_white));
                        }
                    }
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
        }
    }
}
