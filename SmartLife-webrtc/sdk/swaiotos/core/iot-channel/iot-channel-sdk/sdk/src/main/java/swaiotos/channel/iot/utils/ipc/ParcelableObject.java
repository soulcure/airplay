package swaiotos.channel.iot.utils.ipc;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @ClassName: ParcelableObject
 * @Author: lu
 * @CreateDate: 2020/3/21 2:08 PM
 * @Description:
 */
public class ParcelableObject<T extends Parcelable> implements Parcelable {
    public static class ParcelableInteger implements Parcelable {
        public final int value;

        public ParcelableInteger(int value) {
            this.value = value;
        }

        public ParcelableInteger(Parcel source) {
            this.value = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(value);
        }

        public static final Creator<ParcelableInteger> CREATOR = new Creator<ParcelableInteger>() {
            @Override
            public ParcelableInteger createFromParcel(Parcel source) {
                return new ParcelableInteger(source);
            }

            @Override
            public ParcelableInteger[] newArray(int size) {
                return new ParcelableInteger[size];
            }
        };
    }


    public final int code;
    public final String extra;
    public final T object;

    public ParcelableObject(int code, String extra, T object) {
        this.code = code;
        this.extra = extra;
        this.object = object;
    }

    public ParcelableObject(Parcel source) {
        this.code = source.readInt();
        this.extra = source.readString();
        this.object = source.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(extra);
        if (object != null) {
            dest.writeParcelable(object, flags);
        }
    }

    public static final Creator<ParcelableObject> CREATOR = new Creator<ParcelableObject>() {
        @Override
        public ParcelableObject createFromParcel(Parcel source) {
            return new ParcelableObject(source);
        }

        @Override
        public ParcelableObject[] newArray(int size) {
            return new ParcelableObject[size];
        }
    };
}
