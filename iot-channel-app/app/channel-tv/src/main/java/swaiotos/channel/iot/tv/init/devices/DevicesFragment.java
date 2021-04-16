package swaiotos.channel.iot.tv.init.devices;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.swaiotos.skymirror.sdk.capture.MirManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.tv.R;
import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.adapter.DevicesAdapter;
import swaiotos.channel.iot.tv.base.DeviceChangeListener;
import swaiotos.channel.iot.tv.base.MvpFragment;
import swaiotos.channel.iot.tv.dialog.DeviceBindFragmentDialog;
import swaiotos.channel.iot.tv.init.InitFragment;
import swaiotos.channel.iot.tv.view.DialogTools;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class DevicesFragment extends MvpFragment<DevicesContract.Presenter> implements DevicesContract.View,
        DevicesAdapter.OnItemClickListener,DeviceBindFragmentDialog.UnBindCallBackListener {

    private final String TAG = DevicesFragment.class.getSimpleName();
    private DevicesContract.Presenter mPresenter;
    private ArrayList<Device> devices = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DevicesAdapter devicesAdapter;

    public static DevicesFragment newInstance() {
        DevicesFragment fragment = new DevicesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = view.findViewById(R.id.devices_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        devicesAdapter = new DevicesAdapter(getContext(), devices);
        mRecyclerView.setAdapter(devicesAdapter);

        devicesAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter = new DevicesPresenter(this);

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
        DevicesContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.init(getActivity().getApplicationContext());
        }
    }

    @Override
    public void setPresenter(DevicesContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return getActivity() != null && !getActivity().isFinishing() &&  isAdded();
    }

    @Override
    protected DevicesContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onUnBindCallBack(String sid) {
        Log.d(TAG,"--------OnUnBindCallBack---2---");
    }

    @Override
    public void onItemClicked(List<Device> mList, int position, View view) {
        if (position < 0)
            return;
        if (MirManager.instance().isMirRunning() || MirManager.instance().isReverseRunning()) {
            Toast.makeText(getActivity(),"正在同屏控制中，无法解绑",Toast.LENGTH_SHORT).show();
            return;
        }
        DeviceBindFragmentDialog deviceBindFragmentDialog = DeviceBindFragmentDialog.newInstance(position,mList.get(position).getLsid());
        deviceBindFragmentDialog.setUnBindCallBackListener(this);
        deviceBindFragmentDialog.show(getFragmentManager(),"BINDEVICE");
    }

    @Override
    public void unBindSure(final String sid, final int position) {
        final Dialog dialog = DialogTools.showDialogByXml(getActivity());
        Log.d(TAG,"----------unBindSuccess--");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    IOTAdminChannel.mananger.getSSAdminChannel().getDeviceAdminManager().unBindDevice(null, sid, 1, new DeviceAdminManager.unBindResultListener() {
                        @Override
                        public void onSuccess(String lsid) {
                            if (isActive())
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //解绑调用disconnect
                                        try {
                                            Session connectedSession = IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().getConnectedSession();
                                            if (connectedSession != null)
                                                IOTAdminChannel.mananger.getSSAdminChannel().getController().disconnect(connectedSession);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        dialog.dismiss();
                                        devicesAdapter.notifyItem(position);
                                        mRecyclerView.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                RecyclerView.ViewHolder  devicesAdapterHolder = mRecyclerView.findViewHolderForAdapterPosition(position);
                                                if (devicesAdapter.getItemCount() > 0 && devicesAdapterHolder != null)
                                                    devicesAdapterHolder.itemView.requestFocus();
                                            }
                                        },200);


                                        List<DeviceChangeListener> listeners = ((TVChannelApplication)getActivity().getApplication()).getListeners();
                                        if (listeners.size() > 0 ) {
                                            for (int i=0; i < listeners.size(); i++) {
                                                listeners.get(i).OnUnBindCallBack(sid);
                                            }
                                        }
                                        if (devices != null && devices.size() <= 0) {
                                            getActivity().finish();
                                        }
                                    }
                                });
                        }

                        @Override
                        public void onFail(String lsid, String errorType, String msg) {
                            if (isActive())
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(),"解绑失败",Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void refreshDevices(final List<Device> devices) {
        this.devices = (ArrayList<Device>) devices;
        if (isActive()) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @SuppressLint("StringFormatMatches")
                @Override
                public void run() {
                    if (devices != null) {
                        devicesAdapter.setList(DevicesFragment.this.devices);
                        devicesAdapter.notifyDataSetChanged();
                        mRecyclerView.requestFocus();
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
