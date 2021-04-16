package com.coocaa.smartscreen.repository.service;

import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.data.voice.VoiceAdviceInfo;
import com.coocaa.smartscreen.repository.future.InvocateFuture;

/**
 * 语音遥控模块仓库接口
 * Created by  on 2020/6/5
 * @author chenaojun
 */
public interface VoiceControlRepository {


    /**
     * 获取语音命令推荐
     * @return
     */
    InvocateFuture<VoiceAdviceInfo> getAdvice();


}
