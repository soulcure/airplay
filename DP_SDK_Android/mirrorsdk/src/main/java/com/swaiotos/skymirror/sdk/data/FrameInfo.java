package com.swaiotos.skymirror.sdk.data;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class FrameInfo {
    public ByteBuffer encodedFrame;
    public MediaCodec.BufferInfo bufferInfo;

    public FrameInfo(MediaCodec.BufferInfo info, ByteBuffer frame) {
        bufferInfo = info;
        encodedFrame = frame;
    }
}
