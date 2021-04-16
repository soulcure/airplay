package swaiotos.runtime.h5.core.os.exts.storage;

public interface IStorageExt {

    void setStorage(String id, String json);

    void getStorage(String id, String json);

    void removeStorage(String id, String json);

    void clearStorage(String id);

    void getStorageInfo(String id);
}
