package swaiotos.channel.iot.tv.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.PadDeviceInfo;
import swaiotos.channel.iot.ss.device.PhoneDeviceInfo;
import swaiotos.channel.iot.tv.R;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.tv.adapter
 * @ClassName: DevicesAdapter
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/5/18 16:55
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/5/18 16:55
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PluginDevicesAdapter extends RecyclerView.Adapter<PluginDevicesAdapter.DevicesAdapterHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private  ArrayList<Device> devices;

    public PluginDevicesAdapter(Context context, ArrayList<Device> devices) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        setList(devices);
    }

    @NonNull
    @Override
    public PluginDevicesAdapter.DevicesAdapterHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rowView = mLayoutInflater.inflate(R.layout.plugin_bind_devices,viewGroup,false);
        return new DevicesAdapterHolder(rowView);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(@NonNull DevicesAdapterHolder devicesAdapterHolder, int position) {
        final Device info = getItem(position);
        if (info != null) {
            if (info.getInfo() != null && info.getInfo().type() == DeviceInfo.TYPE.PAD) {
                PadDeviceInfo padDeviceInfo = (PadDeviceInfo)info.getInfo();
                if (!TextUtils.isEmpty(padDeviceInfo.mNickName)) {
                    devicesAdapterHolder.deviceName.setText(padDeviceInfo.mNickName);
                } else {
                    devicesAdapterHolder.deviceName.setText("移动智慧屏");
                }
                devicesAdapterHolder.deviceIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.plugin_pad));
                devicesAdapterHolder.deviceType.setText(mContext.getResources().getString(R.string.iot_channel_type_panel));
                devicesAdapterHolder.deviceModel.setText(padDeviceInfo.mModel);
            } else if (info.getInfo() != null && info.getInfo().type() == DeviceInfo.TYPE.PHONE) {
                PhoneDeviceInfo phoneDeviceInfo = (PhoneDeviceInfo) info.getInfo();
                if (TextUtils.isEmpty(phoneDeviceInfo.mNickName)) {
                    devicesAdapterHolder.deviceName.setText(phoneDeviceInfo.mModel);
                } else {
                    devicesAdapterHolder.deviceName.setText(phoneDeviceInfo.mNickName);
                }
                devicesAdapterHolder.deviceIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.plugin_phone));
                devicesAdapterHolder.deviceModel.setText(mContext.getResources().getString(R.string.iot_channel_xiaowei_app));
                devicesAdapterHolder.deviceType.setText(String.format(mContext.getResources().getString(R.string.iot_channel_type_phone),phoneDeviceInfo.mModel));
            }
        }

    }

    public void setList(ArrayList<Device> devices) {
        this.devices = devices;
    }

    private Device getItem(int position) {
        if (devices == null) {
            return null;
        }
        return devices.get(position);
    }

    @Override
    public int getItemCount() {
        if (null == devices) {
            return 0;
        }
        return devices.size();
    }

    public void notifyItem(int positon) {
        if (positon < devices.size())
            devices.remove(positon);
        notifyItemRemoved(positon);
        notifyItemRangeChanged(positon, getItemCount());
    }

    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
    }

    class DevicesAdapterHolder extends RecyclerView.ViewHolder {
        private ImageView deviceIcon;
        private TextView deviceType,deviceModel,deviceName;

        public DevicesAdapterHolder(@NonNull View itemView) {
            super(itemView);

            deviceName = itemView.findViewById(R.id.plugin_channel_devices_show_name);
            deviceIcon = itemView.findViewById(R.id.plugin_channel_devices_icon);
            deviceType = itemView.findViewById(R.id.plugin_channel_devices_type);
            deviceModel = itemView.findViewById(R.id.plugin_channel_devices_model);
        }
    }

}
