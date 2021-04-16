package swaiotos.channel.iot.tv.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.tv.R;

public class DeviceBindDialogFragment extends DialogFragment {

    private Button mBindBtn,mCanceBindBtn;
    private String mAccessToken,mPushToken,mLsid;

    public static DeviceBindDialogFragment newInstance(@NonNull String pushToken,@NonNull String accessToken,String lsid) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.COOCAA_ACCESSTOKEN, accessToken);
        arguments.putString(Constants.COOCAA_BIND_DEVICE_PUSH_TOKEN, pushToken);
        arguments.putString(Constants.COOCAA_BIND_DEVICE_PUSH_LSID, lsid);
        DeviceBindDialogFragment fragment = new DeviceBindDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_fullscreen_base);
        Bundle arguments = getArguments();
        mAccessToken = arguments.getString(Constants.COOCAA_ACCESSTOKEN);
        mPushToken = arguments.getString(Constants.COOCAA_BIND_DEVICE_PUSH_TOKEN);
        mLsid = arguments.getString(Constants.COOCAA_BIND_DEVICE_PUSH_LSID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bind_device_diglog, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mBindBtn = view.findViewById(R.id.id_bind_device_btn);
        mCanceBindBtn = view.findViewById(R.id.id_cance_bind_device_btn);
        ((TextView)view.findViewById(R.id.id_bind_device_tips)).setText(
                String.format(getContext().getResources().getString(R.string.bind_device_name),mLsid));

        mBindBtn.requestFocus();

        mBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取数据
//                BindDeviceUseCase
//                        .getInstance(getContext().getApplicationContext())
//                        .setAccessToken(mAccessToken)
//                        .setPushToken(mPushToken)
//                        .setBindSubmitCallBackListener(new BindDeviceUseCase.BindDeviceCallBackListener() {
//                            @Override
//                            public void onStart() {
//                                //绑定开始
//                                Toast.makeText(getContext().getApplicationContext(),"start",Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            public void onError(String msg) {
//                                //绑定失败
//                                Toast.makeText(getContext().getApplicationContext(),msg,Toast.LENGTH_LONG).show();
//
////                                //绑定成功
////                                try {
////                                    //发送消息
////                                    IOTAdminChannel.mananger.getSSAdminChannel().getController().connect(mLsid,20000);
////                                } catch (Exception e) {
////                                    e.printStackTrace();
////                                }
//
//                            }
//
//                            @Override
//                            public void onSuccess(CooCaaResponse cooCaaResponse) {
//                                Toast.makeText(getContext(),
//                                        String.format(getContext().getResources().getString(R.string.bind_device_name),mLsid)
//                                                +" sucess! connecting sid...",
//                                        Toast.LENGTH_LONG).show();
//                                //绑定成功
//                                try {
//                                    Intent intent = new Intent("waiotos.channel.iot.tv.qrcodexxx");
//                                    intent.putExtra(Constants.COOCAA_PREF_LSID,mLsid);
//                                    LocalBroadcastManager.getInstance(getContext().getApplicationContext()).sendBroadcast(intent);
////                                    dismiss();
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//                        }).queryBindDeviceResponse();

            }
        });

        mCanceBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    @NonNull
	@Override
	public void show(FragmentManager manager, String tag) {
		Log.d(tag, "show");
		super.show(manager, tag);
	}


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
//            dialog.getWindow().setBackgroundDrawableResource(R.color.colorPopupBack);
        }
        dialog.setCanceledOnTouchOutside(false);
//        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (KeyEvent.ACTION_DOWN == event.getAction()) {
//                    if (KeyEvent.KEYCODE_ENTER == keyCode
//                            || KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
//                        dismiss();
//                    }
//                }
//                return false;
//            }
//        });



        return dialog;
    }
}
