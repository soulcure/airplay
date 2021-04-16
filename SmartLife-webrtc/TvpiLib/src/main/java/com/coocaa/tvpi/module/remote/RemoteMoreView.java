//package com.coocaa.tvpi.module.remote;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.CompoundButton;
//import android.widget.RelativeLayout;
//import android.widget.Switch;
//import android.widget.TextView;
//
//import com.coocaa.publib.utils.DimensUtils;
//import com.coocaa.publib.utils.SpUtil;
//import com.coocaa.publib.utils.ToastUtils;
//import com.coocaa.smartscreen.connect.SSConnectManager;
//import com.coocaa.smartscreen.data.channel.AppStoreParams;
//import com.coocaa.smartscreen.data.device.Source;
//import com.coocaa.smartscreen.utils.CmdUtil;
//import com.coocaa.tvpi.module.remote.adapter.RemoteTvSourceAdapter;
//import com.coocaa.tvpi.view.decoration.CommonHorizontalItemDecoration;
//import com.coocaa.tvpilib.R;
//
//import java.util.List;
//
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
///**
// * @ClassName RemoteMoreView
// * @User heni
// * @Date 2020/4/17
// */
//public class RemoteMoreView extends RelativeLayout {
//    private static final String TAG = RemoteMoreView.class.getSimpleName();
//    public static String REMOTE_MODE_KEY = "REMOTE_MODE_KEY";
//
//    private Context mContext;
//    private View keyModeView, touchModeView;
//    private TextView tvKeyMode;
//    private TextView tvTouchMode;
//    private Switch vibrateSwitch; // 震动开关
//    private TextView muteTV;
//    private TextView clearTV;å
//    private View okBtn;
//
//    private RecyclerView recyclerView;
//    private RemoteTvSourceAdapter adapter;
//
//    private RemoteMoreCallback remoteMoreCallback;
//
//    public void setRemoteMoreCallback(RemoteMoreCallback remoteMoreCallback) {
//        this.remoteMoreCallback = remoteMoreCallback;
//    }
//
//    public interface RemoteMoreCallback {
//        void onModeSelected(int mode);
//
//        void onOkBtnClick();
//    }
//
//    public RemoteMoreView(Context context) {
//        super(context);
//        this.mContext = context;
//        initView();
//        initListener();
//        setRemoteMode(SpUtil.getInt(mContext, REMOTE_MODE_KEY, 0));
//    }
//
//    public RemoteMoreView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.mContext = context;
//        initView();
//        initListener();
//        setRemoteMode(SpUtil.getInt(mContext, REMOTE_MODE_KEY, 0));
//    }
//
//    public RemoteMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.mContext = context;
//        initView();
//        initListener();
//        setRemoteMode(SpUtil.getInt(mContext, REMOTE_MODE_KEY, 0));
//    }
//
//    private void initView() {
//        LayoutInflater inflater =
//                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(R.layout.remote_more_view, this);
//
//        keyModeView = findViewById(R.id.remote_more_mode_key_content);
//        touchModeView = findViewById(R.id.remote_more_mode_touch_content);
//        tvKeyMode = findViewById(R.id.remote_more_tv_key_mode);
//        tvTouchMode = findViewById(R.id.remote_more_tv_touch_mode);
//        vibrateSwitch = findViewById(R.id.remote_more_vibrate_switch);
//        muteTV = findViewById(R.id.remote_more_mute_btn);
//        clearTV = findViewById(R.id.remote_more_clear_btn);
//        okBtn = findViewById(R.id.remote_more_tv_bottom);
//
//        recyclerView = findViewById(R.id.remote_more_tv_source_recyclerview);
//        recyclerView.setHasFixedSize(true);
//        CommonHorizontalItemDecoration decoration =
//                new CommonHorizontalItemDecoration(DimensUtils.dp2Px(mContext, 20f),
//                        DimensUtils.dp2Px(mContext, 10f));
//        recyclerView.addItemDecoration(decoration);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext,
//                LinearLayoutManager.HORIZONTAL, false);
//        recyclerView.setLayoutManager(linearLayoutManager);
//
//        adapter = new RemoteTvSourceAdapter(mContext);
//        recyclerView.setAdapter(adapter);
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                //当前RecyclerView显示出来的最后一个的item的position
//                int lastPosition = -1;
//
//                //当前状态为停止滑动状态SCROLL_STATE_IDLE时
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                    if (layoutManager instanceof GridLayoutManager) {
//                        //通过LayoutManager找到当前显示的最后的item的position
//                        lastPosition = ((GridLayoutManager) layoutManager)
//                                .findLastVisibleItemPosition();
//                    } else if (layoutManager instanceof LinearLayoutManager) {
//                        lastPosition = ((LinearLayoutManager) layoutManager)
//                                .findLastVisibleItemPosition();
//                    }
//                }
//            }
//        });
//    }
//
//    private void initListener() {
//        keyModeView.setOnClickListener(remoteModeClickLis);
//        touchModeView.setOnClickListener(remoteModeClickLis);
//        okBtn.setOnClickListener(remoteModeClickLis);
//        clearTV.setOnClickListener(remoteModeClickLis);
//
//        vibrateSwitch.setChecked(SpUtil.getBoolean(mContext, SpUtil.Keys.REMOTE_VIBRATE, true));
//        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                SpUtil.putBoolean(mContext, SpUtil.Keys.REMOTE_VIBRATE, isChecked);
//            }
//        });
//
//        muteTV.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CmdUtil.sendKey(KeyEvent.KEYCODE_VOLUME_MUTE);
//            }
//        });
//    }
//
//    OnClickListener remoteModeClickLis = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (v.getId() == keyModeView.getId()) {
//                setRemoteMode(0);
//                if (null != remoteMoreCallback) {
//                    remoteMoreCallback.onOkBtnClick();
//                }
//                ToastUtils.getInstance().showGlobalShort("已设置为：按键模式");
//            } else if (v.getId() == touchModeView.getId()) {
//                setRemoteMode(1);
//                if (null != remoteMoreCallback) {
//                    remoteMoreCallback.onOkBtnClick();
//                }
//                ToastUtils.getInstance().showGlobalShort("已设置为：触摸模式");
//            } else if (v.getId() == okBtn.getId()) {
//                if (null != remoteMoreCallback) {
//                    remoteMoreCallback.onOkBtnClick();
//                }
//            }else if(v.getId() == clearTV.getId()){
//                if (!SSConnectManager.getInstance().isConnected()) {
//                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
//                    return;
//                }
//                String cmdString = AppStoreParams.CMD.SKY_COMMAND_APPSTORE_MOBILE_ONEKEY_SPEEDUP.toString();
//                AppStoreParams params = new AppStoreParams();
//                CmdUtil.sendAppCmd(cmdString, params.toJson());
//                ToastUtils.getInstance().showGlobalLong("已清理电视应用");
//            }
//        }
//    };
//
//    private void setRemoteMode(int mode) {
//        if (mode == 0) {//按键模式
//            keyModeView.setSelected(true);
//            tvKeyMode.setTextColor(getResources().getColor(R.color.ff4681ff));
//            touchModeView.setSelected(false);
//            tvTouchMode.setTextColor(getResources().getColor(R.color.c_2));
//        } else {//触摸模式
//            touchModeView.setSelected(true);
//            tvTouchMode.setTextColor(getResources().getColor(R.color.ff4681ff));
//            keyModeView.setSelected(false);
//            tvKeyMode.setTextColor(getResources().getColor(R.color.c_2));
//        }
//        if (null != remoteMoreCallback) {
//            remoteMoreCallback.onModeSelected(mode);
//        }
//        SpUtil.putInt(mContext, REMOTE_MODE_KEY, mode);
//    }
//
//    public void setSystemInfoData(List<Source> lists, String currentSource) {
//        adapter.addAll(lists, currentSource);
//        if (adapter.getSelectedPosition() >= 0) {
//            recyclerView.scrollToPosition(adapter.getSelectedPosition());
//        }
//    }
//}
