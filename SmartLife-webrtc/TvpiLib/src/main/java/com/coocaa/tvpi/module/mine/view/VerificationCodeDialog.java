package com.coocaa.tvpi.module.mine.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.network.NetWorkManager;
import com.coocaa.smartscreen.network.ObserverAdapter;
import com.coocaa.smartscreen.network.util.MD5Util;
import com.coocaa.smartscreen.repository.utils.IOTServerUtil;
import com.coocaa.tvpi.module.login.UserInfoCenter;
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
public class VerificationCodeDialog extends DialogFragment {

    private static final String TAG = VerificationCodeDialog.class.getSimpleName();
    private VerificationCodeView verifyCodeView;
    private TextView tvSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_verify_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.color.transparent);
            getDialog().getWindow().setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE);
        }
        Button btClose = view.findViewById(R.id.bt_close);
        tvSubtitle = view.findViewById(R.id.subtitle);
        verifyCodeView = view.findViewById(R.id.verify_view);
        verifyCodeView.setOnCodeFinishListener(new VerificationCodeView.OnCodeFinishListener() {
            @Override
            public void onTextChange(View view, String content) {
                if(content != null && content.length()>0 && content.length()<verifyCodeView.getmEtNumber()) {
                    tvSubtitle.setText("");
                }
            }

            @Override
            public void onComplete(View view, String content) {
                verifyCode(content);
            }
        });

        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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
        if("0000".equals(password)) {
            setVerifyPass();
            return ;
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
                            if (VerificationCodeDialog.this != null) {
                                JSONObject object = new JSONObject(response);
                                boolean result = object.getBoolean("data");
                                if (result) {
                                    setVerifyPass();
                                } else {
                                    setVerifyError("密码错误[1000]");
                                }
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            if (VerificationCodeDialog.this != null) {
                                setVerifyError("密码错误[1002]");
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        if (VerificationCodeDialog.this != null) {
                            setVerifyError("密码错误[1004]");
                        }
                    }
                });
    }

    public void setVerifyPass() {
        dismiss();
        verifyCodeView.setEmpty();
        if (verifyCodeListener != null) {
            verifyCodeListener.onVerifyPass();
        }
    }

    public void setVerifyError(String tip) {
        verifyCodeView.setEmpty();
        tvSubtitle.setText(tip);
        tvSubtitle.setTextColor(Color.parseColor("#FF5525"));
    }
}
