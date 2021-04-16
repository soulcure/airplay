package swaiotos.channel.iot.tv.plugin;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skyworth.smarthome_tv.smarthomeplugininterface.IViewBoundaryCallback;
import com.skyworth.smarthome_tv.smarthomeplugininterface.LifeCycleCallback;
import com.skyworth.util.Util;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.usecase.QRCodeUseCase;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.common.utils.TYPE;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.tv.MainActivity;
import swaiotos.channel.iot.tv.R;
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
public class ChannelPanelView extends FrameLayout implements LifeCycleCallback,ChannelCard,View.OnKeyListener,
        View.OnFocusChangeListener,View.OnClickListener,View.OnTouchListener {
    private static final String TAG = ChannelPanelView.class.getSimpleName();
    private static final String ACTION = "swaiotos.channel.iot.intent.flush.data";
    private IViewBoundaryCallback mCallback;

    private ImageView mBindQRCodeImageView;
    private ChangeTextSpaceView mBindCodeTextView;

    private Context mContext;
    private ScheduledExecutorService mBindExecutorService;
    private FrameLayout mRelativeLayout;
    private String mAccessToken;
    private boolean mIsFirst;
    private ProgressBar progressBar1,progressBar2;
    private TextView failure1,failure2;
    private int mCount = 0 ;//计数器
    private FlushBroadcastReceiver mFlushBroadcastReceiver;

    public ChannelPanelView(Context context, IViewBoundaryCallback callback) {
        super(context);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    public ChannelPanelView(@NonNull Context context, @Nullable AttributeSet attrs, IViewBoundaryCallback callback) {
        super(context, attrs);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    public ChannelPanelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, IViewBoundaryCallback callback) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initCarContentView();
        mCallback = callback;
    }

    private void initCarContentView() {
        AndroidLog.androidLog("---initCarContentView---");
        setClipChildren(false);
        setClipToPadding(false);

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        mRelativeLayout = (FrameLayout) layoutInflater.inflate(R.layout.channel_plugin_panelview,null);

        mBindCodeTextView = mRelativeLayout.findViewById(R.id.plugin_channel_bind_code);
        mBindCodeTextView.setSpacing(getResources().getDimension(R.dimen.px_0));
        mBindQRCodeImageView = mRelativeLayout.findViewById(R.id.plugin_channel_bind_Qrcode);
        progressBar1 = mRelativeLayout.findViewById(R.id.plugin_channel_progressBar1);
        progressBar2 = mRelativeLayout.findViewById(R.id.plugin_channel_progressBar2);
        failure1 = mRelativeLayout.findViewById(R.id.plugin_channel_failure1);
        failure2 = mRelativeLayout.findViewById(R.id.plugin_channel_failure2);

        progressBar1.setVisibility(View.VISIBLE);
        progressBar2.setVisibility(View.VISIBLE);
        failure1.setVisibility(View.INVISIBLE);
        failure2.setVisibility(View.INVISIBLE);

        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams((int)mContext.getResources().getDimension(R.dimen.px_870),
                (int)mContext.getResources().getDimension(R.dimen.px_370));
        fl.topMargin = -Util.Div(5);
        addView(mRelativeLayout,fl);

        setClickable(true);
        setPressed(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);
        setOnClickListener(this);
        setOnFocusChangeListener(this);
        setOnKeyListener(this);

        if (mFlushBroadcastReceiver == null) {
            mFlushBroadcastReceiver = new FlushBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION);
            mContext.registerReceiver(mFlushBroadcastReceiver,intentFilter);
        }


        mCount++;
        if ( NetUtils.isConnected(mContext.getApplicationContext()))
            onChannelCoreData();
        else {
            progressBar1.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            failure1.setVisibility(View.VISIBLE);
            failure2.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onChannelCoreData() {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context swaiotosContext = mContext.createPackageContext("swaiotos.channel.iot",
                            Context.CONTEXT_IGNORE_SECURITY);
                    AndroidLog.androidLog("----swaiotosContext---:"+swaiotosContext);
                    mAccessToken = ShareUtls.getInstance(swaiotosContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                    AndroidLog.androidLog("----mAccessToken---:"+mAccessToken  + " shareUtls:"+ShareUtls.getInstance(swaiotosContext));
                    if (TextUtils.isEmpty(mAccessToken)) {
                        IOTAdminChannel.mananger.open(mContext, "swaiotos.channel.iot", new IOTAdminChannel.OpenCallback() {
                            @Override
                            public void onConntected(SSAdminChannel channel) {
                                try {
                                    mAccessToken = channel.getDeviceAdminManager().getAccessToken();
                                    onBindCode();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(String s) {
                                progressBar1.setVisibility(View.INVISIBLE);
                                progressBar2.setVisibility(View.INVISIBLE);
                                failure1.setVisibility(View.VISIBLE);
                                failure2.setVisibility(View.VISIBLE);
                            }
                        });

                    } else {
                        onBindCode();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidLog.androidLog(""+e.getMessage());
                }
            }
        });
    }

    @Override
    public void onDeliverPluginMessage(Bundle bundle) {
        AndroidLog.androidLog("--onDeliverPluginMessage-");
        if (bundle == null)
            return;
        String COOCAA_REFLUSH_INSTALL = bundle.getString(Constants.COOCAA_REFLUSH_INSTALL);
        AndroidLog.androidLog("--onDeliverPluginMessage-:"+COOCAA_REFLUSH_INSTALL);
        if (!TextUtils.isEmpty(COOCAA_REFLUSH_INSTALL) && COOCAA_REFLUSH_INSTALL.equals(Constants.COOCAA_REFLUSH_INSTALL)) {
            onFlushBindCode();
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


    private void onBindCode() {

        QRCodeUseCase.getInstance(mContext).run(new QRCodeUseCase.RequestValues(mAccessToken, TYPE.TV), new QRCodeUseCase.QRCodeCallBackListener() {
            @Override
            public void onError(String errType,String msg) {
                Log.d(TAG, "QRCodeUseCase:" + msg);
                ThreadManager.getInstance().uiThread(new Runnable() {
                    @Override
                    public void run() {
                        failure1.setVisibility(View.VISIBLE);
                        failure2.setVisibility(View.VISIBLE);
                        progressBar1.setVisibility(View.INVISIBLE);
                        progressBar2.setVisibility(View.INVISIBLE);
                    }
                });
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

                        mBindQRCodeImageView.setVisibility(View.INVISIBLE);
                        mBindCodeTextView.setText(bindCode);
                        failure1.setVisibility(View.INVISIBLE);
                        failure2.setVisibility(View.INVISIBLE);
                        progressBar1.setVisibility(View.INVISIBLE);
                        progressBar2.setVisibility(View.INVISIBLE);
                    }
                });

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
        try {
//            AndroidLog.androidLog(mContext.getPackageName());
//            Intent intent = new Intent("swaiotos.channel.iot.action.tv");
//            intent.setPackage("swaiotos.channel.iot");
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(intent);


            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("swaiotos.channel.iot", "swaiotos.channel.iot.tv.MainActivity");
            intent.setComponent(cn);
            mContext.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (mRelativeLayout != null) {
                mRelativeLayout.setBackground(mContext.getResources().getDrawable(R.drawable.shape_rectangle_stroke_3));
            }
        } else {
            if (mRelativeLayout != null) {
                mRelativeLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));
            }
        }
        Util.forceFocusAnim(this,hasFocus);
    }


    @Override
    public void onResume() {
        mCount++;
        AndroidLog.androidLog("0-----------onResume--------:"+mCount);
        NetUtils.NetworkReceiver.register(mContext.getApplicationContext(),mNetworkReceiver);
        onFlushBindCode();

    }

    private void onFlushBindCode() {

        if ( !NetUtils.isConnected(mContext.getApplicationContext())) {
            progressBar1.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            failure1.setVisibility(View.VISIBLE);
            failure2.setVisibility(View.VISIBLE);

            return;
        }
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context swaiotosContext = mContext.createPackageContext("swaiotos.channel.iot",
                            Context.CONTEXT_IGNORE_SECURITY);
                    AndroidLog.androidLog("onFlushBindCode----swaiotosContext---:"+swaiotosContext);
                    mAccessToken = ShareUtls.getInstance(swaiotosContext).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                    AndroidLog.androidLog("onFlushBindCode----mAccessToken---:"+mAccessToken  + " shareUtls:"+ShareUtls.getInstance(swaiotosContext));
                    if (TextUtils.isEmpty(mAccessToken)) {
                        IOTAdminChannel.mananger.open(mContext, "swaiotos.channel.iot", new IOTAdminChannel.OpenCallback() {
                            @Override
                            public void onConntected(SSAdminChannel channel) {
                                try {
                                    mAccessToken = channel.getDeviceAdminManager().getAccessToken();
                                    onBindCode();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(String s) {
                                progressBar1.setVisibility(View.INVISIBLE);
                                progressBar2.setVisibility(View.INVISIBLE);
                                failure1.setVisibility(View.VISIBLE);
                                failure2.setVisibility(View.VISIBLE);
                            }
                        });

                    } else {
                        onBindCode();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    AndroidLog.androidLog(""+e.getMessage());
                }
            }
        });
    }

    @Override
    public void onPause() {
        AndroidLog.androidLog("0-----------onPause--------");
    }

    @Override
    public void onStop() {
        ShareUtls._instance = null;
        if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
            mBindExecutorService.shutdownNow();
            mBindExecutorService = null;
        }
        mCount--;
        AndroidLog.androidLog("0-----------onStop--------:"+mCount);
        NetUtils.NetworkReceiver.unregister(mContext.getApplicationContext(),mNetworkReceiver);
    }

    @Override
    public void onDestroy() {
        AndroidLog.androidLog("0-----------onDestroy--------");
        if (mFlushBroadcastReceiver != null) {
            mContext.unregisterReceiver(mFlushBroadcastReceiver);
            mFlushBroadcastReceiver = null;
        }
    }

    private NetUtils.NetworkReceiver mNetworkReceiver = new NetUtils.NetworkReceiver() {
        @Override
        public void onConnected() {
            onFlushBindCode();
        }

        @Override
        public void onDisconnected() {
            if (mBindExecutorService != null && !mBindExecutorService.isShutdown()) {
                mBindExecutorService.shutdownNow();
                mBindExecutorService = null;
            }
        }
    };

    private class FlushBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AndroidLog.androidLog("-FlushBroadcastReceiver-:"+ACTION);
            if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION)) {
                AndroidLog.androidLog("-FlushBroadcastReceiver-1:"+ACTION);
                onFlushBindCode();
            }

        }
    }
}
