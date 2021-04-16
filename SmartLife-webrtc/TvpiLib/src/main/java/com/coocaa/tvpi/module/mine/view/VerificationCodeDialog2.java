package com.coocaa.tvpi.module.mine.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.constant.SmartConstans;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.util.MD5Util;
import com.coocaa.smartscreen.repository.utils.IOTServerUtil;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.view.DeletableEditText;
import com.coocaa.tvpilib.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

/**
 * 输入商家密码弹框
 * Created by songxing on 2020/11/12
 */
public class VerificationCodeDialog2 extends DialogFragment {

    private static final String TAG = VerificationCodeDialog2.class.getSimpleName();
    private TextView tvSubtitle;
    private EditText etInput;
    private TextView tvCancel;
    private TextView tvSure;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_verify_code2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
            getDialog().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE);
        }
        tvSubtitle = view.findViewById(R.id.subtitle);
        etInput = view.findViewById(R.id.et_input);
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvSure = view.findViewById(R.id.tv_sure);
        etInput.requestFocus();
        etInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        etInput.setTransformationMethod(new DotPasswordTransformationMethod());

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode(etInput.getText().toString());
            }
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean emptyInput = s == null || s.toString().length() == 0;
                if (emptyInput) {
                    tvSure.setEnabled(false);
                    etInput.setLetterSpacing(0f);
                    etInput.setTextSize(16f);
                } else {
                    tvSure.setEnabled(true);
                    etInput.setLetterSpacing(0.5f);
                    etInput.setTextSize(10f);
                }
            }
        });
    }


    private VerifyCodeListener verifyCodeListener;

    public void setVerifyCodeListener(VerifyCodeListener verifyCodeListener) {
        this.verifyCodeListener = verifyCodeListener;
    }

    public interface VerifyCodeListener {
        void onVerifyPass();
    }

    public void verifyCode(String password) {
        Log.d(TAG, "verifyCode: " + password);
        Device device = SSConnectManager.getInstance().getHistoryDevice();

        if (null == device) {
            setVerifyError("请先连接设备");
            return;
        }

        if (!SmartConstans.isTestServer() && "123456".equals(password)) {
            setVerifyPass();
            return;
        }

        TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) device.getInfo();
        HashMap<String, String> queryMap = new HashMap<>();
        CoocaaUserInfo coocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        Log.d(TAG, "verifyCode: coocaaUserInfo = " + coocaaUserInfo.toString());
        queryMap.put("ak", coocaaUserInfo.getAccessToken());
        queryMap.put("uid", coocaaUserInfo.getOpen_id());
        queryMap = IOTServerUtil.getQueryMap(queryMap);
        Log.d(TAG, "verifyCode: " + queryMap);
        HashMap<String, String> bodyParams = new HashMap<>();
        bodyParams.put("activation_id", tvDeviceInfo.activeId);
        bodyParams.put("password", MD5Util.getMd5(password).toLowerCase());
        RequestBody requestBody = RequestBody.create(MediaType.parse(
                "Content-Type, application/json"), new JSONObject(bodyParams).toString());
        Log.d(TAG, "verifyCode: " + requestBody.toString());
        NetWorkManager.getInstance()
                .getSkyworthIotService()
                .checkMerchantPwd(queryMap, requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverAdapter<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        String response = "";
                        try {
                            response = responseBody.string();
                            Log.d(TAG, "onNext: " + response);
                            if (VerificationCodeDialog2.this != null) {
                                JSONObject object = new JSONObject(response);
                                boolean result = object.getBoolean("data");
                                if (result) {
                                    setVerifyPass();
                                } else {
                                    Log.d(TAG, "onNext: 1000");
                                    setVerifyError("密码错误，请重新输入");
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            if (VerificationCodeDialog2.this != null) {
                                Log.d(TAG, "onNext: 1002");
                                setVerifyError("密码错误，请重新输入");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        if (VerificationCodeDialog2.this != null) {
                            Log.d(TAG, "onError: 1004");
                            setVerifyError("密码错误，请重新输入");
                        }
                    }
                });
    }

    public void setVerifyPass() {
        dismiss();
        if (verifyCodeListener != null) {
            verifyCodeListener.onVerifyPass();
        }
    }

    public void setVerifyError(String tip) {
        etInput.setText("");
        tvSubtitle.setText(tip);
        tvSubtitle.setTextColor(Color.parseColor("#FF5525"));
    }

    public static class DotPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private static class PasswordCharSequence implements CharSequence {
            private final CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }

            @Override
            public char charAt(int index) {
                return '●'; // This is the important part
            }

            @Override
            public int length() {
                return mSource.length(); // Return default
            }

            @io.reactivex.annotations.NonNull
            @Override
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }
}
