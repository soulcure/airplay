package com.coocaa.tvpi.module.mine.lab;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.homepager.cotroller.MirrorScreenController;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.live.LiveTipDialogActivity;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.view.decoration.PictureItemDecoration;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.swaiotos.skymirror.sdk.capture.MirManager;
import com.umeng.analytics.MobclickAgent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
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
public class SmartLabFragment2 extends Fragment {

    private final String TAG = "SmartLab";
    private final String TAG2 = "MirrorScreen";

    private RelativeLayout rootlayout;
    private NestedScrollView mNestedScrollView;
    private ImageView backView;
    private RecyclerView recyclerView;
    GridLayoutManager layoutManager;
    private LabAdapter adapter;

    private RelativeLayout mirrorScreenView;
    private RelativeLayout computerView;
    private RelativeLayout movieView;
    private TextView mirrorTitle;

    private FastClick fastClick = new FastClick();
    private MirrorScreenController mirrorScreenController;
    private int ITEM_COLUMN = 4;

    private String name_mirror = "屏幕镜像";
    private String name_mirroring = "正在镜像中...";
    private static String id_mirror = "com.coocaa.smart.mirror";
    private static boolean isMirroring = false;
    private static boolean isDevelopMode = false; //开发模式打开，加载一些debug内容

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (rootlayout == null) {
            initView(inflater);
            initTopView();
        }
        return rootlayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mirrorScreenController = new MirrorScreenController(this, mirrorScreenListener);
        isMirroring = MirManager.instance().isMirRunning();
        Log.d(TAG2, "onViewCreated...isMirroring: " + isMirroring);
        updateMirrorUI();
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

