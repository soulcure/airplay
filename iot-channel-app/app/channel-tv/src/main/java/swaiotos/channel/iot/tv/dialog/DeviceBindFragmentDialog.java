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

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.tv.R;

public class DeviceBindFragmentDialog extends DialogFragment {

    private Button mBindBtn,mCanceBindBtn;
    private String mLsid;
    private int mPosition;
    private UnBindCallBackListener unBindCallBackListener;

    public static DeviceBindFragmentDialog newInstance(int position,@NonNull String lsid) {
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.COOCAA_UNBIND_POSITION, position);
        arguments.putString(Constants.COOCAA_BIND_DEVICE_PUSH_LSID, lsid);
        DeviceBindFragmentDialog fragment = new DeviceBindFragmentDialog();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDialog);
        Bundle arguments = getArguments();
        mPosition = arguments.getInt(Constants.COOCAA_UNBIND_POSITION);
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

        final Button cancelBtn = view.findViewById(R.id.id_dialog_btn_cancel);
        final Button sureBtn = view.findViewById(R.id.id_dialog_btn_sure);
        sureBtn.requestFocus();
        sureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unBindCallBackListener !=null)
                    unBindCallBackListener.unBindSure(mLsid,mPosition);
                dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        sureBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    sureBtn.setTextColor(getResources().getColor(R.color.white));
                } else {
                    sureBtn.setTextColor(getResources().getColor(R.color.white_10));
                }
            }
        });

        cancelBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    cancelBtn.setTextColor(getResources().getColor(R.color.white));
                } else {
                    cancelBtn.setTextColor(getResources().getColor(R.color.white_10));
                }
            }
        });

    }

	@Override
	public void show(FragmentManager manager, String tag) {
		Log.d(tag, "show");
		super.show(manager, tag);
	}

    public void setUnBindCallBackListener(UnBindCallBackListener unBindCallBackListener) {
        this.unBindCallBackListener = unBindCallBackListener;
    }

    public interface UnBindCallBackListener {
        void unBindSure(String sid,int position);
    }

}
