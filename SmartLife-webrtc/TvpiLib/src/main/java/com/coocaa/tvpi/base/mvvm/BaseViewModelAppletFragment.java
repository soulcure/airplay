package com.coocaa.tvpi.base.mvvm;

import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.np.NPAppletActivity;

/**
 * @Author: yuzhan
 */
public class BaseViewModelAppletFragment<VM extends BaseViewModel> extends BaseViewModelFragment<VM> {
    protected NPAppletActivity.NPAppletInfo mNPAppletInfo;
    protected AppletActivity.HeaderHandler mHeaderHandler;

    public BaseViewModelAppletFragment setAppletInfo(NPAppletActivity.NPAppletInfo appletInfo) {
        this.mNPAppletInfo = appletInfo;
        return this;
    }

    public BaseViewModelAppletFragment setAppletHeaderHandler(AppletActivity.HeaderHandler headerHandler) {
        this.mHeaderHandler = headerHandler;
        return this;
    }
}
