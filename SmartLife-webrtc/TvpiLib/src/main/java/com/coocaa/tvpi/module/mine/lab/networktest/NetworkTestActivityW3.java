
package com.coocaa.tvpi.module.mine.lab.networktest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.swaiotos.virtualinput.iot.GlobalIOT;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.module.connection.WifiConnectActivity;
import com.coocaa.tvpi.module.io.HomeIOThread;
import com.coocaa.tvpi.module.io.HomeUIThread;
import com.coocaa.tvpi.module.local.document.DocumentUtil;
import com.coocaa.tvpi.module.local.document.FileDownloader;
import com.coocaa.tvpi.module.local.document.ResultEnum;
import com.coocaa.tvpi.module.local.document.page.DocumentPlayerActivity;
import com.coocaa.tvpi.util.NetworkUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.utils.SpeedTest;

import static com.airbnb.lottie.LottieDrawable.INFINITE;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_BOTH;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_LOCAL;
import static com.coocaa.smartscreen.connect.SSConnectManager.CONNECT_NOTHING;


public class NetworkTestActivityW3 extends BaseActivity {

    private final static String TAG = "NetworkTestActivityW3";
    private final static String TEST_URL = "https://img-sky-fs.skysrt.com/iotpanel/530f99a28cb1a2b16e7120ce864df55f.pptx";
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final static String TXT_TEST_AGAIN = "重新检测";
    private final static String TXT_TEST_ING = "检测中";
    private final static String LOSE_RATE_SUCCESS = "0%";
    private final static String TIP_ERROR = "数据返回失败";
    private View mStartTestBtn;
    private TextView mBtnText, mDeviceName, mDeviceActiveId, mDeviceMac;
    private long mStartTime = 0, linkPhoneTime, startTimePhone;
    private boolean isChecking = false;
    private double mLanSpeed = 0;
    private String mLossRate = "0%";
    private Device mDevice;
    private FileDownloader mDownloader = null;
    private TextView tvNetSpeedTip, tvPingTip, tvPingResult, tvCloudTip, tvCloudResult, tvLocalTip, tvLocalResult;
    private TextView tvPhoneLinkTime;
    private ProgressBar pingProgress, cloudProgress, localProgress, speedProgress, linkProgress;
    private ImageView imgPingResult, imgCloudResult, imgLocalResult;
    private View linkTestLine, speedTestLine;
    private RelativeLayout linkTestLayout, speedTestLayout, speedTestPingLayout, speedTestLocalLayout, speedTestCloudLayout, linkTestResultLayout;
    private CommonTitleBar commonTitleBar;
    private LottieAnimationView netTestTypeAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_test_w3);
        StatusBarHelper.translucent(this);
        DocumentUtil.deleteFile(new File(DocumentUtil.SAVE_DOC_PATH));
        initView();
        initStartTestBtn();
        getPermission(false);
        setDeviceInfo();
    }

    private void initView() {
        mDeviceName = findViewById(R.id.device_name);
        mDeviceActiveId = findViewById(R.id.device_active_id);
        mDeviceMac = findViewById(R.id.device_mac);

        tvCloudResult = findViewById(R.id.tv_net_cloud_result);
        tvCloudTip = findViewById(R.id.tv_net_cloud_tip);
        tvLocalResult = findViewById(R.id.tv_net_local_result);
        tvLocalTip = findViewById(R.id.tv_net_local_tip);
        tvPingResult = findViewById(R.id.tv_net_ping_result);
        tvPingTip = findViewById(R.id.tv_net_ping_tip);
        tvNetSpeedTip = findViewById(R.id.tv_net_speed_tip);
        tvPhoneLinkTime = findViewById(R.id.tv_phone_link_time);

        pingProgress = findViewById(R.id.net_ping_progress);
        cloudProgress = findViewById(R.id.net_cloud_progress);
        localProgress = findViewById(R.id.net_local_progress);
        linkProgress = findViewById(R.id.link_test_progress);
        speedProgress = findViewById(R.id.speed_test_progress);

        linkTestLine = findViewById(R.id.link_test_line);
        speedTestLine = findViewById(R.id.speed_test_line);

        imgPingResult = findViewById(R.id.net_ping_result_img);
        imgCloudResult = findViewById(R.id.net_cloud_img);
        imgLocalResult = findViewById(R.id.net_local_img);

        linkTestLayout = findViewById(R.id.link_speed_layout);
        speedTestLayout = findViewById(R.id.net_speed_layout);
        speedTestCloudLayout = findViewById(R.id.speed_test_cloud_layout);
        speedTestLocalLayout = findViewById(R.id.speed_test_local_layout);
        speedTestPingLayout = findViewById(R.id.speed_test_ping_layout);
        linkTestResultLayout = findViewById(R.id.link_test_result_layout);

        netTestTypeAnim = findViewById(R.id.net_test_type_anim);
        mStartTestBtn = findViewById(R.id.btn_start_test);
        mBtnText = findViewById(R.id.btn_text);
        commonTitleBar = findViewById(R.id.titleBar);
        showTestedState("net_speed_test.json");
    }

    private void initStartTestBtn() {
        mStartTestBtn.setOnClickListener(v -> {
            if (isChecking) {
                ToastUtils.getInstance().showGlobalShort("当前正在检测，请稍后操作");
                return;
            }
            resetView();
            getPermission(true);
        });

        commonTitleBar.setOnClickListener((CommonTitleBar.OnClickListener) position -> {
            if (position == CommonTitleBar.ClickPosition.LEFT) {
                finish();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void setDeviceInfo() {
        try {
            if (SSConnectManager.getInstance().isConnected()) {
                mDevice = SSConnectManager.getInstance().getDevice();
                if (mDevice != null) {
                    if (mDevice.getInfo() != null) {
                        TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) mDevice.getInfo();
                        mDeviceName.setText(SSConnectManager.getInstance().getDeviceName(SSConnectManager.getInstance().getHistoryDevice()));
                        mDeviceActiveId.setText("激活ID:" + tvDeviceInfo.activeId);
                        mDeviceMac.setText("MAC:" + tvDeviceInfo.MAC);
                    } else {
                        mDeviceName.setText("null");
                        mDeviceActiveId.setText("激活ID:");
                        mDeviceMac.setText("MAC:");
                    }
                } else {
                    mDeviceName.setText("null");
                    mDeviceActiveId.setText("激活ID:");
                    mDeviceMac.setText("MAC:");
                }
            } else {
                mDeviceName.setText("未连接");
                mDeviceActiveId.setText("激活ID:");
                mDeviceMac.setText("MAC:");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getPermission(boolean startTest) {
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                Log.i(TAG, "permissionGranted");
                if (startTest) {
                    DocumentUtil.deleteFile(new File(DocumentUtil.SAVE_DOC_PATH));
                    if (NetworkUtil.isNetworkConnected(NetworkTestActivityW3.this)) {
                        int connectState = SSConnectManager.getInstance().getConnectState();
                        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
                        Log.d(TAG, "pushToTv: connectState" + connectState);
                        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
                        //未连接
                        if(connectState == CONNECT_NOTHING || deviceInfo == null){
                            ConnectDialogActivity.start(NetworkTestActivityW3.this);
                            return;
                        }
                        //本地连接不通
                        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
                            WifiConnectActivity.start(NetworkTestActivityW3.this);
                            return;
                        }

                        if (TXT_TEST_AGAIN.equals(mBtnText.getText().toString().trim())) {
                            //主动发个命令退出投屏
                            GlobalIOT.iot.sendKeyEvent(KeyEvent.KEYCODE_HOME, KeyEvent.ACTION_DOWN);
                        }
                        setDeviceInfo();
                        if (mDevice == null) {
                            ConnectDialogActivity.start(NetworkTestActivityW3.this);
                            return;
                        }
                        if (mDevice.getLsid() == null) {
                            ConnectDialogActivity.start(NetworkTestActivityW3.this);
                            return;
                        }
                        showTestingState("net_speed_test.json");
                        linkPhoneTest();
                    } else {
                        ToastUtils.getInstance().showGlobalShort("请先连接网络");
                    }
                }
            }

            @Override
            public void permissionDenied(String[] permission) {
                Log.i(TAG, "permissionDenied: ");

            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void linkPhoneTest() {
        //imgTestType.setImageResource(R.drawable.icon_speed_testing_type);
        refreshBtnText(TXT_TEST_ING);
        HomeUIThread.execute(10000, timeOutRunnable);
        linkTestLayout.setVisibility(View.VISIBLE);
        startTimePhone = System.currentTimeMillis();
        SSConnectManager.getInstance().sendMessageLocalTest(mDevice.getLsid(), new IConnectResult() {
            @Override
            public void onProgress(String lsid, int code, String info) {
                Log.d(TAG, "onProgress: " + code + " info " + info);
                if (code == 1) {
                    startTimePhone = System.currentTimeMillis();
                }
                if (code == 0) {
                    linkPhoneTime = System.currentTimeMillis() - startTimePhone;
                    refreshLinkTestUI();
                }
            }

            @Override
            public void onFail(String lsid, int code, String info) {
                Log.d(TAG, "onFail: " + info);
                linkPhoneTime = -1;
                refreshLinkTestUI();
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        });
    }


    @SuppressLint("SetTextI18n")
    private void pingIp() {
        speedTestLayout.setVisibility(View.VISIBLE);
        String ip = SSConnectManager.getInstance().getConnectSession().getExtra(SSChannel.STREAM_LOCAL);
        tvNetSpeedTip.setVisibility(View.VISIBLE);
        pingProgress.setVisibility(View.VISIBLE);
        tvPingTip.setVisibility(View.VISIBLE);
        tvPingResult.setVisibility(View.VISIBLE);
        HomeIOThread.execute(() -> {
            try {
                Process process = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ip);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
                StringBuilder sb = new StringBuilder();
                String line;
                if ((line = reader.readLine()) != null) {
                    sb.append(line);
                    while ((line = reader.readLine()) != null) {
                        sb.append(LINE_SEP).append(line);
                    }
                }
                String msg = sb.toString();
                Log.d(TAG, "ping ip---" + msg);
                String[] splits = msg.split(" ");
                String time = "";
                boolean pingSuccess = false;
                for (String item : splits) {
                    if (item.contains("ttl")) {
                        pingSuccess = true;
                    }
                    if (item.contains("time=")) {
                        time = item;
                        break;
                    }
                }
                if (!pingSuccess) {
                    time = "";
                }
                String finalTime = time;
                refreshPingUI(finalTime, ip);
            } catch (IOException e) {
                e.printStackTrace();
                refreshPingUI("", ip);
            }
        });
    }

    private void startCloudTest() {
        speedTestCloudLayout.setVisibility(View.VISIBLE);
        //imgTestType.setImageResource(R.drawable.icon_speed_test_type);
        cloudProgress.setVisibility(View.VISIBLE);
        tvCloudTip.setVisibility(View.VISIBLE);
        tvCloudResult.setVisibility(View.VISIBLE);
        Log.i(TAG, "startTest download: " + TEST_URL);
        isChecking = true;
        mStartTime = System.currentTimeMillis();

        if (mDownloader != null) {
            mDownloader.cancel();
        } else {
            mDownloader = new FileDownloader();
        }
        speedTestCloudLayout.setVisibility(View.VISIBLE);
        refreshBtnText(TXT_TEST_ING);
        mStartTime = System.currentTimeMillis();
        mDownloader.download(TEST_URL, DocumentUtil.getFileNameFromPath(TEST_URL), new FileDownloader.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String saveDir) {
                Log.i(TAG, "startTest onDownloadSuccess: " + saveDir);
                long endTime = System.currentTimeMillis();
                File file = new File(saveDir);
                String fileSize = Formatter.formatFileSize(NetworkTestActivityW3.this, file.length());
                int sec = (int) (endTime - mStartTime) / 1000;
                double speed = DocumentUtil.matchNumber(fileSize) / sec;
                String unit = DocumentUtil.matchNonNumber(fileSize);
                @SuppressLint("DefaultLocale") String tip = String.format("%.2f", speed) + "" + unit + "/s," + "耗时" + sec + "s";
                refreshCloudUI(tip, true);
            }

            @Override
            public void onDownloading(int progress) {
                Log.d(TAG, "onDownloading: " + progress);
            }

            @Override
            public void onDownloadFailed(String url, ResultEnum result) {
                Log.i(TAG, "startTest onDownloadFailed: " + result.getMsg());
                refreshCloudUI(TIP_ERROR, false);
                refreshBtnText(TXT_TEST_AGAIN);
            }
        });
    }

    private void startTestDongle() {
        Log.i(TAG, "startTestDongle: ");
        speedTestLocalLayout.setVisibility(View.VISIBLE);
        tvLocalTip.setVisibility(View.VISIBLE);
        tvLocalResult.setVisibility(View.VISIBLE);
        localProgress.setVisibility(View.VISIBLE);
        mStartTime = System.currentTimeMillis();
        String ip = SSConnectManager.getInstance().getConnectSession().getExtra(SSChannel.STREAM_LOCAL);
        mStartTime = System.currentTimeMillis();
        SpeedTest speedTest = new SpeedTest(ip, 20480, new SpeedTest.CallBack() {
            @Override
            public void curSpeed(float speed, String unit) {
                Log.d(TAG, "curSpeed: ");
            }

            @Override
            public void onFinished(float speed, String unit) {
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "onFinished: ");
                if (speed != 0 && LOSE_RATE_SUCCESS.equals(mLossRate)) {
                    int sec = (int) (endTime - mStartTime) / 1000;
                    String tip = speed + "MB/s," + "耗时" + sec + "s";
                    mLanSpeed = speed;
                    refreshLocalUI(tip, true);
                } else {
                    refreshLocalUI(TIP_ERROR, false);
                }
            }

            @Override
            public void onProgress(float rate, String unit) {

            }

            @Override
            public void lossRate(String rate) {
                Log.d(TAG, "lossRate: " + rate);
                if (!LOSE_RATE_SUCCESS.equals(rate)) {
                    refreshLocalUI(TIP_ERROR, false);
                }
                mLossRate = rate;
            }
        });
        if (!NetworkUtil.isNetworkConnected(NetworkTestActivityW3.this)) {
            ToastUtils.getInstance().showGlobalShort("请先连接网络");
            refreshLocalUI(TIP_ERROR, false);
            return;
        }

        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);
        //未连接
        if(connectState == CONNECT_NOTHING || deviceInfo == null){
            ConnectDialogActivity.start(NetworkTestActivityW3.this);
            refreshLocalUI(TIP_ERROR, false);
            return;
        }
        //本地连接不通
        if(!(connectState == CONNECT_LOCAL || connectState == CONNECT_BOTH)){
            WifiConnectActivity.start(NetworkTestActivityW3.this);
            refreshLocalUI(TIP_ERROR, false);

            return;
        }


        try {
            speedTest.open();
        } catch (Exception e) {
            refreshLocalUI(TIP_ERROR, false);
        }

    }

    private void refreshLinkTestUI() {
        Log.d(TAG, "refreshLinkTestUI: ");
        HomeUIThread.removeTask(timeOutRunnable);
        HomeUIThread.execute(new Runnable() {
            @Override
            public void run() {
                linkTestResultLayout.setVisibility(View.VISIBLE);
                linkTestLine.setVisibility(View.VISIBLE);
                linkProgress.setVisibility(View.INVISIBLE);

                if (linkPhoneTime != -1) {
                    tvPhoneLinkTime.setText(String.format("%dms", linkPhoneTime));
                    tvPhoneLinkTime.setTextColor(getResources().getColor(R.color.color_FF5ACC69));
                } else {
                    tvPhoneLinkTime.setText("超时");
                    tvPhoneLinkTime.setTextColor(getResources().getColor(R.color.colorText_FF5525));
                }
                pingIp();
            }
        });

    }

    private void refreshPingUI(String finalTime, String ip) {
        HomeUIThread.execute(() -> {
            speedTestPingLayout.setVisibility(View.VISIBLE);
            speedProgress.setVisibility(View.INVISIBLE);
            speedTestLine.setVisibility(View.VISIBLE);
            imgPingResult.setVisibility(View.VISIBLE);
            pingProgress.setVisibility(View.INVISIBLE);
            tvPingTip.setText("建立连接 (" + ip+")");
            if (!TextUtils.isEmpty(finalTime)) {
                tvPingResult.setText(finalTime + "ms");
                imgPingResult.setImageResource(R.drawable.icon_net_speed_test_success_w3);
            } else {
                tvPingResult.setText("数据返回失败");
                imgPingResult.setImageResource(R.drawable.icon_net_speed_test_error_w3);
            }
            startCloudTest();
        });
    }


    public void refreshCloudUI(String tip, boolean isSuccess) {
        HomeUIThread.execute(() -> {
            tvCloudResult.setText(tip);
            cloudProgress.setVisibility(View.INVISIBLE);
            imgCloudResult.setVisibility(View.VISIBLE);
            if (isSuccess) {
                imgCloudResult.setImageResource(R.drawable.icon_net_speed_test_success_w3);
            } else {
                imgCloudResult.setImageResource(R.drawable.icon_net_speed_test_error_w3);
            }
            startTestDongle();
        });
    }

    public void refreshLocalUI(String tip, boolean isSuccess) {
        localProgress.setVisibility(View.INVISIBLE);
        if (isSuccess) {
            tvLocalResult.setText(tip);
            imgLocalResult.setVisibility(View.VISIBLE);
            imgLocalResult.setImageResource(R.drawable.icon_net_speed_test_success_w3);
        } else {
            tvLocalResult.setText(tip);
            imgLocalResult.setVisibility(View.VISIBLE);
            imgLocalResult.setImageResource(R.drawable.icon_net_speed_test_error_w3);
        }
        refreshBtnText(TXT_TEST_AGAIN);
    }

    private void refreshBtnText(String str) {
        runOnUiThread(() -> {
            mBtnText.setText(str);
            if (TXT_TEST_ING.equals(str)) {
                isChecking = true;
                mStartTestBtn.getBackground().setAlpha(125);
            }
            if (TXT_TEST_AGAIN.equals(str)) {
                isChecking = false;
                mStartTestBtn.getBackground().setAlpha(255);
                //imgTestType.setImageResource(R.drawable.icon_speed_test_type);
                showTestedState("net_speed_test.json");
            }
            if (mLanSpeed > 0 && mLanSpeed < 3 && !isChecking) {
                String tip = tvLocalResult.getText().toString();
                tvLocalResult.setText(tip);
            }
        });
    }

    private void showTestingState(String targetJson) {
        netTestTypeAnim.setVisibility(View.VISIBLE);
        netTestTypeAnim.setAnimation(targetJson);
        netTestTypeAnim.setRepeatCount(INFINITE);
        netTestTypeAnim.playAnimation();
    }

    private void showTestedState(String targetJson) {
        netTestTypeAnim.setVisibility(View.VISIBLE);
        netTestTypeAnim.setAnimation(targetJson);
        netTestTypeAnim.setRepeatCount(INFINITE);
        netTestTypeAnim.setProgress(0.5f);
    }

    private void resetView() {
        tvLocalResult.setText("");
        tvCloudResult.setText("");
        tvPingResult.setText("");
        tvPhoneLinkTime.setText("");
        tvLocalTip.setVisibility(View.INVISIBLE);
        tvCloudTip.setVisibility(View.INVISIBLE);
        tvPingTip.setVisibility(View.INVISIBLE);

        imgLocalResult.setVisibility(View.INVISIBLE);
        imgPingResult.setVisibility(View.INVISIBLE);
        imgCloudResult.setVisibility(View.INVISIBLE);

        localProgress.setVisibility(View.INVISIBLE);
        cloudProgress.setVisibility(View.INVISIBLE);
        pingProgress.setVisibility(View.INVISIBLE);
        linkProgress.setVisibility(View.VISIBLE);
        speedProgress.setVisibility(View.VISIBLE);

        speedTestLocalLayout.setVisibility(View.GONE);
        speedTestCloudLayout.setVisibility(View.GONE);
        speedTestPingLayout.setVisibility(View.GONE);
        speedTestLayout.setVisibility(View.GONE);
        linkTestResultLayout.setVisibility(View.GONE);
        linkTestLayout.setVisibility(View.GONE);

        //imgTestType.setImageResource(R.drawable.icon_link_test_type);
    }

    private Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            linkPhoneTime = -1;
            refreshLinkTestUI();
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDownloader != null) {
            mDownloader.cancel();
            mDownloader = null;
        }
        DocumentUtil.deleteFile(new File(DocumentUtil.SAVE_DOC_PATH));
    }

}
