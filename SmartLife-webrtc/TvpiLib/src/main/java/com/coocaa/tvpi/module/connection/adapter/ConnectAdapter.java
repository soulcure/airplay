package com.coocaa.tvpi.module.connection.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.utils.DeviceListManager;
import com.coocaa.tvpilib.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;

/**
 * @ClassName DeviceAdapter
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/18
 * @Version TODO (write something)
 */
public class ConnectAdapter extends RecyclerView.Adapter {

    private static final String TAG = ConnectAdapter.class.getSimpleName();

    private List<Device> dataList;
    private int connectingPosition = -1;

    public ConnectAdapter() {
    }

    public void setList(List<Device> dataList) {
        List<Device> soutedList = DeviceListManager.getInstance().getSortedDevices(dataList);
        if (null != soutedList && soutedList.size() > 0) {
            this.dataList = soutedList;
        } else {
            this.dataList = dataList;
        }
        notifyDataSetChanged();
    }

    public void updateDevice(Device device) {
        Log.d(TAG, "updateDevice: ");
        if (null == device) {
            return;
        }
        if(dataList != null) {
            for (int i = 0; i < dataList.size(); i++) {
                if (dataList.get(i) instanceof Device) {
                    Device item = (Device) dataList.get(i);
                    if (item.getLsid().equals(device.getLsid())) {
                        Log.d(TAG, "updateDevice: " + device.getStatus());
                        item.setStatus(device.getStatus());
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void showConnecting(int connectingPosition) {
        this.connectingPosition = connectingPosition;
        notifyDataSetChanged();
    }

    public void removeItem(String lsid) {
        Log.d(TAG, "removeItem: " + lsid);
        for (int i = 0; i < dataList.size(); i++) {
            Object item = dataList.get(i);
            if (item instanceof Device && ((Device) item).getLsid().equals(lsid)) {
                DeviceInfo deviceInfo = ((Device) item).getInfo();
                TVDeviceInfo tvDeviceInfo = null;
                if (null != deviceInfo) {
                    switch (deviceInfo.type()) {
                        case TV:
                            tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                            break;
                    }
                }
                Log.d(TAG, "removeItem: " + i + "  info:" + tvDeviceInfo.mModel);
                ToastUtils.getInstance().showGlobalShort(tvDeviceInfo.mNickName + "已解绑");
                dataList.remove(i);
                notifyDataSetChanged();
                return;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.connect_holder_layout, parent, false);
        holder = new ConnectHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConnectHolder connectHolder = (ConnectHolder) holder;
        connectHolder.onBind((Device) dataList.get(position), position == connectingPosition);

        int count = getItemCount();
        if (count == 1) {
            connectHolder.setItemBackground(R.drawable.bg_white_round_12);
        } else if (position == 0) {
            connectHolder.setItemBackground(R.drawable.bg_white_top_round_12);
        } else if (position == count -1) {
            connectHolder.setItemBackground(R.drawable.bg_white_bottom_round_12);
        } else {
            connectHolder.setItemBackground(R.color.white);
        }
        connectHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position, dataList.get(position));
                }
            }
        });
        connectHolder.setOnDisconnectClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    Log.d(TAG, "onClick: position = " + position + "    size = " + dataList.size());
                    mOnItemClickListener.onDisconnectClick(dataList.get(position));
                }
            }
        });
        connectHolder.setOnUnbindClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    Log.d(TAG, "onClick: position = " + position + "    size = " + dataList.size());
                    mOnItemClickListener.onUnbindClick(dataList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int positon, Object object);

        void onDisconnectClick(Object object);

        void onUnbindClick(Object object);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }
}