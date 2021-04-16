package swaiotos.channel.iot.utils;

/**
 * @ClassName: SyncObject
 * @Author: lu
 * @CreateDate: 2020/4/13 2:21 PM
 * @Description:
 */
public class SyncObject<T> {
    private T object = null;

    public T get(long timeout) {
        synchronized (this) {
            if (object == null) {
                try {
                    wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return object;
        }
    }

    public void set(T object) {
        synchronized (this) {
            this.object = object;
            notifyAll();
        }
    }
}