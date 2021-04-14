package com.coocaa.mediacodec;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class FrameInfo {
    MediaCodec.BufferInfo info;
    ByteBuffer byteBuffer;

    public FrameInfo(MediaCodec.BufferInfo info, ByteBuffer byteBuffer) {
        this.info = info;
        this.byteBuffer = byteBuffer;
    }
}
