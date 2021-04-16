package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.device.BindCodeMsg;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

/**
 * 获取分享绑定码
 */
public interface BindCodeRepository {

    InvocateFuture<BindCodeMsg> getBindCode(String accessToken, String activationId, String spaceId);
}
