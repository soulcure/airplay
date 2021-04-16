package com.coocaa.publib.voice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

/**
 * Created by dengxiuzhen on 2018/3/3.
 */

public class SkyAudioTask implements Runnable {
    private static final String TAG = SkyAudioTask.class.getSimpleName();

    private Context mContext;

    private static AudioRecord mAudioRecord;

    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int audioSource = MediaRecorder.AudioSource.DEFAULT;
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int sampleRate = 16000;
    public static final int MIN_BUFFER = 640;//4096;

    private int minBufferSize;
    private short[] audioBuffer;

    private volatile boolean isAudioRecorderWorking = false;

    private OnRawDataSendCallback callbackRawData;

    public SkyAudioTask(Context context, OnRawDataSendCallback callback) {
        mContext = context;
        callbackRawData = callback;
    }


    public interface OnRawDataSendCallback{
        void onRawDataSend(final byte[] data, int volMax);
    }

    private boolean prepareAudioRecord() {
        Log.e(TAG, "prepareAudioRecord");
        boolean ret = false;

        this.sampleRate = 16000;
        this.channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        this.audioFormat = AudioFormat.ENCODING_PCM_16BIT;

        releaseAudioRecord();

        try {
            this.minBufferSize = AudioRecord.getMinBufferSize(this.sampleRate, this.channelConfig, this.audioFormat);
            if (this.minBufferSize <= 0) {
                this.channelConfig = AudioFormat.CHANNEL_IN_MONO;
                this.minBufferSize = AudioRecord.getMinBufferSize(this.sampleRate, this.channelConfig, this.audioFormat);
                if (this.minBufferSize <= 0) {
                    Log.e(TAG, "prepare getMinBufferSize faild! " + minBufferSize);
                    return false;
                }
            }

//            this.minBufferSize = MIN_BUFFER; //这句话导致小米3 4.4系统的机器无法初始化AudioRecorder modified by wuhaiyuan 2018.5.23

            if (this.channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                mAudioRecord = new AudioRecord(this.audioSource, this.sampleRate, this.channelConfig, this.audioFormat, 2 * this.minBufferSize);
                if ((mAudioRecord == null) || (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)) {
                    releaseAudioRecord();
                    this.channelConfig = AudioFormat.CHANNEL_IN_MONO;
                    mAudioRecord = new AudioRecord(this.audioSource, this.sampleRate, this.channelConfig, this.audioFormat, this.minBufferSize);
                }
            } else {
                mAudioRecord = new AudioRecord(this.audioSource, this.sampleRate, this.channelConfig, this.audioFormat, this.minBufferSize);
            }

            if ((mAudioRecord == null) || (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)) {
                releaseAudioRecord();
                Log.e(TAG, "get state failed!");
                return false;
            }

            if (this.channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                audioBuffer = new short[minBufferSize];
            } else {
                audioBuffer = new short[minBufferSize / 2];
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void releaseAudioRecord() {
        Log.e(TAG, "releaseAudioRecord");
        if (mAudioRecord == null)
            return;

        try {
            if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                mAudioRecord.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    @Override
    public void run() {
        synchronized (SkyAudioTask.class) {
            if (!prepareAudioRecord()) {
                Log.e(TAG, "prepareAudioRecord failed!");
                releaseAudioRecord();
                return;
            }

            try {
                mAudioRecord.startRecording();
            } catch (Exception e) {
                e.printStackTrace();
                releaseAudioRecord();
                return;
            }

            if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                Log.d(TAG, "请去系统设置检查录音权限是否打开！");
                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable(){
                    public void run(){
                        Toast.makeText(mContext.getApplicationContext(), "请去系统设置检查录音权限是否打开！", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            while ((audioBuffer != null) && (mAudioRecord != null) && (isAudioRecorderWorking == true)) {
                int curWavSize = mAudioRecord.read(audioBuffer, 0, audioBuffer.length);
                if ((curWavSize > 0) && (curWavSize <= audioBuffer.length)) {
                    short[] tmpWavData;
                    if (channelConfig == AudioFormat.CHANNEL_IN_STEREO) {
                        int i = 0;
                        int j = 0;
                        tmpWavData = new short[curWavSize / 2];
                        for (i = 0; i < curWavSize; i += 2) {
                            tmpWavData[j] = ((short) ((audioBuffer[i] + audioBuffer[(i + 1)]) / 2));
                            j++;
                        }
                    } else {
                        tmpWavData = new short[curWavSize];
                        System.arraycopy(audioBuffer, 0, tmpWavData, 0, curWavSize);
                    }

                    ByteArrayOutputStream mWaveBuffer = new ByteArrayOutputStream();
                    for (short singlebuf : tmpWavData) {
                        try {
                            mWaveBuffer.write((byte) (singlebuf & 0xFF));
                            mWaveBuffer.write((byte) (0xFF & (singlebuf >> 8)));
                        } catch (Exception localException3) {
                            Log.e(TAG, "write data failed!");
                        }
                    }

                    try {
                        Log.e(TAG, "callback raw data: " + mWaveBuffer.size());
                        if (callbackRawData != null) {
                            int volMax = getVolumeMax(curWavSize, audioBuffer);
                            Log.d(TAG, "volMax = : " + volMax);
                            callbackRawData.onRawDataSend(mWaveBuffer.toByteArray(), volMax);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }

            Log.e(TAG, "finish while");
            releaseAudioRecord();
        }
    }

    public synchronized void setAudioTaskIsWorking(boolean b) {
        Log.e(TAG, "setAudioTaskIsWorking  = " + b);
        this.isAudioRecorderWorking = b;

    }

    private int getVolumeMax(int r, short[] bytes_pkg) {
        //way 2
        int mShortArrayLenght = r / 2;
        short[] short_buffer = bytes_pkg.clone();
        int max = 0;
        if (r > 0) {
            for (int i = 0; i < mShortArrayLenght; i++) {
                if (Math.abs(short_buffer[i]) > max) {
                    max = Math.abs(short_buffer[i]);
                }
            }
        }
        return max;
    }

    private short[] byteArray2ShortArray(byte[] data, int items) {
        short[] retVal = new short[items];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);

        return retVal;
    }
}
