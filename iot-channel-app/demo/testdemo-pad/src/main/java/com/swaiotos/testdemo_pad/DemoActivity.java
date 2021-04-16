package com.swaiotos.testdemo_pad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;
import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;
import swaiotos.channel.iot.ss.session.Session;
import swaiotos.channel.iot.ss.session.SessionManager;

public class DemoActivity extends AppCompatActivity {
    public static final int REQ_CODE = 1;
    private TextView mMyView, mConntectedToView, mContentView;
    private LinearLayout root, buttons;
    public static final String AUTH = "ss-clientID-mobile";

    private Map<String, TextView> mConnectedFromViews = new HashMap<>();
    public static HT mHt = new HT("tv-thread", true);
    public static Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new LinearLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setOrientation(LinearLayout.VERTICAL);

        buttons = new LinearLayout(this);
        buttons.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        buttons.setOrientation(LinearLayout.VERTICAL);
        root.addView(buttons);


        mMyView = new TextView(this);
        root.addView(mMyView);
        mConntectedToView = new TextView(this);
        root.addView(mConntectedToView);


        mContentView = new TextView(this);
        mContentView.setTextSize(24);
        root.addView(mContentView);

        setContentView(root);
        mHt.post(new Runnable() {
            @Override
            public void run() {
                IOTAdminChannel.mananger.open(getApplicationContext(), "swaiotos.channel.iot", new IOTAdminChannel.OpenCallback() {
                    @Override
                    public void onConntected(SSAdminChannel channel) {
                        try {
                            ddd(channel, channel.getSessionManager().getMySession().getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            }
        });


    }

    @Override
    protected void onDestroy() {
        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().removeOnMySessionUpdateListener(mOnMySessionUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().removeServerSessionOnUpdateListener(mOnSessionUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().removeConnectedSessionOnUpdateListener(mOnSessionConnectUpdateListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IOTAdminChannel.mananger.close();
        super.onDestroy();
    }

    private void ddd(SSAdminChannel channel, String id) throws Exception {
        channel.getSessionManager().addOnMySessionUpdateListener(mOnMySessionUpdateListener);

        channel.getSessionManager().addServerSessionOnUpdateListener(mOnSessionUpdateListener);
        channel.getSessionManager().addConnectedSessionOnUpdateListener(mOnSessionConnectUpdateListener);

        final Session target;
        try {
            target = channel.getController().connect(id, 5000);

            Session my = channel.getSessionManager().getMySession();
            sendMessage(my, target, "从手机发的消息！！" + Math.random() * 100000);
            sendMessage(my, target, "zxczxc~~线程不出现");
            initMenu(my, target, channel);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(final Session source, final Session target, final String content) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    IMMessage message = IMMessage.Builder.createTextMessage(source, target, "", "", content);
                    IOTAdminChannel.mananger.getSSAdminChannel().getIMChannel().send(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mHt.post(runnable);
    }


    private void initMenu(final Session my, final Session target, final SSAdminChannel s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttons.removeAllViews();
                mMyView.setText("my@" + my);
                buttons.addView(new ImageButton(DemoActivity.this, false, my, target, s));
                buttons.addView(new ImageButton(DemoActivity.this, true, my, target, s));
                buttons.addView(new VideoButton(DemoActivity.this, false, my, target, s));
                buttons.addView(new VideoButton(DemoActivity.this, true, my, target, s));
                buttons.addView(new ActivityButton(DemoActivity.this, false, my, target, s));
                buttons.addView(new ActivityButton(DemoActivity.this, true, my, target, s));
                buttons.addView(new StreamButton(DemoActivity.this, false, my, target, s, mStreamReceiver));
                buttons.addView(new StreamButton(DemoActivity.this, true, my, target, s, mStreamReceiver));
                buttons.addView(new SyncMessageButton(DemoActivity.this, false, my, target, s));
                buttons.addView(new SyncMessageButton(DemoActivity.this, true, my, target, s));
                buttons.addView(new BindButton(DemoActivity.this, "zxczxc2", s));
                buttons.addView(new ClientVersionButton(DemoActivity.this, target, "ss-clientID-12345", s));
                buttons.addView(new TestActivityButton(DemoActivity.this, target.getId()));

                setContentView(root);
            }
        });
    }

    private SessionManager.OnMySessionUpdateListener mOnMySessionUpdateListener = new SessionManager.OnMySessionUpdateListener() {
        @Override
        public void onMySessionUpdate(final Session mySession) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMyView.setText("my@" + mySession);
                }
            });
        }
    };

    private SessionManager.OnSessionUpdateListener mOnSessionUpdateListener = new SessionManager.OnSessionUpdateListener() {
        @Override
        public void onSessionConnect(final Session session) {
            synchronized (mConnectedFromViews) {
                if (!mConnectedFromViews.containsKey(session.getId())) {
                    final TextView textView = new TextView(DemoActivity.this);
                    mConnectedFromViews.put(session.getId(), textView);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            root.addView(textView);
                            textView.setText("connected from@" + session);
                        }
                    });
                }
            }
        }

