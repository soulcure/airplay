package com.swaiotos.skymirror.sdk.reverse;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

/**
 * @ClassName: AudioPlayer
 * @Description: java类作用描述
 * @Author: lfz
 * @Date: 2020/4/15 10:52
 */
public class AudioPlayer {

    private String TAG = AudioPlayer.class.getSimpleName();

    private int mFrequency;// 采样率
    private int mChannel;// 声道
    private int mSampBit;// 采样精度
    private AudioTrack mAudioTrack;

    public AudioPlayer(int frequency, int channel, int sampbit) {
        this.mFrequency = frequency;
        this.mChannel = channel;
        this.mSampBit = sampbit;
    }

    /**
     * 初始化
     */
    public void init() {
        if (mAudioTrack != null) {
            release();
        }
        // 获得构建对象的最小缓冲区大小
        int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);
        Log.d(TAG, "AudioTrack init minbuffer size :" + minBufSize);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mFrequency, mChannel, mSampBit, minBufSize * 2, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

    /**
     * 将解码后的pcm数据写入audioTrack播放
     *
     * @param data   数据
     * @param offset 偏移
     * @param length 需要播放的长度
     */
    public void play(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            mAudioTrack.write(data, offset, length);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public long gettimeStamp() {//ms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            long CurrentPositionUs = ((long) (mAudioTrack.getPlaybackHeadPosition()) * 1000L) / (long) mFrequency;
            Log.d(TAG, "Audio Track CurrentPositionUs " + CurrentPositionUs);
            return CurrentPositionUs;
        }

        return 0L;
    }
}
