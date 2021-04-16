package com.coocaa.tvpi.module.mine.lab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.coocaa.publib.base.GlideApp;
import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.function.FunctionBean;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.smartscreen.utils.AndroidUtil;
import com.coocaa.smartscreen.utils.CmdUtil;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.live.LiveTipDialogActivity;
import com.coocaa.tvpi.module.local.document.page.DocumentPlayerActivity;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.umeng.analytics.MobclickAgent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;
import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_CAST_PHONE;
import static com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController.MIRROR_SCREEN_REQUEST_CODE;
import static com.coocaa.tvpi.module.live.LiveTipDialogActivity.KEY_URL;

/**
 * @Author: yuzhan
 */
public class SmartLabFragment extends Fragment {

    private MirrorScreenController mirrorScreenController;
    private LinearLayout layout;
    private ImageView backView;
    private RecyclerView recyclerView;
    GridLayoutManager layoutManager;
    private LabAdapter adapter;
    private FastClick fastClick = new FastClick();

    private final String TAG = "SmartLab";
    private int ITEM_COLUMN = 4;

    private String icon_mirror = "file:///android_asset/img/function_mirror_screen.png";
    private String icon_mirroring = "file:///android_asset/img/function_mirroring_screen.png";
    private String name_mirror = "屏幕镜像";
    private String name_mirroring = "镜像中";
    private static String id_mirror = "com.coocaa.smart.mirror";
    private static boolean isMirroring = false;
    private static boolean isDevelopMode = false; //开发模式打开，加载一些debug内容

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (layout == null) {
            initView(inflater);
        }
        return layout;
    }

    private void initView(LayoutInflater inflater) {
        layout = (LinearLayout) inflater.inflate(R.layout.activity_smartlab, null);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setOrientation(LinearLayout.VERTICAL);

        recyclerView = layout.findViewById(R.id.smart_lab_recyclerview);

        layoutManager = new GridLayoutManager(getContext(), ITEM_COLUMN);
        recyclerView.setLayoutManager(layoutManager);
        PictureItemDecoration itemDecoration = new PictureItemDecoration(ITEM_COLUMN,
                DimensUtils.dp2Px(getContext(), 20), DimensUtils.dp2Px(getContext(), 0));
        recyclerView.addItemDecoration(itemDecoration);
        adapter = new LabAdapter(this);
        recyclerView.setAdapter(adapter);

        backView = layout.findViewById(R.id.back_img);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().finish();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated...");
        super.onViewCreated(view, savedInstanceState);
        mirrorScreenController = new MirrorScreenController(this, mirrorScreenListener);
        isMirroring = MirManager.instance().isMirRunning();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mirrorScreenController.resume();
    }

    @Override
    public void onDestroy() {
//        mirrorScreenController.destroy();
        super.onDestroy();
    }

    private void mirrorScreen() {
        Log.d(TAG, "start switch mirrorScreen, cur is mirroring=" + isMirroring);
        MobclickAgent.onEvent(getContext(), MAIN_PAGE_CAST_PHONE);

        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if (connectState == CONNECT_NOTHING || deviceInfo == null) {
            showConnectDialog();
            return;
        }
        //本地连接不通
        if (!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)) {
            ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
            return;
        }

        mirrorScreenController.switchMirrorScreen();
    }

    //转发到MirrorScreenController
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", data=" + data);
        if (requestCode == MIRROR_SCREEN_REQUEST_CODE && mirrorScreenController != null) {
            mirrorScreenController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateMirrorState(boolean _isMirror) {
        Log.d(TAG, "updateMirrorState, _isMirror=" + _isMirror);
        isMirroring = _isMirror;
        refreshMirrorFunctionUI();
    }

    private void showConnectDialog() {
        ConnectDialogActivity.start(getActivity());
    }

    private MirrorScreenController.MirrorScreenListener mirrorScreenListener
            = new MirrorScreenController.MirrorScreenListener() {
        @Override
        public void onStartMirrorScreen() {
            updateMirrorState(false);
        }

        @Override
        public void onMirroringScreen() {
            updateMirrorState(true);
        }

        @Override
        public void onStopMirrorScreen() {
            updateMirrorState(false);
        }
    };

    private void loadData() {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<FunctionBean> functionBeanList = new LinkedList<>();

                Gson gson = new Gson();
                Type type = new TypeToken<List<FunctionBean>>() {
                }.getType();
                //String normalDataString = AndroidUtil.readAssetFile(isDongle ? "json/smart_lab_dongle.json" : "json/smart_lab_tv.json");
                String normalDataString = AndroidUtil.readAssetFile("json/smart_lab_dongle.json");
                List<FunctionBean> normalDataList = gson.fromJson(normalDataString, type);
                if (normalDataList != null)
                    functionBeanList.addAll(normalDataList);

                //更新屏幕镜像状态
                for (FunctionBean bean : functionBeanList) {
                    if ("com.coocaa.smart.mirror".equals(bean.id)) {
                        bean.icon = isMirroring ? icon_mirroring : icon_mirror;
                        bean.name = isMirroring ? name_mirroring : name_mirror;
                    }
                }

                if (SmartConstans.getBuildInfo().debug || isDevelopMode) {
                    //add debug data.
                    String debugDataString = AndroidUtil.readAssetFile("json/smart_lab_ext_debug.json");
                    List<FunctionBean> debugDataList = gson.fromJson(debugDataString, type);
                    if (debugDataList != null)
                        functionBeanList.addAll(debugDataList);
//                    functionBeanList.addAll(HomeHttpMethod.getInstance().getCachedFunctionList());
                } else {
                    recyclerView.setOnTouchListener(onTouchListener);
                }

                HomeUIThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setList(functionBeanList);
                    }
                });
            }
        });
    }

    static class LabAdapter extends BaseQuickAdapter<FunctionBean, BaseViewHolder> {
        WeakReference<SmartLabFragment> ref;

        public LabAdapter(SmartLabFragment activity) {
            super(R.layout.item_function_lab);
            ref = new WeakReference<>(activity);
        }

        @Override
        protected void convert(BaseViewHolder holder, FunctionBean functionBean) {
            holder.setText(R.id.tv_function_name, functionBean.name);
            ImageView image = holder.findView(R.id.iv_function);
            assert image != null;
            GlideApp.with(getContext())
                    .load(functionBean.icon)
                    .centerCrop()
                    .into(image);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ref.get().onFunctionClick(functionBean);
                }
            });
        }
    }

    private void refreshMirrorFunctionUI() {
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        Log.d(TAG, "first index=" + first + ", last=" + last);
        FunctionBean functionBean;
        for (int i = Math.max(first, 0); i <= last && adapter.getItemCount() > i; i++) {
            functionBean = adapter.getItem(i);
            if (isMirrorFunction(functionBean)) {
                functionBean.icon = isMirroring ? icon_mirroring : icon_mirror;
                functionBean.name = isMirroring ? name_mirroring : name_mirror;
                refreshMirrorUI(i);
                break; //我们认为目前一个页面只会有一个tab
            }
        }
    }

    private void refreshMirrorUI(final int index) {
        Log.d(TAG, "refresh mirror function index=" + index);
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemChanged(index);
            }
        });
    }

    private void onFunctionClick(FunctionBean bean) {
        if (!fastClick.isFaskClick()) {
            if (isMirrorFunction(bean)) {
                Log.d(TAG, "onMirrorClick...");
                mirrorScreen();
            } else if ("com.coocaa.smart.donglevirtualinput".equals(bean.id)) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
                    return;
                } else {
                    CmdUtil.sendKey(769);
                }
            } else if ("com.coocaa.smart.virtualinput".equals(bean.id)) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
                    return;
                }
            } else if (isLive(bean) && interceptLive()) {
//                显示弹框
                Intent intent = new Intent(getContext(), LiveTipDialogActivity.class);
                intent.putExtra(KEY_URL, bean.uri());
                getContext().startActivity(intent);
                return;
            }

            if (!isMirrorFunction(bean)) {
                TvpiClickUtil.onClick(getContext(), bean.uri());
            }
        }
    }

    private static boolean isMirrorFunction(FunctionBean functionBean) {
        return functionBean != null && id_mirror.equals(functionBean.id);
    }

    private void onDevelopModeOn() {
        Log.d(TAG, "onDevelopModeOn");
        clickCount = 0;
        HomeUIThread.removeTask(resetCountRunnable);
        isDevelopMode = true;
        recyclerView.setOnTouchListener(null);
        loadData();
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (isDevelopMode) {
                return false;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                clickCount++;
                if (clickCount >= 10) {
                    onDevelopModeOn();
                } else if (clickCount >= 5) {
                    ToastUtils.getInstance().showGlobalShort("再点击" + (10 - clickCount) + "次进入开发模式");
                }
                HomeUIThread.removeTask(resetCountRunnable);
                HomeUIThread.execute(1000, resetCountRunnable);
            }
            return false;
        }
    };

    private int clickCount = 0;
    private Runnable resetCountRunnable = new Runnable() {
        @Override
        public void run() {
            clickCount = 0;
            HomeUIThread.removeTask(resetCountRunnable);
        }
    };

    private boolean interceptLive() {
        return !Preferences.LiveTipConfirm.getLiveTipConfirm();
    }

    //直播需要判断是否拦截
    private boolean isLive(FunctionBean functionBean) {
        try {
            return "m.91kds.cn".equals(Uri.parse(functionBean.uri()).getHost());
        } catch (Exception e) {
            return false;
        }
    }

}