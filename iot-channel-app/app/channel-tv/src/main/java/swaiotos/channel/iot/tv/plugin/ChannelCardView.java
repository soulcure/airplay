package swaiotos.channel.iot.tv.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.skyworth.smarthome_tv.smarthomeplugininterface.IViewBoundaryCallback;
import com.skyworth.smarthome_tv.smarthomeplugininterface.LifeCycleCallback;
import com.skyworth.util.Util;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.usecase.BindCallBackUseCase;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.tv.MainActivity;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.adapter.PluginDevicesAdapter;
import swaiotos.channel.iot.tv.view.ChangeTextSpaceView;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.plugin
 * @ClassName: ChannelCardView
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/8/20 10:31
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/8/20 10:31
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ChannelCardView extends FrameLayout implements LifeCycleCallback,ChannelCard,View.OnKeyListener,
        View.OnFocusChangeListener,View.OnClickListener,View.OnTouchListener {
    private static final String TAG = ChannelCardView.class.getSimpleName();
    private IViewBoundaryCallback mCallback;

    private ImageView mBindQRCodeImageView,mMoreTips, mUnderline;;
    private ChangeTextSpaceView mBindCodeTextView;
    private TextView mDeviceSizeTextView;
    private RecyclerView mRecyclerView;

    private Context mContext;
    private ScheduledExecutorService mBindExecutorService;
    private String mOldBindCode,mBindCode;
    private FrameLayout mRelativeLayout;
    private String mAccessToken;
    private RelativeLayout mLayout;
    private PluginDevicesAdapter mPluginDevicesAdapter;
    private ArrayList<Device> devices;
    private boolean mIsFirst = true;

    public ChannelCardView(Context context, IViewBoundaryCallback callback) {
        super(context);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    public ChannelCardView(@NonNull Context context, @Nullable AttributeSet attrs, IViewBoundaryCallback callback) {
        super(context, attrs);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    public ChannelCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, IViewBoundaryCallback callback) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    private void initCarContentView() {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        mRelativeLayout = (FrameLayout) layoutInflater.inflate(R.layout.channel_plugin_cardview,null);

        mBindCodeTextView = mRelativeLayout.findViewById(R.id.plugin_channel_bind_code);
        mBindCodeTextView.setSpacing(getResources().getDimension(R.dimen.px_0));
        mBindQRCodeImageView = mRelativeLayout.findViewById(R.id.plugin_channel_bind_Qrcode);
        mDeviceSizeTextView = mRelativeLayout.findViewById(R.id.plugin_channel_device_size);
        mRecyclerView = mRelativeLayout.findViewById(R.id.plugin_channel_recycleview);

        mLayout = mRelativeLayout.findViewById(R.id.plugin_channel_device_layout);
        mMoreTips = mRelativeLayout.findViewById(R.id.plugin_channel_more_tips);
        mLayout.setVisibility(View.INVISIBLE);
        mUnderline = mRelativeLayout.findViewById(R.id.plugin_channel_underline);

        addView(mRelativeLayout);

        setClickable(true);
        setPressed(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
        setOnClickListener(this);
        setOnFocusChangeListener(this);
        setOnKeyListener(this);

        onChannelCoreData();
    }

    @Override
    public void onChannelCoreData() {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context swaiotosContext = mContext.createPackageContext("swaiotos.channel.iot",
                            Context.CONTEXT_IGNORE_SECURITY);
                    mAccessToken = ShareUtls.getInstance(swaiotosContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                    String devicesJson = ShareUtls.getInstance(swaiotosContext).getString(swaiotos.channel.iot.ss.server.utils.Constants.COOCAA_PREF_DEVICEs_LIST,"");
                    if (!TextUtils.isEmpty(devicesJson)) {
                        ArrayList<Device> list = new ArrayList<>();
                        Map<String,String> map = JSONObject.parseObject(devicesJson, Map.class);
                        if (map != null && map.size() > 0) {
                            for (String key : map.keySet()) {
                                Device device = new Device();
                                device.parse(map.get(key));
                                list.add(device);
                            }
                            devices = list;
                        }
                    }

                    AndroidLog.androidLog("0-------------"+mAccessToken);
                    ThreadManager.getInstance().uiThread(new Runnable() {
                        @Override
                        public void run() {
                            setReflushrecyclerView();
                        }
                    });
                    onBindCode();
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidLog.androidLog(""+e.getMessage());
                }
            }
        });
    }

    @Override
    public void onDeliverPluginMessage(Bundle bundle) {
        try {
            String devicesJson = bundle.getString(swaiotos.channel.iot.ss.server.utils.Constants.COOCAA_PREF_DEVICEs_LIST);
            AndroidLog.androidLog("2------------:"+devicesJson);
            if (!TextUtils.isEmpty(devicesJson)) {
                ArrayList<Device> list = new ArrayList<>();
                Map<String,String> map = (Map<String,String>) JSONObject.parseObject(devicesJson, Map.class);
                if (map != null && map.size() > 0) {
                    for (String key : map.keySet()) {
                        Device device = new Device();
                        device.parse(map.get(key));
                        list.add(device);
                    }
                    devices = list;
                } else {
                    devices = null;
                }
            } else {
                devices = null;
            }
            ThreadManager.getInstance().uiThread(new Runnable() {
                @Override
                public void run() {
                    if (devices != null)
                     AndroidLog.androidLog("xx-----------"+devices.size());
                    else {
                        AndroidLog.androidLog("xx-----------"+devices);
                    }
                    setReflushrecyclerView();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            AndroidLog.androidLog("xx-----------"+e.getLocalizedMessage());
        }

    }

    @Override
    public void onShortcutDataChanged(int state) {
        if (state == 2) {
            //隐藏
            mIsFirst = false;

            if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                mBindExecutorService.shutdownNow();
                mBindExecutorService = null;
            }

        } else if (state == 1) {
            //显示
            if (!mIsFirst && !TextUtils.isEmpty(mAccessToken)) {
                onBindCode();
            }
        }
    }

    private void setReflushrecyclerView() {
        AndroidLog.androidLog("setReflushrecyclerView");
        if (devices != null && devices.size() > 0) {
            mDeviceSizeTextView.setText(String.format(getResources().getString(R.string.plugin_channel_device_size),""+devices.size()));
            if (devices.size() >= 2) {
                RelativeLayout.LayoutParams RL = new RelativeLayout.LayoutParams((int)getResources().getDimension(R.dimen.px_360),
                        (int)getResources().getDimension(R.dimen.px_200));
                RL.topMargin = (int)getResources().getDimension(R.dimen.px_20);
                RL.addRule(RelativeLayout.BELOW,R.id.plugin_channel_tip);
                mRecyclerView.setLayoutParams(RL);
                if (devices.size() == 2)
                    mMoreTips.setVisibility(View.GONE);
                else
                    mMoreTips.setVisibility(View.VISIBLE);
            } else {
                RelativeLayout.LayoutParams RL = new RelativeLayout.LayoutParams((int)getResources().getDimension(R.dimen.px_360),
                        (int)getResources().getDimension(R.dimen.px_100));
                RL.topMargin = (int)getResources().getDimension(R.dimen.px_20);
                RL.addRule(RelativeLayout.BELOW,R.id.plugin_channel_tip);
                mRecyclerView.setLayoutParams(RL);

                mMoreTips.setVisibility(View.GONE);
            }
            mLayout.setVisibility(View.VISIBLE);
            if (mPluginDevicesAdapter == null) {
                mPluginDevicesAdapter = new PluginDevicesAdapter(getContext(),devices);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
                mRecyclerView.setAdapter(mPluginDevicesAdapter);
            } else {
                mPluginDevicesAdapter.setDevices(devices);
                mPluginDevicesAdapter.notifyDataSetChanged();
            }

        } else {
            AndroidLog.androidLog(mLayout+" ------- "+mLayout.getVisibility());
            mDeviceSizeTextView.setText(String.format(getResources().getString(R.string.plugin_channel_device_size),""+0));
            if (mPluginDevicesAdapter != null) {
                mPluginDevicesAdapter.setDevices(devices);
                mPluginDevicesAdapter.notifyDataSetChanged();
            }
            mLayout.setVisibility(View.INVISIBLE);
            mUnderline.setVisibility(View.INVISIBLE);

        }

    }


    private void onBindCode() {

        QRCodeUseCase.getInstance(mContext).run(new QRCodeUseCase.RequestValues(mAccessToken, TYPE.TV), new QRCodeUseCase.QRCodeCallBackListener() {
            @Override
            public void onError(String errType,String msg) {
                Log.d(TAG, "QRCodeUseCase:" + msg);
            }

            @Override
            public void onSuccess(final String bindCode, final String url,final String expiresIn,final String typeLoopTime) {
                //加载二维码
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (StringUtils.isEmpty(bindCode)) return;

                        if (TextUtils.isEmpty(url)) {
                            Bitmap mBitmap = CodeUtils.createImage(PublicParametersUtils.getURLAndBindCode(bindCode),(int)getResources().getDimension(R.dimen.px_256), (int)getResources().getDimension(R.dimen.px_256), null);
                            mBindQRCodeImageView.setImageBitmap(mBitmap);
                        } else {
                            Bitmap mBitmap = CodeUtils.createImage(url,(int)getResources().getDimension(R.dimen.px_256), (int)getResources().getDimension(R.dimen.px_256), null);
                            mBindQRCodeImageView.setImageBitmap(mBitmap);
                        }

                        mBindCodeTextView.setText(bindCode);
                    }
                });

                mOldBindCode = mBindCode;
                mBindCode = bindCode;

                if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                    mBindExecutorService.shutdownNow();
                    mBindExecutorService = null;
                }
                mBindExecutorService = Executors.newSingleThreadScheduledExecutor();
                mBindExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        onBindCode();
                    }
                }, Integer.parseInt(expiresIn), Integer.parseInt(expiresIn), TimeUnit.SECONDS);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && mCallback != null) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    Log.d(TAG, "KEYCODE_DPAD_UP");
                    return mCallback.onTopBoundary(view);
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    Log.d(TAG, "KEYCODE_DPAD_RIGHT");
                    return mCallback.onRightBoundary(view);
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    Log.d(TAG, "KEYCODE_DPAD_DOWN");
                    return mCallback.onDownBoundary(view);
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    Log.d(TAG, "KEYCODE_DPAD_LEFT");
                    return mCallback.onLeftBoundary(view);
                case KeyEvent.KEYCODE_BACK:
                    return mCallback.onBackKey(view);
                default:
                    break;
            }
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        AndroidLog.androidLog(mContext.getPackageName()+"   "+MainActivity.class.getName());
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mRelativeLayout != null) {
                mRelativeLayout.setBackground(mContext.getResources().getDrawable(R.drawable.shape_rectangle_stroke_4));
            }
        } else {
            if (mRelativeLayout != null) {
                mRelativeLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
        }
        Util.forceFocusAnim(mRelativeLayout,hasFocus);
    }


    @Override
    public void onResume() {
        Log.d(TAG,"0-----------onResume--------");
    }

    @Override
    public void onPause() {
        Log.d(TAG,"0-----------onPause--------");
    }

    @Override
    public void onStop() {
        Log.d(TAG,"0-----------onStop--------");
    }

    @Override
    public void onDestroy() {

        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
        mCallback = null;
    }


}