        @Override
        public void onSessionUpdate(final Session session) {
            synchronized (mConnectedFromViews) {
                final TextView textView = mConnectedFromViews.get(session.getId());
                if (textView != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("connected from@" + session);
                        }
                    });
                }
            }
        }

        @Override
        public void onSessionDisconnect(Session session) {
            synchronized (mConnectedFromViews) {
                final TextView textView = mConnectedFromViews.get(session.getId());
                if (textView != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            root.removeView(textView);
                        }
                    });
                }
            }
        }
    };

    private SessionManager.OnSessionUpdateListener mOnSessionConnectUpdateListener = new SessionManager.OnSessionUpdateListener() {
        @Override
        public void onSessionConnect(final Session session) {
            try {
                final Session c = IOTAdminChannel.mananger.getSSAdminChannel().getSessionManager().getConnectedSession();
                if (c != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DemoActivity.this, "connected to@" + c, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConntectedToView.setText("connected to@" + session);
                }
            });
        }

        @Override
        public void onSessionUpdate(final Session session) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConntectedToView.setText("connected to@" + session);
                }
            });
        }

        @Override
        public void onSessionDisconnect(final Session session) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConntectedToView.setText("connected to@null");
                }
            });
        }
    };

    private static File releaseAssets(Context context, String asset) {
        File f = new File(context.getFilesDir(), "assets");
        f = FileUtils.copyAssetFile(context, asset, f.getAbsolutePath());
        f.setReadable(true, false);
        return f;
    }

    static class ImageButton extends AppCompatButton implements View.OnClickListener {
        private boolean sse;
        private Session my, target;
        private SSChannel s;

        public ImageButton(Context context, boolean sse, Session my, Session target, SSChannel s) {
            super(context);
            this.my = my;
            this.target = target;
            this.s = s;
            setOnClickListener(this);
            this.sse = sse;
            setText("image@" + (sse ? "sse" : "local"));
        }

        @Override
        public void onClick(View v) {
            File f = releaseAssets(getContext(), "aaa.jpg");
            IMMessage message = IMMessage.Builder.createImageMessage(my, target, AUTH, "ss-clientID-12345", f);
            if (sse) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }
//                                                        .createTextMessage(my, session, "zxczxc~~线程不出现" + UUID.randomUUID().toString());
            try {
                s.getIMChannel().send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class VideoButton extends AppCompatButton implements View.OnClickListener {
        private boolean sse;
        private Session my, target;
        private SSChannel s;

        public VideoButton(Context context, boolean sse, Session my, Session target, SSChannel s) {
            super(context);
            this.my = my;
            this.target = target;
            this.s = s;
            setOnClickListener(this);
            this.sse = sse;
            setText("video@" + (sse ? "sse" : "local"));
        }

        @Override
        public void onClick(View v) {
            File f = releaseAssets(getContext(), "video.mp4");
            IMMessage message = IMMessage.Builder.createVideoMessage(my, target, AUTH, "ss-clientID-12345", f);
            if (sse) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }
//                                                        .createTextMessage(my, session, "zxczxc~~线程不出现" + UUID.randomUUID().toString());
            try {
                s.getIMChannel().send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ActivityButton extends AppCompatButton implements View.OnClickListener {
        private boolean sse;
        private Session my, target;
        private SSChannel s;

        public ActivityButton(Context context, boolean sse, Session my, Session target, SSChannel s) {
            super(context);
            this.my = my;
            this.target = target;
            this.s = s;
            setOnClickListener(this);
            this.sse = sse;
            setText("activity@" + (sse ? "sse" : "local"));
        }

        @Override
        public void onClick(View v) {
            File f = releaseAssets(getContext(), "video.mp4");
            IMMessage message = IMMessage.Builder.createVideoMessage(my, target, AUTH, "ss-clientID-activity1", f);
            if (sse) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }
//                                                        .createTextMessage(my, session, "zxczxc~~线程不出现" + UUID.randomUUID().toString());
            try {
                s.getIMChannel().send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class StreamButton extends AppCompatButton implements View.OnClickListener {
        private boolean sse;
        private Session my, target;
        private SSChannel s;
        private int streamId;
        private IStreamChannel.Receiver receiver;

        public StreamButton(Context context, boolean sse, Session my, Session target, SSChannel s, IStreamChannel.Receiver receiver) {
            super(context);
            this.my = my;
            this.target = target;
            this.s = s;
            this.streamId = streamId;
            setOnClickListener(this);
            this.sse = sse;
            this.receiver = receiver;
            setText("stream@" + (sse ? "sse" : "local"));
        }

        @Override
        public void onClick(View v) {
            IMMessage message = IMMessage.Builder.createTextMessage(my, target, AUTH, "ss-clientID-activity1", "start stream");
            message.putExtra("streamId", String.valueOf(this.streamId));
            try {
                s.getIMChannel().send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private IStreamChannel.Receiver mStreamReceiver = new IStreamChannel.Receiver() {
            @Override
            public void onReceive(byte[] data) {
                receiver.onReceive(data);
            }
        };
    }

    static class SyncMessageButton extends AppCompatButton implements View.OnClickListener {
        private boolean sse;
        private Session my, target;
        private SSChannel s;

        public SyncMessageButton(Context context, boolean sse, Session my, Session target, SSChannel s) {
            super(context);
            this.my = my;
            this.target = target;
            this.s = s;
            setOnClickListener(this);
            this.sse = sse;
            setText("sync-message@" + (sse ? "sse" : "local"));
        }

        @Override
        public void onClick(View v) {
            final IMMessage message = IMMessage.Builder.createTextMessage(my, target,AUTH, "ss-clientID-12345", "sync-message");
            if (sse) {
                message.putExtra(SSChannel.FORCE_SSE, "true");
            }
            mHt.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        final IMMessage reply = s.getIMChannel().sendSync(message, 3000);
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "reply@" + reply.getContent(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    static class BindButton extends AppCompatButton implements View.OnClickListener {
        private String code;
        private SSAdminChannel s;

        public BindButton(Context context, String code, SSAdminChannel s) {
            super(context);
            this.code = code;
            this.s = s;
            setOnClickListener(this);
            setText("bind@" + code);
        }

        @Override
        public void onClick(View v) {
            mHt.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        s.getDeviceAdminManager().startBind("", code, new DeviceAdminManager.OnBindResultListener() {
                            @Override
                            public void onSuccess(String bindCode, final Device device) {
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "bind onSuccess@" + device, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onFail(final String bindCode, final String errorType, final String msg) {
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "bind onFail@" + bindCode, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }, 20000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }


    static class ClientVersionButton extends AppCompatButton implements View.OnClickListener {
        private String clientID;
        private Session target;
        private SSAdminChannel s;

        public ClientVersionButton(Context context, Session target, String clientID, SSAdminChannel s) {
            super(context);
            this.clientID = clientID;
            this.s = s;
            this.target = target;
            setOnClickListener(this);
            setText("getVersion@" + clientID);
        }

        @Override
        public void onClick(View v) {
            mHt.post(new Runnable() {
                         @Override
                         public void run() {
                             String text;
                             try {
                                 int version = s.getController().getClientVersion(target, clientID, 3000);
                                 text = "clientID@" + version;
                             } catch (Exception e) {
                                 e.printStackTrace();
                                 text = e.getMessage();
                             }
                             final String tt = text;
                             uiHandler.post(new Runnable() {
                                 @Override
                                 public void run() {
                                     Toast.makeText(getContext(), tt, Toast.LENGTH_SHORT).show();
                                 }
                             });
                         }
                     }
            );
        }
    }


    static class TestActivityButton extends AppCompatButton implements View.OnClickListener {

        private String sid;

        public TestActivityButton(Context context, String sid) {
            super(context);
            this.sid = sid;
            setOnClickListener(this);
            setText("startTestActivity");
        }

        @Override
        public void onClick(View v) {
            mHt.post(new Runnable() {
                         @Override
                         public void run() {
                             Intent intent = new Intent();
                             intent.setClass(getContext(), TestActivity.class);
                             intent.putExtra("sid", sid);
                             getContext().startActivity(intent);
                         }
                     }
            );
        }
    }


    private IStreamChannel.Receiver mStreamReceiver = new IStreamChannel.Receiver() {
        @Override
        public void onReceive(byte[] data) {
            final String v = new String(data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContentView.setText(v);
                }
            });
        }
    };
}
