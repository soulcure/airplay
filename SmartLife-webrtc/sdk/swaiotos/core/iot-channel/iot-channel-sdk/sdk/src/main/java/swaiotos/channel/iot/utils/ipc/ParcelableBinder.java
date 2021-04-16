package swaiotos.channel.iot.utils.ipc;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName: PacelableBinder
 * @Author: lu
 * @CreateDate: 2020/3/21 3:54 PM
 * @Description:
 */
public class ParcelableBinder implements Parcelable {
    public final int code;
    public final String extra;
    public final IBinder mBinder;


    public ParcelableBinder(IBinder binder) {
        this(0, "", binder);
    }

    public ParcelableBinder(int code, String extra) {
        this(code, extra, null);
    }

    public ParcelableBinder(int code, String extra, IBinder binder) {
        this.code = code;
        this.extra = extra;
        this.mBinder = binder;
    }

    public ParcelableBinder(Parcel source) {
        this.code = source.readInt();
        this.extra = source.readString();
        this.mBinder = source.readStrongBinder();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(extra);
        if (mBinder != null) {
            dest.writeStrongBinder(mBinder);
        }
    }

    public static final Creator<ParcelableBinder> CREATOR = new Creator<ParcelableBinder>() {
        @Override
        public ParcelableBinder createFromParcel(Parcel source) {
            return new ParcelableBinder(source);
        }

        @Override
        public ParcelableBinder[] newArray(int size) {
            return new ParcelableBinder[size];
        }
    };
}
