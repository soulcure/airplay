package swaiotos.channel.iot.tv.init;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import swaiotos.channel.iot.common.push.PushMsg;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.PublicParametersUtils;
import swaiotos.channel.iot.common.utils.StringUtils;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.base.MvpFragment;
import swaiotos.channel.iot.tv.dialog.DeviceBindDialogFragment;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class InitFragment extends MvpFragment<InitContract.Presenter> implements InitContract.View {

    private InitContract.Presenter mPresenter;
    private ImageView mImageView;
    private TextView mQrCodeTextView,mTips;
    private DeviceBindDialogFragment deviceBindDialogFragment;

    public static InitFragment newInstance(String param1, String param2) {
        InitFragment fragment = new InitFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_init, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.init_qrcode_imageview);
        mQrCodeTextView = view.findViewById(R.id.init_qrcode_textview);
        mTips = view.findViewById(R.id.init_open_tips);
        ((Button)view.findViewById(R.id.tv_pad)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String str = "{\"from\":1,\"iot_chanel\":{\"cmd\":\"IOT_CHANNEL_LSID_REGISTER\",\"data\":{\"activeId\":\"3666512\",\"lsid\":\"e8eeaa28bd2d49008a62f76e2f44fc62\",\"nickname\":\"wesly\",\"pushToken\":\"76d5047b48e24de592466f320e25f013\"}},\"msgType\":\"\"}\n";
                String str = "{\"from\":1,\"iot_chanel\":{\"cmd\":\"IOT_CHANNEL_LSID_REGISTER\",\"data\":{\"activeId\":\"3666512\",\"lsid\":\"6a3c8d6b381a410abb346634c2399be6\",\"nickname\":\"wesly\",\"pushToken\":\"1267626857744f6ba6e7ed4e0dba63cc\"}},\"msgType\":\"\"}";
                String accessTokenPref = ShareUtls.getInstance(getContext().getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                PushMsg push = JSON.parseObject(str, PushMsg.class);
                if (push != null && push.getIot_chanel() != null
                        && push.getIot_chanel().getData() != null
                        && push.getIot_chanel().getData().getPushToken() != null
                        && push.getIot_chanel().getData().getLsid() != null
                        && !StringUtils.isEmpty(accessTokenPref)) {
                    deviceBindDialogFragment =DeviceBindDialogFragment.newInstance(push.getIot_chanel().getData().getPushToken(),
                            accessTokenPref,push.getIot_chanel().getData().getLsid());
                    deviceBindDialogFragment.show(getFragmentManager(),"BINDEVICE");
                }
            }
        });

    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter = new InitPresenter(this);

        View decorView = getActivity().getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    return;
                }
                initData();
            }
        });

    }

    private void initData() {
        InitContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.init(getActivity().getApplicationContext());
        }
    }

    @Override
    public void setPresenter(InitContract.Presenter presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public boolean isActive() {
        return getActivity() != null && !getActivity().isFinishing() &&  isAdded();
    }

    @Override
    protected InitContract.Presenter getPresenter() {
        return mPresenter;
    }


    @Override
    public void refushOrUpdateQRCode(String qrcodeInfo, String qrcodeExpire) {

        if (StringUtils.isEmpty(qrcodeInfo)) return;

//        Bitmap mBitmap = CodeUtils.createImage(PublicParametersUtils.getURLAndBindCode(qrcodeInfo,getContext()),400, 400, null);
        Bitmap mBitmap = CodeUtils.createImage(qrcodeInfo,400, 400, null);
        mImageView.setImageBitmap(mBitmap);
        mQrCodeTextView.setText(String.format(getContext().getResources().getString(R.string.bind_sid_name),qrcodeInfo));
        mQrCodeTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showBindView(String pushMsg) {
        Log.d("PushMsgIntentService","pushMsg:"+pushMsg);
        //11-12:绑定信息+添加设备管理接口
        if (isActive()) {
            if (StringUtils.isEmpty(pushMsg)) {
                Toast.makeText(getContext().getApplicationContext(),"push msg is null",Toast.LENGTH_LONG).show();
                return;
            }
            String accessTokenPref = ShareUtls.getInstance(getContext().getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
            PushMsg push = JSON.parseObject(pushMsg,PushMsg.class);
            if (push != null && push.getIot_chanel() != null
                    && push.getIot_chanel().getData() != null
                    && push.getIot_chanel().getData().getPushToken() != null
                    && push.getIot_chanel().getData().getLsid() != null
                    && !StringUtils.isEmpty(accessTokenPref)) {
                deviceBindDialogFragment =DeviceBindDialogFragment.newInstance(push.getIot_chanel().getData().getPushToken(),
                        accessTokenPref,push.getIot_chanel().getData().getLsid());

                deviceBindDialogFragment.show(getFragmentManager(),"BINDEVICE");
            }


        }

    }

    @Override
    public void hideBindDialog() {
        if (isActive()){
             if(deviceBindDialogFragment != null) {
                 deviceBindDialogFragment.dismiss();
             }
            mImageView.setVisibility(View.INVISIBLE);
//            mQrCodeTextView.setVisibility(View.INVISIBLE);
            mQrCodeTextView.setText("设备与服务绑定成功");
            mTips.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void refreshTips(final String msg,final boolean success) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!success) {
                    mImageView.setVisibility(View.GONE);
                    mQrCodeTextView.setVisibility(View.GONE);
                    mTips.setText(msg);
                } else {
                    mTips.setText(msg);
                }
            }
        });
    }
}
