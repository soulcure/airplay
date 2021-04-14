package com.skyworth.dpclientsdk;

import android.media.MediaCodec;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface StreamSinkCallback {

    void onConnectState(ConnectState state);

    //local socket string data
    void onData(String data, SocketChannel channel);

    //local socket bytes data
    void onData(byte[] data, SocketChannel channel);

    //audio frame
    void onAudioFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel);

    //video frame
    void onVideoFrame(MediaCodec.BufferInfo bufferInfo, ByteBuffer data, SocketChannel channel);

    void ping(String msg, SocketChannel channel);

    void pong(String msg, SocketChannel channel);

}
