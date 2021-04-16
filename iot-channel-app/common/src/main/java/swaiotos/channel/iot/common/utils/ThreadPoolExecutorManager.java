package swaiotos.channel.iot.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.utils
 * @ClassName: ThreadPoolManager
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/10 9:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/10 9:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ThreadPoolExecutorManager {

    private static ExecutorService executorServiceSingleton;

    public static ExecutorService getInstance() {
        if (executorServiceSingleton == null) {
            synchronized (ThreadPoolExecutorManager.class) {
                if (executorServiceSingleton == null) {
                    executorServiceSingleton = new ThreadPoolExecutor(5,10,0,
                            TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>());
                }
            }
        }
        return executorServiceSingleton;
    }

}
