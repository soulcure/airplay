package com.coocaa.mediacodec;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SurfaceTextureCamera2Activity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener {
    HandlerThread mThreadHandler;
    Handler mHandler, mainHandler;
    TextureView mPreviewView;
    CameraCaptureSession mSession;
    CaptureRequest.Builder mPreviewBuilder;
    ImageView iv_show;
    CameraDevice mCameraDevice;
    ImageReader mImageReader;
    Surface mEncoderSurface;
    BufferedOutputStream outputStream;
    private MediaCodec mCodec, mDecodec;
    boolean isEncode = false;
    private String TAG = "yao";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    ///为了使照片竖直显示
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main2);
        mThreadHandler = new HandlerThread("CAMERA2");
        mThreadHandler.start();
        mHandler = new Handler(mThreadHandler.getLooper());
        mainHandler = new Handler(getMainLooper());
        mPreviewView = (TextureView) findViewById(R.id.cameraSurface);
        mPreviewView.setSurfaceTextureListener(this);

        Button btn = (Button) findViewById(R.id.takePhoto);
        btn.setOnClickListener(this);

        iv_show = (ImageView) findViewById(R.id.show_photo);

        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i(TAG, "onImageAvailable");
                // mCameraDevice.close();
                //mPreviewView.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                // 拿到拍照照片数据
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);//由缓冲区存入字节数组
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    iv_show.setImageBitmap(bitmap);
                    image.close();
                }
            }
        }, mainHandler);
    }

    public void initDecoder() {
        try {
            //根据需要解码的类型创建解码器
            mDecodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mPreviewView.getWidth(), mPreviewView.getHeight());
        //MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

        //TextureView
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewView.getWidth(), mPreviewView.getHeight());
        //texture.setOnFrameAvailableListener(this);
        Surface surface0 = new Surface(texture);
        mDecodec.configure(mediaFormat, surface0, null, 0); //直接解码送surface显示

        // mCodec.setCallback(new DecoderCallback());
        //开始解码
        mDecodec.start();
    }

    public void startCodec() {
        File f = new File(Environment.getExternalStorageDirectory(), "camera2mediacodec0.264");
        if (!f.exists()) {
            try {
                f.createNewFile();
                Log.e(TAG, "       create a file     ");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (f.delete()) {
                try {
                    f.createNewFile();
                    Log.e(TAG, "      delete and create a file    ");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(f));
            Log.i("Encoder", "outputStream initialized");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mPreviewView.getWidth(), mPreviewView.getHeight());

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 500000);//500kbps
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
        //ediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface); //COLOR_FormatSurface
        //mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
        //      MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);  //COLOR_FormatYUV420Planar
        //mediaFormat.setInteger(MediaFormat.KEY_ROTATION, 90);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5); //锟截硷拷帧锟斤拷锟绞憋拷锟� 锟斤拷位s
        mCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mEncoderSurface = mCodec.createInputSurface();
        //method 1
        mCodec.setCallback(new EncoderCallback());
        mCodec.start();
    }

    public void stopCodec() {
        try {
            if (isEncode) {
                isEncode = false;
            } else {
                mCodec.stop();
                mCodec.release();
                mCodec = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mCodec = null;
        }
    }

    int mCount = 0;

    public void onFrameDecodec(byte[] buf, int offset, int length) {
        //-1表示一直等待；0表示不等待；其他大于0的参数表示等待毫秒数
        int inputBufferIndex = mDecodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mDecodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            //解码
            long timestamp = mCount * 1000000 / 25;
            mDecodec.queueInputBuffer(inputBufferIndex, 0, length, timestamp, 0);
            mCount++;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mDecodec.dequeueOutputBuffer(bufferInfo, 0); //10
        //循环解码，直到数据全部解码完成
        while (outputBufferIndex >= 0) {
            //logger.d("outputBufferIndex = " + outputBufferIndex);
            mDecodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mDecodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    public void onFrame(byte[] buf, int offset, int length) {
        //-1表示一直等待；0表示不等待；其他大于0的参数表示等待毫秒数
        int inputBufferIndex = mCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0); //10
        //循环解码，直到数据全部解码完成
        while (outputBufferIndex >= 0) {
            //logger.d("outputBufferIndex = " + outputBufferIndex);
            ByteBuffer outputBuffer = mCodec.getOutputBuffer(outputBufferIndex);

            byte[] outData = new byte[bufferInfo.size];
            outputBuffer.get(outData);

            try {
                outputStream.write(outData, 0, outData.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            mCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    private class EncoderCallback extends MediaCodec.Callback {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            //
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
            //ByteBuffer encodedData = codec.getOutputBuffer(index);
            //encodedData.position(info.offset);
            //encodedData.limit(info.offset + info.size);
            //Log.d(TAG, "onOutputBufferAvailable, info.size: " + info.size);
            ByteBuffer outPutByteBuffer = mCodec.getOutputBuffer(index);
            byte[] outDate = new byte[info.size];
            outPutByteBuffer.get(outDate);

            try {
                Log.d(TAG, " outDate.length : " + outDate.length);
                outputStream.write(outDate, 0, outDate.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mCodec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            Log.d(TAG, "Error: " + e);
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
            Log.d(TAG, "encoder output format changed: " + format);
        }
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            Log.i(TAG, "onSurfaceTextureAvailable:  width = " + width + ", height = " + height);
            String[] CameraIdList = cameraManager.getCameraIdList();
            //获取可用相机设备列表
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(CameraIdList[0]);
            //在这里可以通过CameraCharacteristics设置相机的功能,当然必须检查是否支持
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            //就像这样
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startCodec();
            cameraManager.openCamera(CameraIdList[0], mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        surfaceView = surface;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopCodec();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }


    SurfaceTexture surfaceView;

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            try {
                Log.i(TAG, "       CameraDevice.StateCallback  onOpened            ");
                mCameraDevice = camera;
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                SurfaceTextureCamera2Activity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
        }
    };

    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewView.getWidth(), mPreviewView.getHeight());
        Surface surface = new Surface(texture);

        Log.i(TAG, "startPreview");
        try {
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW); //CameraDevice.TEMPLATE_STILL_CAPTURE
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);
        mPreviewBuilder.addTarget(mEncoderSurface);

        //对于拍照而言，有两个输出流：一个用于预览、一个用于拍照。
        //对于录制视频而言，有两个输出流：一个用于预览、一个用于录制视频。
        camera.createCaptureSession(Arrays.asList(surface, mEncoderSurface, mImageReader.getSurface()), mSessionStateCallback, mHandler);
    }


    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                Log.i(TAG, "      onConfigured          ");
                //session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
                mSession = session;
                // 自动对焦
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                // 打开闪光灯
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                mPreviewBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
                session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler); //null
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };

    int callback_time;
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            //Toast.makeText(SurfaceTextureCamera2Activity.this, "picture success！", Toast.LENGTH_SHORT).show();
            callback_time++;
            Log.i(TAG, "    CaptureCallback =  " + callback_time);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            Toast.makeText(SurfaceTextureCamera2Activity.this, "picture failed！", Toast.LENGTH_SHORT).show();
        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback0 = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            Toast.makeText(SurfaceTextureCamera2Activity.this, "take picture success！", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            Toast.makeText(SurfaceTextureCamera2Activity.this, "take picture failed！", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        takePicture();
        /*
        try {
            mSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback0, mHandler);
            //mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

        //session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);就可以了
    }


    private void takePicture() {
        if (mCameraDevice == null) return;
        // 创建拍照需要的CaptureRequest.Builder
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 获取手机方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mSession.capture(mCaptureRequest, mSessionCaptureCallback0, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mPreviewView.isShown()) {
                mPreviewView.setVisibility(View.VISIBLE);
                iv_show.setVisibility(View.GONE);
                return true;
            }
            if (iv_show.isShown()) {
                iv_show.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}