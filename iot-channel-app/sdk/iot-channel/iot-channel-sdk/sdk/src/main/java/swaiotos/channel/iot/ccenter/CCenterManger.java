package swaiotos.channel.iot.ccenter;

import java.util.Map;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.ccenter
 * @ClassName: CCenterManger
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/12/18 10:32
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/12/18 10:32
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public interface CCenterManger {

    interface CCenterListener {
        void ccodeCallback(String code);
    }

    void getCCodeString(CCenterListener cCenterListener);

    void getCCodeString(Map<String, String> map, CCenterListener cCenterListener);

}
