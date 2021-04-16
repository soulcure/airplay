package swaiotos.channel.iot.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 将日志保存到文件
 */
public class LogFile {

    public static final boolean DEBUG = true;
    private static final int HANDLER_SAVE_LOG_TO_FILE = 1;
    private ProcessHandler mProcessHandler;

    private static LogFile instance;

    private LogFile() {
        initHandler();
    }

//    public static LogFile inStance() {
//        if (instance == null) {
//            instance = new LogFile();
//        }
//        return instance;
//    }


    /**
     * 将日志保存到SDCARD
     *
     * @param log
     */
    public void toFile(String log) {
        Message msg = mProcessHandler.obtainMessage(HANDLER_SAVE_LOG_TO_FILE);
        msg.obj = log;
        mProcessHandler.sendMessage(msg);
    }


    /**
     * 线程初始化
     */

    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }

    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_SAVE_LOG_TO_FILE:
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA).format(new Date());
                    String log = timeStamp + "  :  " + msg.obj + "\n";
                    String path = getLogPaths();

                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date());

                    String filePath = path + "/log_" + date + ".txt";

                    if (new File(filePath).length() > 20 * 1024) {
                        FileUtils.writeFile(filePath, log, false);
                    } else {
                        FileUtils.writeFile(filePath, log, true);
                    }

                    break;
                default:
                    break;
            }

        }

    }


    /**
     * 获取日志文件路径
     *
     * @return
     */
    public static String getLogPaths() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/LogFile";
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return fileDir.getAbsolutePath();
    }

}
