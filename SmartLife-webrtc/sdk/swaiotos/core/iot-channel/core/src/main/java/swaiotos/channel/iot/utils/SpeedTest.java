package swaiotos.channel.iot.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;

public class SpeedTest implements Runnable {
    private static final int TIMES = 10;

    private final String targetIp;
    private int mPingSize;
    private int mCount;
    private float mInterval;
    private CallBack mCallBack;
    private ConnectCallback mConnectCallback;

    private final Handler mHandler;

    private boolean isExit;

    public interface CallBack {
        void curSpeed(float speed, String unit);

        void onFinished(float speed, String unit);

        void onProgress(float rate, String unit);

        void lossRate(String rate);
    }


    public interface ConnectCallback {
        void onFinished(float speed, String unit);

        void onProgress(float rate, String unit);

        void onResult(int code, String message);

        void lossRate(String rate);
    }

    public void close() {
        isExit = true;
    }


    public SpeedTest(String address, int pingSize, CallBack callback) {
        targetIp = address;
        mPingSize = pingSize;
        mCallBack = callback;
        mHandler = new Handler(Looper.getMainLooper());
        isExit = false;
    }


    public SpeedTest(String address, int count, float interval, ConnectCallback callback) {
        targetIp = address;
        mCount = count;
        mInterval = interval;
        mConnectCallback = callback;
        mHandler = new Handler(Looper.getMainLooper());
        isExit = false;
    }


    public void open() {
        Log.d("yao", "SpeedTest ping---" + targetIp);
        new Thread(this, "SpeedTest-thread").start();
    }


    @Override
    public void run() {
        if (mCallBack != null) {
            pingTarget();
        } else if (mConnectCallback != null) {
            pingConnection();
        }
    }