    //转发到MirrorScreenController
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", data=" + data);
        if (requestCode == MIRROR_SCREEN_REQUEST_CODE && mirrorScreenController != null) {
            mirrorScreenController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initView(LayoutInflater inflater) {
        rootlayout = (RelativeLayout) inflater.inflate(R.layout.activity_smartlab_v2, null);
        mNestedScrollView = rootlayout.findViewById(R.id.nested_scroll);
        recyclerView = rootlayout.findViewById(R.id.smart_lab_recyclerview);
        layoutManager = new GridLayoutManager(getContext(), ITEM_COLUMN);
        PictureItemDecoration decoration = new PictureItemDecoration(ITEM_COLUMN,
                DimensUtils.dp2Px(getContext(), 20), 0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);

        adapter = new LabAdapter(this);
        recyclerView.setAdapter(adapter);

        backView = rootlayout.findViewById(R.id.back_img);
        backView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    getActivity().finish();
            }
        });
    }

    private void initTopView() {
        mirrorScreenView = rootlayout.findViewById(R.id.item_mirror_screen);
        computerView = rootlayout.findViewById(R.id.item_computer);
        movieView = rootlayout.findViewById(R.id.item_movie);

        mirrorTitle = mirrorScreenView.findViewById(R.id.tv_lab_name);
        mirrorTitle.setText("屏幕镜像");
        ImageView icon1 = mirrorScreenView.findViewById(R.id.lab_icon);
        ImageView icon2 = computerView.findViewById(R.id.lab_icon);
        ImageView icon3 = movieView.findViewById(R.id.lab_icon);
        TextView title2 = computerView.findViewById(R.id.tv_lab_name);
        TextView title3 = movieView.findViewById(R.id.tv_lab_name);
        TextView subtitle1 = mirrorScreenView.findViewById(R.id.tv_lab_detail);
        TextView subtitle2 = computerView.findViewById(R.id.tv_lab_detail);
        TextView subtitle3 = movieView.findViewById(R.id.tv_lab_detail);

        icon1.setImageResource(R.drawable.lab_mirror_screen);
        icon2.setImageResource(R.drawable.lab_computer);
        icon3.setImageResource(R.drawable.lab_movie);
        title2.setText("电脑投电视");
        title3.setText("看影视");
        subtitle1.setText("将手机上的内容在电视上镜像显示");
        subtitle2.setText("将电脑上的内容投送到电视上");
        subtitle3.setText("在电视上看影视内容");

        mirrorScreenView.setOnClickListener( v -> {
            Log.d(TAG, "onMirrorClick...");
            mirrorScreen();
        });

        computerView.setOnClickListener(v -> {

        });

        movieView.setOnClickListener(v -> {

        });
    }

    private void updateMirrorUI() {
        //更新屏幕镜像状态
        if (isMirroring) {
            mirrorTitle.setText(name_mirroring);
        } else {
            mirrorTitle.setText(name_mirror);
        }
    }

    private void mirrorScreen() {
        Log.d(TAG, "start switch mirrorScreen, cur is mirroring=" + isMirroring);
        MobclickAgent.onEvent(getContext(), MAIN_PAGE_CAST_PHONE);

        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            showConnectDialog();
            return;
        }
        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            ToastUtils.getInstance().showGlobalShort(R.string.not_same_wifi_tips);
            return;
        }
        mirrorScreenController.switchMirrorScreen();
    }

    private void showConnectDialog() {
        ConnectDialogActivity.start(getActivity());
    }

    private MirrorScreenController.MirrorScreenListener mirrorScreenListener
            = new MirrorScreenController.MirrorScreenListener() {
        @Override
        public void onStartMirrorScreen() {
            Log.d(TAG2, "onStartMirrorScreen: ");
            updateMirrorState(false);
        }

        @Override
        public void onMirroringScreen() {
            Log.d(TAG2, "onMirroringScreen: ");
            updateMirrorState(true);
        }

        @Override
        public void onStopMirrorScreen() {
            Log.d(TAG2, "onStopMirrorScreen: ");
            updateMirrorState(false);
        }
    };

    private void updateMirrorState(boolean _isMirror) {
        Log.d(TAG2, "updateMirrorState, _isMirror=" + _isMirror);
        isMirroring = _isMirror;
        updateMirrorUI();
    }

    private void loadData() {
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                List<FunctionBean> functionBeanList = new LinkedList<>();

                Gson gson = new Gson();
                Type type = new TypeToken<List<FunctionBean>>() {}.getType();

                String normalDataString = AndroidUtil.readAssetFile("json/smart_lab_dongle.json");
                if(!TextUtils.isEmpty(normalDataString)) {
                    List<FunctionBean> normalDataList = gson.fromJson(normalDataString, type);
                    if (normalDataList != null)
                        functionBeanList.addAll(normalDataList);
                }

                if (SmartConstans.getBuildInfo().debug || isDevelopMode) {
                    //add debug data.
                    String debugDataString = AndroidUtil.readAssetFile("json/smart_lab_ext_debug.json");
                    List<FunctionBean> debugDataList = gson.fromJson(debugDataString, type);
                    if (debugDataList != null)
                        functionBeanList.addAll(debugDataList);
//                    functionBeanList.addAll(HomeHttpMethod.getInstance().getCachedFunctionList());

                    //判断是否有手动放置的json配置文件
                    try {
                        StringBuilder sb = new StringBuilder();
                        InputStream is = new FileInputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        is.close();
                        List<FunctionBean> extList = gson.fromJson(sb.toString(), type);
                        if (extList != null)
                            functionBeanList.addAll(extList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    mNestedScrollView.setOnTouchListener(onTouchListener);
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
        WeakReference<SmartLabFragment2> ref;

        public LabAdapter(SmartLabFragment2 activity) {
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

    private void onFunctionClick(FunctionBean bean) {
        if (!fastClick.isFaskClick()) {
            if ("com.coocaa.smart.donglevirtualinput".equals(bean.id)) {
                if (!SSConnectManager.getInstance().isConnected()) {
                    ToastUtils.getInstance().showGlobalLong("请先连接设备");
                    return;
                } else {
                    CmdUtil.startSettingApp();
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
            } else if("com.coocaa.smart.screenshot".equals(bean.id)) {
                int connectState = SSConnectManager.getInstance().getConnectState();
                final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
                //未连接
                if(connectState == CONNECT_NOTHING || deviceInfo == null){
                    ConnectDialogActivity.start(getActivity());
                    return;
                }
                //本地连接不通
                if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
                    WifiConnectActivity.start(getActivity());
                    return;
                }
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
        mNestedScrollView.setOnTouchListener(null);
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