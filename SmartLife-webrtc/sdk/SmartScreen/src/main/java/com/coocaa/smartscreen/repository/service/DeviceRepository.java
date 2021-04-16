package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.device.TvProperty;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

import androidx.annotation.Nullable;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;

/**
 * 设备
 * Created by songxing on 2020/7/24
 */
public interface DeviceRepository {

    InvocateFuture<TvProperty> getTvProperty(String chip, String model);
}
