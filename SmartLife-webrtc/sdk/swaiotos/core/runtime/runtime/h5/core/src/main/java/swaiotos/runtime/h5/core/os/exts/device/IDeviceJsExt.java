package swaiotos.runtime.h5.core.os.exts.device;

import java.io.Serializable;

/**
 * Describe:
 * Created by AwenZeng on 2021/01/07
 */
public interface IDeviceJsExt extends Serializable {

    /**
     * 是否显示dangleTV的二维码
     * @param id js请求id
     * @param isShow 是否显示，"true":显示  "false":隐藏
     */
    void enableDeviceQrcode(String id,String isShow);

    /**
     * 获取当前dangle设备二维码字符串
     * @param id js请求id
     * @param url 加载网页地址
     * @return
     */
    void getDeviceQrcodeString(String id,String url);

    /**
     * 添加二维码变化监听
     * @param id js请求id
     * @return
     */
    void addQrcodeChangedListener(String id);

    /**
     * 删除二维码变化监听
     * @param id js请求id
     * @return
     */
    void removeQrcodeChangedListener(String id,String listenerId);

    void scanQrcode(String id);
}
