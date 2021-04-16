package com.coocaa.tvpi.module.upgrade;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.upgrade.UpgradeData;
import com.coocaa.tvpi.util.FastClick;
import com.coocaa.tvpilib.R;

/**
 * @Author: yuzhan
 */
public class CallUpgradeActivity extends BaseActivity {

    private Button btnCancel;
    private Button btnConfirm;
    private FastClick fastClick = new FastClick();

    @Override
    protected void onResume() {
        super.onResume();
//        initUpgradeData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_call_upgrade);
        initView();
        initListener();
        //finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void initView() {
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void initListener() {
        btnCancel.setOnClickListener(v -> {
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            if (!fastClick.isFaskClick()) {
                initUpgradeData();
                finish();
            }
        });
    }

    private void initUpgradeData() {
        UpgradeManager.getInstance().upgradeLatest(new UpgradeManager.UpgradeCallback() {
            @Override
            public void onSuccess(UpgradeData upgradeData) {
                if (null != upgradeData) {
                    Log.d(TAG, "onSuccess: "+upgradeData.version_code);
                    long versionCode = UpgradeManager.getInstance().getAppVersionCode(CallUpgradeActivity.this);
                    if (versionCode >= upgradeData.version_code) {
                        ToastUtils.getInstance().showGlobalShort("已是最新版本");
                    } else {
                        UpgradeManager.getInstance().downloadLatestAPK();
                    }
                }
            }

            @Override
            public void onFailed(Throwable e) {
                ToastUtils.getInstance().showGlobalShort("未检测到新版本");
            }
        });
    }
}
