package com.swaiotos.skymirror.sdk.capture;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.swaiotos.skymirror.sdk.Command.Command;
import com.swaiotos.skymirror.sdk.data.MediaCodecConfig;
import com.swaiotos.skymirror.sdk.util.DLNACommonUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @ProjectName: airPlayDemo
 * @Package: com.swaiotos.skymirror.sdk.capture
 * @ClassName: PlayerEncode
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/9/7 11:32
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/9/7 11:32
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PlayerEncoder {
    private static final String TAG = PlayerEncoder.class.getSimpleName();

    private static final int MIN_BITRATE_THRESHOLD = 4 * 1024 * 1024;  //bit per second，每秒比特率
    private static final int DEFAULT_BITRATE = 6 * 1024 * 1024;
    private static final int MAX_BITRATE_THRESHOLD = 8 * 1024 * 1024;
    private int mBitrate = DEFAULT_BITRATE;
    private String mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;

    private static final int MAX_VIDEO_FPS = 30;   //frames/sec
    private static final int I_FRAME_INTERVAL = 5;  //关键帧频率，5秒一个关键帧

    private MediaProjection mMediaProjection;
    private MediaCodec encoder;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec.BufferInfo mBufferInfo;
    private boolean isReset = false;

    private int mWidth;
    private int mHeight;
    private int mEncoderCodecSupportType = -1; //硬件编解码器信息
    private Context mContext;

    public PlayerEncoder(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        mBufferInfo = new MediaCodec.BufferInfo();
        isReset = false;
    }

    public void createMediaProjection(int resultCode, Intent data) {
        if (resultCode == -10 || data == null)
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mMediaProjection == null) {
                mMediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            }
        }
    }

    /**
     * 检测是否支持H264 和 H265硬编码
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void checkEncoderSupportCodec() {
        MediaCodecConfig h264Config = new MediaCodecConfig();
        MediaCodecConfig h265Config = new MediaCodecConfig();

        //获取所有编解码器个数
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            //获取所有支持的编解码器信息
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            // 判断是否为编码器，否则直接进入下一次循环
            if (codecInfo.isEncoder()) {  //编码器
                // 如果是解码器，判断是否支持Mime类型
                String[] types = codecInfo.getSupportedTypes();
                for (String type : types) {
                    if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                        Log.d(TAG, codecInfo.getName() + "H264 硬编码 Supported");
                        h264Config.setSupport(true);

                        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                        Range<Integer> bitrateRange = videoCapabilities.getBitrateRange();
                        int maxBitrate = bitrateRange.getUpper();
                        if (h264Config.getMaxBitrate() < maxBitrate) {
                            h264Config.setMaxBitrate(maxBitrate);
                        }
                        Log.d(TAG, codecInfo.getName() + "H264 硬编码 maxBitrate---" + maxBitrate);

                        Range<Integer> widthRange = videoCapabilities.getSupportedWidths();
                        int maxWidth = widthRange.getUpper();
                        if (h264Config.getMaxWidth() < maxWidth) {
                            h264Config.setMaxWidth(maxWidth);
                        }

                        Log.d(TAG, codecInfo.getName() + "H264 硬编码 maxWidth---" + maxWidth);

                        Range<Integer> heightRange = videoCapabilities.getSupportedHeights();
                        int maxHeight = heightRange.getUpper();
                        if (h264Config.getMaxHeight() < maxHeight) {
                            h264Config.setMaxHeight(maxHeight);
                        }

                        Log.d(TAG, codecInfo.getName() + "H264 硬编码 maxHeight---" + maxHeight);

                    } else if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                        Log.d(TAG, codecInfo.getName() + "H265 硬编码 Supported");
                        if (Config.isDongle()) {
                            Log.d(TAG, "Dongle set H265 not Supported");
                            h265Config.setSupport(false);
                        } else {
                            h265Config.setSupport(true);
                        }
                        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(type);
                        MediaCodecInfo.VideoCapabilities videoCapabilities = capabilities.getVideoCapabilities();
                        Range<Integer> bitrateRange = videoCapabilities.getBitrateRange();
                        int maxBitrate = bitrateRange.getUpper();
                        if (h265Config.getMaxBitrate() < maxBitrate) {
                            h265Config.setMaxBitrate(maxBitrate);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬编码 maxBitrate---" + maxBitrate);

                        Range<Integer> widthRange = videoCapabilities.getSupportedWidths();
                        int maxWidth = widthRange.getUpper();
                        if (h265Config.getMaxWidth() < maxWidth) {
                            h265Config.setMaxWidth(maxWidth);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬编码 maxWidth---" + maxWidth);

                        Range<Integer> heightRange = videoCapabilities.getSupportedHeights();
                        int maxHeight = heightRange.getUpper();
                        if (h265Config.getMaxHeight() < maxHeight) {
                            h265Config.setMaxHeight(maxHeight);
                        }
                        Log.d(TAG, codecInfo.getName() + "H265 硬编码 maxHeight---" + maxHeight);
                    }
                }
            }
        }
        Log.d(TAG, "h264Config 硬编码---" + h264Config.toString());
        Log.d(TAG, "h265Config 硬编码---" + h265Config.toString());

        checkEncodeWH();

        mEncoderCodecSupportType = 0;   //0 代表初始化完成

        if (mWidth <= h264Config.getMaxWidth()
                && mHeight <= h264Config.getMaxHeight()
                && h264Config.isSupport()) {
            mEncoderCodecSupportType |= Command.CODEC_AVC_FLAG;  //支持h264
        }
        if (mWidth <= h265Config.getMaxWidth()
                && mHeight <= h265Config.getMaxHeight()
                && h265Config.isSupport()) {
            mEncoderCodecSupportType |= Command.CODEC_HEVC_FLAG; //支持h265
        }

        Log.d(TAG, "h264 || h265 encoder---" + mWidth + "X" + mHeight +
                "mEncoderCodecSupportType:" + mEncoderCodecSupportType);
    }


    public void checkEncodeWH() {
        DisplayMetrics dm = new DisplayMetrics();
        Display mDisplay = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        mDisplay.getMetrics(dm);

        int deviceWidth = dm.widthPixels;
        int deviceHeight = dm.heightPixels;

        Configuration configuration = mContext.getResources().getConfiguration(); //获取设置的配置信息
        int ori = configuration.orientation; //获取屏幕方向

        if (DLNACommonUtil.checkPermission(mContext)) {//for tv
            if (deviceHeight >= 2160) {  //4K TV
                Log.e(TAG, "tv deviceHeight= 2160");
                deviceWidth = deviceWidth / 2;
                deviceHeight = deviceHeight / 2;
            }
            mWidth = deviceWidth;
            mHeight = deviceHeight;
        } else if (ori == Configuration.ORIENTATION_LANDSCAPE) {  ///横屏 for pad
            if (deviceWidth > 1920) {  //横屏优先配置宽度
                mWidth = 1920;
                mHeight = 1920 * deviceHeight / deviceWidth;
            } else if (deviceHeight > 1080) {
                mWidth = 1080 * deviceWidth / deviceHeight;
                mHeight = 1080;
            } else {
                mWidth = deviceWidth;
                mHeight = deviceHeight;
            }
        } else {  //竖屏 for mobile
            if (deviceHeight > 1920) {  //竖屏优先配置高度
                mWidth = 1920 * deviceWidth / deviceHeight;
                mHeight = 1920;
            } else if (deviceWidth > 1080) {
                mWidth = 1080;
                mHeight = 1080 * deviceHeight / deviceWidth;
            } else {
                mWidth = deviceWidth;
                mHeight = deviceHeight;
            }
        }

        if ((mWidth & 1) == 1) {
            mWidth--;
        }
        if ((mHeight & 1) == 1) {
            mHeight--;
        }
    }


    public void setContentMimeType(String mimeType) {
        Log.d(TAG, "setContentMimeType---" + mimeType);
        this.mimeType = mimeType;
    }


    public void createMediaCodec() throws IOException {

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mimeType, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate); //设置比特率

        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, MAX_VIDEO_FPS);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);//将一个android surface进行mediaCodec编码
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        encoder = MediaCodec.createEncoderByType(mimeType);
        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        Log.d(TAG, "createDisplaySurface:" + mWidth + "x" + mHeight + "---mimeType:"
                + mimeType + "---mBitrate:" + mBitrate);
    }


    public void createDisplayManager() {
        if (encoder == null)
            return;

        if (DLNACommonUtil.checkPermission(mContext)) { //for tv
            Log.d(TAG, "startDisplayManager: create virtualDisplay by DisplayManager");
            DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            mVirtualDisplay = displayManager.createVirtualDisplay(
                    "TV Screen Mirror", mWidth, mHeight,
                    50,
                    encoder.createInputSurface(),
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                            | DisplayManager.VIRTUAL_DISPLAY_FLAG_SECURE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && mMediaProjection != null) {
            Log.d(TAG, "startDisplayManager: create virtualDisplay by mediaProjection");
            mVirtualDisplay = mMediaProjection
                    .createVirtualDisplay(
                            "PAD Screen Mirror", mWidth, mHeight,
                            50,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                            encoder.createInputSurface(),
                            null, null);// bsp
        }
    }


    public void reConfigure() {
        if (encoder != null) {
            Log.d(TAG, "encoder reConfigure.................");
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(mimeType, mWidth, mHeight);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate); //设置比特率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, MAX_VIDEO_FPS);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);//将一个android surface进行mediaCodec编码
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
            encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            createDisplayManager();
        }
    }


    public void reset() {
        if (encoder != null) {
            Log.d(TAG, "encoder reset.................");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "encoder reset11.................");
                encoder.reset();
                isReset = true;
            }
        }
    }


    public void start() {
        if (encoder != null)
            encoder.start();
    }


    public void release() {
        try {
            if (mVirtualDisplay != null) {
                Log.d(TAG, "virtualDisplay release.................");
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mMediaProjection != null) {
                    mMediaProjection.stop();
                    mMediaProjection = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "virtualDisplay release---" + e.getMessage());
        }

        try {
            if (encoder != null) {
                Log.d(TAG, "encoder release.................");
                encoder.signalEndOfInputStream();
                encoder.stop();
                encoder.release();
                encoder = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "encoder release---" + e.getMessage());
        }

    }


    public void adjustBitRate(long consumedUs) {
        long ts = (mBufferInfo.presentationTimeUs - consumedUs) / 1000;  //纳秒转换为毫秒
        //Log.d(TAG, "adjustBitRate ts---" + ts);

        int bitrate;
        if (ts > 300) { //延时大于300ms
            bitrate = MIN_BITRATE_THRESHOLD;
        } else if (ts < 100) { //延时小于100ms
            bitrate = MAX_BITRATE_THRESHOLD;
        } else {
            bitrate = DEFAULT_BITRATE;
        }

        if (bitrate == mBitrate) {
            //Log.d(TAG, "adjustBitRate no need");
            return;
        }

        Log.d(TAG, "adjustBitRate increase bit rate---" + bitrate);
        Bundle param = new Bundle();
        param.putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, bitrate);
        encoder.setParameters(param);
        mBitrate = bitrate;
    }

    public int dequeueOutputBuffer(long timeoutUs) {
        if (encoder != null)
            return encoder.dequeueOutputBuffer(mBufferInfo, timeoutUs);
        else {
            return -1;
        }
    }


    public ByteBuffer getOutputBuffers(int index) {
        ByteBuffer encodeData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ByteBuffer[] byteBuffers = encoder.getOutputBuffers();
            encodeData = byteBuffers[index];
        } else {
            encodeData = encoder.getOutputBuffer(index);
        }
        return encodeData;
    }


    public void releaseOutputBuffer(int index, boolean render) {
        encoder.releaseOutputBuffer(index, false);
    }

    public MediaProjection getMediaProjection() {
        return mMediaProjection;
    }

    public MediaCodec getEncoder() {
        return encoder;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public MediaCodec.BufferInfo getBufferInfo() {
        return mBufferInfo;
    }

    public int getEncoderCodecSupportType() {
        return mEncoderCodecSupportType;
    }

    public boolean isReset() {
        return isReset;
    }

    public void setReset(boolean reset) {
        isReset = reset;
    }

}
