package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.connection.adapter.WifiAdapter;
import com.coocaa.tvpi.util.GpsUtil;
import com.coocaa.tvpi.util.WifiUtil;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//https://developer.android.google.cn/guide/topics/connectivity/wifi-scan
public class WifiListActivity extends BaseActivity implements UnVirtualInputable {
    private static final String TAG = WifiListActivity.class.getSimpleName();
    public static final String KEY_START_MODE = "key_start_mode";

    //通过wifi配网切换网络跳转过来
    public static final int START_MODE_START_ACTIVITY_FOR_RESULT_WIFI_SSID = 0;
    //通过配网时未连接wifi的情况下跳转过来
    public static final int START_MODE_NOT_CONNECTED_WIFI = 1;
    @IntDef({START_MODE_START_ACTIVITY_FOR_RESULT_WIFI_SSID, START_MODE_NOT_CONNECTED_WIFI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StartMode {}
    private @StartMode int startMode;

    private RelativeLayout emptyLayout;
    private WifiAdapter wifiAdapter;
    private WifiManager wifiManager;
    private final Map<String, ScanResult> scanResultMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        parseIntent();
        registerReceiver();
        initView();
        openGpsAndLocation();
        //先获取历史wifi
        setScanWifiResult();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        scanResultMap.clear();
    }

    private void openGpsAndLocation() {
        if (GpsUtil.isOpen(WifiListActivity.this)) {
            Log.d(TAG, "gps is open");
            PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
                @Override
                public void permissionGranted(String[] permission) {
                    Log.d(TAG, "permissionGranted: ACCESS_FINE_LOCATION");
                    scanWifi();
                }

                @Override
                public void permissionDenied(String[] permission) {
                    Log.d(TAG, "permissionDenied: ACCESS_FINE_LOCATION");
                    ToastUtils.getInstance().showGlobalShort("需开启访问位置信息，才能连接酷开共享屏");
                    finish();
                }
            }, Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            ToastUtils.getInstance().showGlobalShort("需开启定位服务，才能连接酷开共享屏");
            finish();
        }
    }


    private void parseIntent(){
        if(getIntent() != null){
            startMode = getIntent().getIntExtra(KEY_START_MODE,START_MODE_NOT_CONNECTED_WIFI);
        }
    }

    private void initView() {
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        Button btConnectByHotspot = findViewById(R.id.btConnectByHotspot);
        TextView tvGoSetting = findViewById(R.id.tvGoSetting);
        emptyLayout = findViewById(R.id.emptyLayout);
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        wifiAdapter = new WifiAdapter();
        recyclerView.setAdapter(wifiAdapter);
        WifiAdapter.WifiListDivider divider = new WifiAdapter.WifiListDivider(this);
        recyclerView.addItemDecoration(divider);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        wifiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ScanResult scanResult = wifiAdapter.getData().get(position);
                String ssid = scanResult.SSID;
                boolean hasPassword = WifiUtil.isWifiHasPassword(scanResult);
                Log.d(TAG, "onItemClick: ssid:" + ssid + "\nhasPassword:" + hasPassword + "\ncapabilities:" + scanResult.capabilities);
                if (startMode == START_MODE_START_ACTIVITY_FOR_RESULT_WIFI_SSID) {
                    Intent intent = new Intent();
                    intent.putExtra("wifi", ssid);
                    intent.putExtra("hasPassword", hasPassword);
                    setResult(Activity.RESULT_OK, intent);
                } else if (startMode == START_MODE_NOT_CONNECTED_WIFI){
                    ConnectNetForDongleActivity.startUseWifi(WifiListActivity.this, ssid, hasPassword);
                }
                finish();
            }
        });

        btConnectByHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenHotspotActivity.Companion.starter(WifiListActivity.this);
            }
        });

        tvGoSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(wifiSettingsIntent);
                finish();
            }
        });
    }

    private void scanWifi() {
        Log.d(TAG, "scanWifi");
        if (wifiManager == null) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
//部分手机关闭wifi开关的情况下需调用setWifiEnabled(true)才能获取wifi列表
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
        wifiManager.startScan();
    }

    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "onReceive: " + intent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    Log.d(TAG, "onReceive: EXTRA_RESULTS_UPDATED " + success);
                }
                setScanWifiResult();
            }
        }
    };

    private void setScanWifiResult() {
        List<ScanResult> results = wifiManager.getScanResults();
        Log.d(TAG, "setScanWifiResult: " + results);
        if(results != null && !results.isEmpty()) {
            for (ScanResult result : results) {
                if (!TextUtils.isEmpty(result.SSID) && !scanResultMap.containsKey(result.SSID)) {
                    scanResultMap.put(result.SSID, result);
                }
            }
            List<ScanResult> scanResultList = new ArrayList<>(scanResultMap.values());
            Collections.sort(scanResultList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    return o2.level - o1.level;
                }
            });
            wifiAdapter.setList(scanResultList);
            emptyLayout.setVisibility(View.GONE);
        }else {
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(wifiScanReceiver);
    }
}
