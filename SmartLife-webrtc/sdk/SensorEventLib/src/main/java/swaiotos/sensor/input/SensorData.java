package swaiotos.sensor.input;

import java.io.Serializable;

/**
 * @Author: yuzhan
 */
public class SensorData implements Serializable {
    public String  mName;
    public String  mVendor;
    public int     mVersion;
    public int     mHandle;
    public int     mType;
    public float   mMaxRange;
    public float   mResolution;
    public float   mPower;
    public int     mMinDelay;
    public int     mFifoReservedEventCount;
    public int     mFifoMaxEventCount;
//    public String  mStringType;
    public String  mRequiredPermission;
//    public int     mMaxDelay;
    public int     mFlags;
    public int     mId;

    public int accuracy;
    public long timestamp;
    public float[] values;
}
