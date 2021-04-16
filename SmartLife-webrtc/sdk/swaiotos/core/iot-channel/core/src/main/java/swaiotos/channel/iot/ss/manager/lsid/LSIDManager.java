package swaiotos.channel.iot.ss.manager.lsid;

/**
 * @ClassName: LSIDManager
 * @Author: lu
 * @CreateDate: 2020/4/26 10:35 AM
 * @Description:
 */
public interface LSIDManager {
    interface Callback {
        void onLSIDUpdate();
    }

    void addCallback(Callback callback);

    void removeCallback(Callback callback);

    LSIDInfo refreshLSIDInfo();

    LSIDInfo getLSIDInfo();

    LSIDInfo reset();

    void setSid(String sid, String token);

    void setSidAndUserId(String sid, String token,String userId);
}
