package swaiotos.runtime.h5.gameengine;

import java.util.Objects;

public class IotKeyCodeMapKey {
    // int keyCodeMapKey 是自增的值
    public int keyCodeMapKey;
    public String identify;

    public IotKeyCodeMapKey(int keyCodeID,String identify){
        this.keyCodeMapKey = keyCodeID;
        this.identify = identify;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IotKeyCodeMapKey))
            return false;
        if (this.identify==null){
            return false;
        }
        IotKeyCodeMapKey that = (IotKeyCodeMapKey) o;
        return keyCodeMapKey == that.keyCodeMapKey &&
               identify.equalsIgnoreCase(that.identify);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodeMapKey, identify);
    }
}
