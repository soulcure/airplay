package swaiotos.runtime.h5.gameengine;

import java.util.concurrent.atomic.AtomicInteger;

public class FixAtomicInteger {
    private final AtomicInteger i;

    public FixAtomicInteger() {
        i = new AtomicInteger();
    }

    public final int incrementAndGet() {
        int current;
        int next;
        do {
            current = this.i.get();
            next = current >= 2147483647?0:current + 1;
        } while(!this.i.compareAndSet(current, next));

        return next;
    }

    public final int decrementAndGet() {
        int current;
        int next;
        do {
            current = this.i.get();
            next = current <= 0?2147483647:current - 1;
        } while(!this.i.compareAndSet(current, next));

        return next;
    }
}
