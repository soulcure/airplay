package swaiotos.channel.iot.utils;

import java.util.concurrent.CountDownLatch;

/**
 * @ClassName: ObjectCountDownLatch
 * @Author: lu
 * @CreateDate: 2020/3/19 9:28 PM
 * @Description:
 */
public class ObjectCountDownLatch<T> extends CountDownLatch {
    private T object;

    public ObjectCountDownLatch(int count) {
        super(count);
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
