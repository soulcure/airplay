package swaiotos.channel.iot.tv.pad;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.base.MvpFragment;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class PadInitFragment extends MvpFragment<PadInitContract.Presenter> implements PadInitContract.View {
    private static final String TAG = PadInitFragment.class.getSimpleName();
    private PadInitContract.Presenter mPresenter;
    private Button mCheckBtn,mUnBindBtn,mDeviceBtn,mJoinBtn,mLeaveBtn,mListerBtn;
    private EditText mEditText;
    private TextView mTextView,mDevicesText;
    private AtomicBoolean mAtomicBoolean = new AtomicBoolean(true);
    private List<Device> mDevices;

    public static PadInitFragment newInstance(String param1, String param2) {
        PadInitFragment fragment = new PadInitFragment();
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

        return inflater.inflate(R.layout.fragment_init_pad, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = view.findViewById(R.id.check_bt_editText);
        mEditText.setRawInputType(Configuration.KEYBOARD_QWERTY);
        mCheckBtn = view.findViewById(R.id.check_btn);
        mTextView = view.findViewById(R.id.check_text);
        mDevicesText = view.findViewById(R.id.devices_text);
        mUnBindBtn = view.findViewById(R.id.un_bind_btn);
        mDeviceBtn = view.findViewById(R.id.devices_btn);
        mJoinBtn = view.findViewById(R.id.join_btn);
        mLeaveBtn = view.findViewById(R.id.leave_btn);
        mListerBtn = view.findViewById(R.id.lister_btn);

        mCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("绑定中...");
                Toast.makeText(getContext(),"on binding",Toast.LENGTH_LONG).show();
                if (!mAtomicBoolean.get()) {
                    return;
                }
                mAtomicBoolean.compareAndSet(true,false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String accessToken = ShareUtls.getInstance(getContext().getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                            IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().startBind(accessToken,mEditText.getText().toString(), new DeviceAdminManager.OnBindResultListener() {
                                @Override
                                public void onSuccess(String bindCode, Device device) {
                                    if ( getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText("TV端绑定成功，并通知绑定端成功");
                                                Toast.makeText(getContext(),"绑定成功!" ,Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    mAtomicBoolean.compareAndSet(false,true);
                                    Log.d(TAG,"msg----:"+bindCode);
                                }

                                @Override
                                public void onFail(String bindCode, final String errorType, final String msg) {
                                    if ( getActivity() != null) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTextView.setText("绑定端失败:"+msg);
                                                Toast.makeText(getContext(),"onfail",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                    mAtomicBoolean.compareAndSet(false,true);
                                    Log.d(TAG,"bindCode----:"+bindCode);
                                }
                            },50000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        mUnBindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDevices == null || mDevices.size() <= 0) {
                    Toast.makeText(getContext(),"请获取设备列表或 设备绑定列表为空",Toast.LENGTH_LONG).show();
                    return;
                }
                mTextView.setText("");
                mDevicesText.setText("");
                String accessToken = ShareUtls.getInstance(getContext().getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                getPresenter().unBind(accessToken,mDevices.get(0).getLsid(),1);
            }
        });

        mDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("");
                mDevices = null;
                mDevicesText.setText("");
                getPresenter().queryDevices();
            }
        });

        mJoinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadManager.getInstance().ioThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Session session = IOTAdminChannel.mananger.getSSAdminChannel().getController().connect(mDevices.get(0).getLsid(),20000);
                            session.encode();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        });

        mLeaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    IOTAdminChannel.mananger.getSSAdminChannel().getController().disconnect(IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().getConnectedSession());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mListerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new PadInitPresenter(this);

        View decorView = getActivity().getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    return;
                }
                Log.d(TAG,"init data");
                initData();
            }
        });

    }
    private void initData() {

        PadInitContract.Presenter presenter = getPresenter();
        if (presenter != null && getActivity() != null) {
            presenter.init(getActivity().getApplicationContext());
        }
    }

    @Override
    public void setPresenter(PadInitContract.Presenter presenter) {
        this.mPresenter = presenter;
    }


    @Override
    public boolean isActive() {
        return getActivity() != null && !getActivity().isFinishing() &&  isAdded();
    }

    @Override
    protected PadInitContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(getActivity().getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBindStartShow(@NonNull String msg) {
        if (isActive())
            mTextView.setText(msg);
    }

    @Override
    public void onBindEndShow(String pushToken) {
        if (isActive())
            mTextView.setText(pushToken + " query success!");
    }

    @Override
    public void refrushTips(final String msg,final boolean success) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!success) {
                    mEditText.setVisibility(View.GONE);
                    mCheckBtn.setVisibility(View.GONE);
                }
                mTextView.setText(msg);
            }
        });
    }

    @Override
    public void showDevices(final List<Device> devices) {

        this.mDevices = devices;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String devicesLsids = "";
                for (int i = 0; i < devices.size(); i++) {
                    devicesLsids += devices.get(i).getLsid()+"  ";
                }
                if (TextUtils.isEmpty(devicesLsids)) {
                    mDevicesText.setText("设备列表为空");

                } else {
                    mDevicesText.setText(devicesLsids);
                }
            }
        });
    }

}
