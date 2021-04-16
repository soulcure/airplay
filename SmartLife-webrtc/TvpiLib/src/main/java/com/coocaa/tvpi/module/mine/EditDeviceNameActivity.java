package com.coocaa.tvpi.module.mine;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.channel.CmdData;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.module.connection.ConnectDialogActivity;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpilib.R;

import static com.coocaa.smartscreen.connect.SSConnectManager.TARGET_APPSTATE;

public class EditDeviceNameActivity extends BaseActivity implements UnVirtualInputable {

    private final static String TAG = EditDeviceNameActivity.class.getSimpleName();

    private TextView cancelBtn, saveBtn;
    private EditText etName;
    private ImageView deleteBtn;

    private SDialog dialog;
    private String currentDeviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_editname);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        initListener();
        //打开软键盘
        etName.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void initView() {
        cancelBtn = findViewById(R.id.cancel_btn);
        saveBtn = findViewById(R.id.save_btn);
        etName = findViewById(R.id.user_et_name);
        deleteBtn = findViewById(R.id.delete_btn);
        currentDeviceName = SSConnectManager.getInstance().getDeviceName(SSConnectManager.getInstance().getHistoryDevice());
        if (!TextUtils.isEmpty(currentDeviceName)) {
            etName.setText(currentDeviceName);
        }
        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12), spaceFilter});
        etName.addTextChangedListener(new FilterEmojiTextWatcher(this) {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                super.beforeTextChanged(charSequence, start, count, after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                super.afterTextChanged(editable);
            }
        });
    }

    private void initListener() {
        cancelBtn.setOnClickListener(listener);
        saveBtn.setOnClickListener(listener);
        deleteBtn.setOnClickListener(listener);
    }

    private void confirmName() {
        String deviceName = etName.getText().toString();
        Log.d(TAG, "confirmName: " + deviceName);
        if (currentDeviceName.equals(deviceName)) {
            finish();
            return;
        }
        if (TextUtils.isEmpty(deviceName)) {
            showErrorDialog("没有输入名称，请重新填写");
            return;
        }
        updateName(etName.getText().toString());
    }

    private void confirmFinish() {
        String deviceName = etName.getText().toString();
        Log.d(TAG, "confirmName: " + deviceName);
        if (currentDeviceName.equals(deviceName)) {
            finish();
        } else {
            showConfirmDialog();
        }
    }

    private void updateName(String deviceName) {
        int connectState = SSConnectManager.getInstance().getConnectState();
        final ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        Log.d(TAG, "pushToTv: connectState" + connectState);
        Log.d(TAG, "pushToTv: deviceInfo" + deviceInfo);

        //本地连接不通
        if (!SSConnectManager.getInstance().isConnected()) {
            ConnectDialogActivity.start(EditDeviceNameActivity.this);
            return;
        }
        updateDeviceName(deviceName);
        ToastUtils.getInstance().showGlobalShort("保存成功");
        finish();
    }

    public void updateDeviceName(String name) {
        CmdData data = new CmdData("DEVICE_INFO", CmdData.CMD_TYPE.DEVICE_INFO.toString(), name);
        String cmd = data.toJson();
        SSConnectManager.getInstance().sendTextMessage(cmd, TARGET_APPSTATE);
    }


    private void clearName() {
        etName.setText("");
    }

    private void showErrorDialog(String errMsg) {
        dialog = new SDialog(EditDeviceNameActivity.this, errMsg, "我知道了",
                () -> dialog.dismiss());
        dialog.show();
    }

    private void showConfirmDialog() {
        dialog = new SDialog(EditDeviceNameActivity.this, "是否保存修改的名称？", "不保存", "保存", (l, view) -> {
            dialog.dismiss();
            if (!l) {
                confirmName();
            } else {
                finish();
            }
        });
        dialog.show();
    }

    private final View.OnClickListener listener = v -> {
        if (v.getId() == R.id.cancel_btn) {
            confirmFinish();
        } else if (v.getId() == R.id.save_btn) {
            confirmName();
        } else if (v.getId() == R.id.delete_btn) {
            clearName();
        }
    };

    InputFilter spaceFilter = (source, start, end, dest, dstart, dend) -> {
        if (" ".equals(source)) {
            return "";
        } else {
            return null;
        }
    };

}