    private void pingTarget() {
        StringBuilder comment = new StringBuilder();
        comment.append("ping").append(" -c ").append(TIMES).append(" -w ")
                .append(TIMES).append(" -s ").append(" ").append(mPingSize).append(" ").append(targetIp);

        //ping -c 10 -w 10 -s 20480 172.20.144.80
        Log.d("test", "SpeedTest cmd=" + comment.toString());
        try {
            Process process = Runtime.getRuntime().exec(comment.toString());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
            int totalSize = 0;
            float totalTime = 0;
            int packageSize = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d("test", "SpeedTest pingTarget line=" + line);
                if (!TextUtils.isEmpty(line) && line.contains("ttl")) {
                    try {
                        String[] splits = line.split(" ");

                        if (splits.length > 0) {
                            for (int i = 0; i < splits.length; i++) {
                                String item = splits[i];

                                if (item.equals("bytes") && i > 0) {
                                    String size = splits[i - 1];
                                    packageSize = Integer.parseInt(size);

                                    totalSize = totalSize + packageSize;

                                    Log.d("test", "packageSize=" + packageSize + "  totalSize=" + totalSize);

                                } else if (item.startsWith("icmp_seq=")) {
                                    String seqStr = item.substring("icmp_seq=".length());
                                    Log.d("test", "icmp_seq=" + seqStr);

                                    int seq = Integer.parseInt(seqStr);


                                    final float rate = seq * 100.0f / TIMES;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mCallBack != null) {
                                                mCallBack.onProgress(rate, "%");
                                            }
                                        }
                                    });

                                } else if (item.startsWith("Destination")) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mCallBack != null) {
                                                mCallBack.onFinished(0, "Mb/s");
                                            }
                                        }
                                    });
                                } else if (item.startsWith("time=")) {
                                    String timeStr = item.substring("time=".length());

                                    float time = Float.parseFloat(timeStr);
                                    totalTime = totalTime + time;

                                    Log.d("test", "time=" + time + "  totalTime=" + totalTime);

                                    try {
                                        final float speed = (packageSize * 8 * 1000) / (time * 1024 * 1024);
                                        if (speed > 0) {
                                            BigDecimal b = new BigDecimal(speed);
                                            final float sp = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (mCallBack != null) {
                                                        mCallBack.curSpeed(sp, "Mb/s");
                                                    }
                                                }
                                            });

                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line) && line.contains("packet loss")) {
                    String[] splits = line.split(" ");
                    if (splits.length > 0) {

                        for (int i = 0; i < splits.length; i++) {
                            if (splits[i].equals("packet") && i > 0) {
                                final String lost = splits[i - 1].trim();

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCallBack != null) {
                                            mCallBack.lossRate(lost);
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }
                }


            }

            try {
                final float speedTotal = (totalSize * 8 * 1000) / (totalTime * 1024 * 1024);
                if (speedTotal > 0) {
                    BigDecimal b = new BigDecimal(speedTotal);
                    final float sp = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallBack != null) {
                                mCallBack.onFinished(sp, "Mb/s");
                            }
                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //add lost


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void pingConnection() {
        if (mCount < 1 || mInterval < 0) {
            return;
        }

        StringBuilder comment = new StringBuilder();
        comment.append("ping").append(" -c ").append(mCount).append(" -i ")
                .append(mInterval).append(" ").append(targetIp);

        //ping -c 10 -i 2.5 172.20.144.104
        Log.d("test", "SpeedTest cmd=" + comment.toString());
        try {
            Process process = Runtime.getRuntime().exec(comment.toString());
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
            int totalSize = 0;
            float totalTime = 0;
            int packageSize = 0;

            String line;
            while (!isExit && (line = reader.readLine()) != null) {
                Log.d("test", "SpeedTest pingConnection line=" + line);

                if (!TextUtils.isEmpty(line) && line.contains("ttl")) {
                    try {
                        String[] splits = line.split(" ");

                        if (splits.length > 0) {
                            for (int i = 0; i < splits.length; i++) {
                                String item = splits[i];

                                if (item.equals("bytes") && i > 0) {
                                    String size = splits[i - 1];
                                    packageSize = Integer.parseInt(size);

                                    totalSize = totalSize + packageSize;

                                    Log.d("test", "packageSize=" + packageSize + "  totalSize=" + totalSize);

                                } else if (item.startsWith("icmp_seq=")) {
                                    String seqStr = item.substring("icmp_seq=".length());
                                    Log.d("test", "icmp_seq=" + seqStr);

                                    int seq = Integer.parseInt(seqStr);


                                    final float rate = seq * 100.0f / mCount;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mConnectCallback != null) {
                                                mConnectCallback.onProgress(rate, "%");
                                            }
                                        }
                                    });

                                } else if (item.startsWith("time=")) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mConnectCallback != null) {
                                                mConnectCallback.onResult(0, "ping success");
                                            }
                                        }
                                    });
                                }


                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (!TextUtils.isEmpty(line) && line.contains("packet loss")) {
                    String[] splits = line.split(" ");
                    if (splits.length > 0) {

                        for (int i = 0; i < splits.length; i++) {
                            if (splits[i].equals("packet") && i > 0) {
                                final String lost = splits[i - 1].trim();

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mConnectCallback != null) {
                                            mConnectCallback.lossRate(lost);
                                        }

                                        if (lost.contains("100")) {
                                            if (mConnectCallback != null) {
                                                mConnectCallback.onResult(-1, "100%");
                                            }
                                        }

                                    }
                                });
                                break;
                            }
                        }
                    }
                } else if (!TextUtils.isEmpty(line) && line.contains("Destination")) {
                    String[] splits = line.split(" ");
                    if (splits.length > 0) {
                        for (int i = 0; i < splits.length; i++) {
                            String item = splits[i];
                            if (item.startsWith("icmp_seq=")) {
                                String seqStr = item.substring("icmp_seq=".length());
                                Log.d("test", "icmp_seq=" + seqStr);

                                int seq = Integer.parseInt(seqStr);

                                final float rate = seq * 100.0f / mCount;
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mConnectCallback != null) {
                                            mConnectCallback.onResult(-1, rate + "%");
                                        }
                                    }
                                });
                                break;
                            }
                        }
                    }
                }//Destination


            }


            final float speedTotal = (totalSize * 8 * 1000) / (totalTime * 1024 * 1024);
            if (speedTotal > 0) {
                try {
                    BigDecimal b = new BigDecimal(speedTotal);
                    final float sp = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mConnectCallback != null) {
                                mConnectCallback.onFinished(sp, "Mb/s");
                            }
                        }
                    });
                    //add lost
                } catch (Exception e) {
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
