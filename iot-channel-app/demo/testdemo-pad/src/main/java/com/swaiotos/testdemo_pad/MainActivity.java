package com.swaiotos.testdemo_pad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swaiotos.testdemo_pad.message.IMessage;
import com.swaiotos.testdemo_pad.message.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.im.IMMessageCallback;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.session.Session;

public class MainActivity extends AppCompatActivity {

    private Context mContext = null;
    private LinearLayout ll = null;
    private SSAdminChannel curChannel = null;
    private TextView tv = null;
    private TextView tv2 = null;

    private int sendTimes = 0;
    private int sendSuccess = 0;
    private int sendFaile = 0;
    private boolean resetFlag = false;
    public static boolean isCloud = false;
    private Button btn1,btn2,btn4;
    private List<Device> devices;
    private DeviceStatusChange listener;
    int sucConnect = 0;
    int failConnect = 0;
    int sucDisconnect = 0;
    int failDisconnect = 0;


    @SuppressLint("HandlerLeak")
    Handler myHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
             updateData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        resetFlag = false;
        ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.VERTICAL);

        tv2 = new TextView(this);
        tv2.setWidth(200);
        tv2.setHeight(100);
        tv2.setText("通道测试");
        tv2.setTextSize(35);

        tv = new TextView(this);
        tv.setWidth(200);
        tv.setHeight(350);
        tv.setText("统计中...");
        tv.setTextSize(35);

        btn1 = new Button(this);
        btn1.setText("iot-channel云端通道测试");
        btn1.setHeight(100);
        btn1.setWidth(300);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("c-test","----------------iot-channel---cloud----------");
                resetFlag = false;
                sendTimes = 0;
                sendSuccess = 0;
                sendFaile = 0;
                sucConnect = 0;
                failConnect = 0;
                sucDisconnect = 0;
                failDisconnect = 0;
                isCloud = true;
                tv2.setText("云端通道测试");
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                btn4.setVisibility(View.GONE);
                start(curChannel);
            }
        });

        btn2= new Button(this);
        btn2.setText("iot-channel本地通道测试");
        btn2.setHeight(100);
        btn2.setWidth(300);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("c-test","----------------iot-channel---local----------");
                resetFlag = false;
                isCloud = false;
                sendTimes = 0;
                sendSuccess = 0;
                sendFaile = 0;
                sucConnect = 0;
                failConnect = 0;
                sucDisconnect = 0;
                failDisconnect = 0;
                tv2.setText("本地通道测试");
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                btn4.setVisibility(View.GONE);
                start(curChannel);
            }
        });

        Button btn3 = new Button(this);
        btn3.setText("重置");
        btn3.setHeight(100);
        btn3.setWidth(300);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFlag = true;
                sendTimes = 0;
                sendSuccess = 0;
                sendFaile = 0;
                sucConnect = 0;
                failConnect = 0;
                sucDisconnect = 0;
                failDisconnect = 0;
                tv.setText("");
                tv2.setText("通道测试");
                btn2.setVisibility(View.VISIBLE);
                btn1.setVisibility(View.VISIBLE);
                btn4.setVisibility(View.VISIBLE);
            }
        });

        btn4= new Button(this);
        btn4.setText("iot-channel连接断连测试");
        btn4.setHeight(100);
        btn4.setWidth(300);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("c-test","----------------iot-channel---local----------");
                resetFlag = false;
                isCloud = false;
                sendTimes = 0;
                sendSuccess = 0;
                sendFaile = 0;
                sucConnect = 0;
                failConnect = 0;
                sucDisconnect = 0;
                failDisconnect = 0;
                tv2.setText("连接断连测试");
                btn1.setVisibility(View.GONE);
                btn2.setVisibility(View.GONE);
                btn4.setVisibility(View.GONE);
                testConnnectStatus();
            }
        });


        ll.addView(tv2);
        ll.addView(tv);
        ll.addView(btn1);
        ll.addView(btn2);
        ll.addView(btn4);
        ll.addView(btn3);
        setContentView(ll);
        initChannel();
    }

    private void updateData() {
        int percent = 0;
        try {
            percent = (int)(((double)sendSuccess / sendTimes) * 100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuffer sb = new StringBuffer();
        sb.append("发送：" + sendTimes + "\n");
        sb.append("成功：" + sendSuccess + "\n");
        sb.append("失败：" + sendFaile + "\n");
        sb.append("成功率：" + percent + "%");
        tv.setText(sb.toString());
    }

    private void initChannel() {
        try {
            Log.d("c-test", " initChannel");

            IOTAdminChannel.mananger.open(mContext, "swaiotos.channel.iot", new IOTAdminChannel.OpenCallback() {
                @Override
                public void onConntected(SSAdminChannel channel) {
                    Log.d("c-test", " channel success");
                    curChannel = channel;
                    try {
                        devices = curChannel.getDeviceManager().getDevices();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onError(String s) {
                    Log.d("c-test", " channel onError");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testConnnectStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (devices == null || devices.size() <= 0) {
                    Toast.makeText(getApplicationContext(),"测试pad没有绑定过TV，不能测试连接断开测试",Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("c-test","devices.get(0).getLsid():"+devices.get(0).getLsid());
                while (!resetFlag) {
                    testConnectAndDisconnect();
                }

            }
        }).start();
    }

    private void testConnectAndDisconnect() {
        int times = 10000;

        for (int i = 0; i < times; i++) {
            try {
                Session session = curChannel.getController().connect(devices.get(0).getLsid(), 5000);
                sucConnect++;
                try {
                    curChannel.getController().disconnect(session);
                    sucDisconnect++;
                } catch (Exception e) {
                    e.printStackTrace();
                    failDisconnect++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                failConnect++;
            }

            if (resetFlag)
                break;

            final String content = "connect:[ success count:" + sucConnect + " failure count:" + failConnect + "]\n"
                    + "disconnect:[ success coun:" + sucDisconnect + " failure count:" + failDisconnect + "]";
            Log.d("c-test","-----:"+content);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText(content);
                }
            });
        }

    }

    private void start(final SSChannel channel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.d("c-test", "start " + (channel != null));
                    List<TestTask> list = getTestTasks(channel);
                    for (TestTask task : list) {
                        if(resetFlag)
                            return;
                        long delayTime = task.delayTime;
                        Log.d("c-test", "start delaytime:" + task.delayTime);
                        try {
                            Thread.sleep(delayTime);
                        } catch (Exception e) {
                        }
                        sendTimes++;
                        myHandle.sendEmptyMessage(0);
                        try {
                            Log.d("c-test", "IMMessage id:" + task.message.getId());
                            IMMessage msg = channel.getIMChannel().sendSync(task.message, new IMMessageCallback() {
                                @Override
                                public void onStart(IMMessage message) {
                                    Log.d("c-test", "sendSync  onStart");
                                }

                                @Override
                                public void onProgress(IMMessage message, int progress) {
                                    Log.d("c-test", "sendSync  onProgress");
                                }

                                @Override
                                public void onEnd(IMMessage message, int code, String info) {
                                    Log.d("c-test", "sendSync onEnd  code:" + code);
                                    if(code == 0) {
                                        sendSuccess++;
                                    } else {
                                        sendFaile++;
                                    }
                                }
                            }, 20000);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public List<TestTask> getTestTasks(SSChannel channel) {
        List<TestTask> list = new ArrayList<>();
        MessageManager messageManager = (MessageManager) IMessage.MSG;
        messageManager.setContext(getApplicationContext());
        IMMessage message = messageManager.getTestMessage(channel);

        //小于1s
        for (int i = 0; i < 1000; i++) {
            TestTask tt = new TestTask();
            tt.message = IMessage.MSG.getTestMessage(channel);
            tt.delayTime = (long) (Math.random() * 1000);
            list.add(tt);
        }

        //1s~5s
        for (int i = 0; i < 400; i++) {
            TestTask tt = new TestTask();
            tt.message = IMessage.MSG.getTestMessage(channel);
            tt.delayTime = (long) (((int) (Math.random() * 4) + 1) * 1000);
            list.add(tt);
        }

        //5s~10s
        for (int i = 0; i < 300; i++) {
            TestTask tt = new TestTask();
            tt.message = IMessage.MSG.getTestMessage(channel);
            tt.delayTime = (long) (((int) (Math.random() * 5) + 5) * 1000);
            list.add(tt);
        }

        //10s~30s
        for (int i = 0; i < 200; i++) {
            TestTask tt = new TestTask();
            tt.message = IMessage.MSG.getTestMessage(channel);
            tt.delayTime = (long) (((int) (Math.random() * 20) + 10) * 1000);
            list.add(tt);
        }

        //30s~60s
        for (int i = 0; i < 100; i++) {
            TestTask tt = new TestTask();
            tt.message = message;
            tt.delayTime = (long) (((int) (Math.random() * 30) + 30) * 1000);
            list.add(tt);
        }

        //60s~300s
        for (int i = 0; i < 30; i++) {
            TestTask tt = new TestTask();
            tt.message = message;
            tt.delayTime = (long) (((int) (Math.random() * 240) + 60) * 1000);
            list.add(tt);
        }

        //300s~1800s
        for (int i = 0; i < 5; i++) {
            TestTask tt = new TestTask();
            tt.message = message;
            tt.delayTime = (long) (((int) (Math.random() * 1500) + 300) * 1000);
            list.add(tt);
        }

        //1800s~3600s
        for (int i = 0; i < 3; i++) {
            TestTask tt = new TestTask();
            tt.message = message;
            tt.delayTime = (long) (((int) (Math.random() * 1800) + 1800) * 1000);
            list.add(tt);
        }

        //3600s~10800s
        for (int i = 0; i < 1; i++) {
            TestTask tt = new TestTask();
            tt.message = message;
            tt.delayTime = (long) (((int) (Math.random() * 7200) + 3600) * 1000);
            list.add(tt);
        }

        return list;
    }

    class TestTask {
        public int messageID;
        public IMMessage message;
        public long delayTime;
    }

    private class DeviceStatusChange implements DeviceAdminManager.OnDeviceChangedListener {

        @Override
        public void onDeviceOffLine(Device device) {
            Log.d("c_test","onDeviceOffLine-----");
            if (device != null && !TextUtils.isEmpty(device.getLsid())
                    && devices != null && devices.size() > 0
                    && devices.get(0).getLsid().equals(device.getLsid())) {
                Log.d("c_test","unbind-----");
            }
        }

        @Override
        public void onDeviceOnLine(Device device) {
            Log.d("c_test","onDeviceOnLine-----");
        }

        @Override
        public void onDeviceUpdate(Device device) {
            Log.d("c_test","onDeviceOnLine-----");
        }
    }

}
