package com.threesoft.webrtc.webrtcroom.webrtcmodule;

import android.content.Context;
import android.util.Log;

import org.webrtc.CapturerObserver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MP4Capturer implements VideoCapturer {



    private static final String TAG = "MP4Capturer";
    private final  VideoReader videoReader;
    private CapturerObserver capturerObserver;
    private  Timer timer = new Timer();
    private  TimerTask tickTask = new TimerTask() {
        public void run() {
            MP4Capturer.this.tick();
        }
    };

    public MP4Capturer(String inputFile)  {
        Log.d(TAG,"MP4Capturer,inputfile:"+inputFile);
        this.videoReader = new VideoReaderMP4(inputFile);

    }


    public void tick() {
        VideoFrame videoFrame = this.videoReader.getNextFrame();
        if(videoFrame != null){
            if(capturerObserver != null){
                this.capturerObserver.onFrameCaptured(videoFrame);
            }
            videoFrame.release();
        }

    }

    public void setVideoPath(String path){
        videoReader.setVideoPath(path);
        if(timer != null){
            timer.cancel();
            timer = null;

        }
        if(tickTask != null){
            tickTask.cancel();
            tickTask = null;
        }
        timer = new Timer();
        tickTask = new TimerTask() {
            public void run() {
                MP4Capturer.this.tick();
            }
        };
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper, Context context, CapturerObserver capturerObserver) {
        this.capturerObserver = capturerObserver;
    }
    @Override
    public void startCapture(int width, int height, int framerate) {
        Log.d(TAG,"startCapture,width:"+width+",height:"+height+",framerate:"+framerate);
        this.timer.schedule(this.tickTask, 0L, (long)(1000 / framerate));
    }
    @Override
    public void stopCapture() throws InterruptedException {
        this.timer.cancel();
    }
    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {
    }
    @Override
    public void dispose() {
        this.videoReader.close();
    }
    @Override
    public boolean isScreencast() {
        return true;
    }


    private static class VideoReaderMP4 implements VideoReader {

        private AvcDecoder2 decoder;

        public  VideoReaderMP4(String path){
            if(path != null && path.length() > 0){
                decoder = new AvcDecoder2();
                try{
                    decoder.setDecoderParams(path,AvcDecoder2.FILE_TypeI420);
                    decoder.start();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        }
        public void setVideoPath(String path){
            if(path != null && path.length() > 0){
                if(decoder != null){
                    decoder.stop();
                }
                decoder = new AvcDecoder2();
                try{
                    decoder.setDecoderParams(path,AvcDecoder2.FILE_TypeI420);
                    decoder.start();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        public VideoFrame getNextFrame(){
            //Log.d(TAG,"getNextFrame");
            return decoder.getNextFrame();
        }

        public void close() {
            try {
                decoder.stop();
            } catch (Exception var2) {
                Log.e(TAG, "Problem closing file", var2);
            }

        }

    }

    private interface VideoReader {
        VideoFrame getNextFrame();
        void close();
        void setVideoPath(String path);
    }

}
