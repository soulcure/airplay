package swaiotos.runtime.h5.core.os.webview;

/**
 * @ClassName: IAppletPushListener
 * @Author: AwenZeng
 * @CreateDate: 2020/10/22 16:22
 * @Description:
 */
public interface IAppletJsPushListener {

    /**
     * 推送消息
     * @param data
     */
    void onReceiveMessage(String data);

}
