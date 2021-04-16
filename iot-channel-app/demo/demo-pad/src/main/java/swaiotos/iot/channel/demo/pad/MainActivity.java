package swaiotos.iot.channel.demo.pad;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import swaiotos.channel.iot.IOTChannel;
import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.session.Session;

public class MainActivity extends AppCompatActivity {
    public final static int RESULT_CODE_NEWDEVICE = 1;

    public static Handler uiHandler = new Handler(Looper.getMainLooper());

    private LinearLayout root;

    private long count=0;

    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        root = new LinearLayout(this);
//        root.setOrientation(LinearLayout.VERTICAL);
//        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        setContentView(root);



        textView = new TextView(this);

        textView.setTextSize(32);
        setContentView(textView);
        textView.setText("" + count);
        try {
            IOTChannel.mananger.open(this, "swaiotos.channel.iot", new IOTChannel.OpenCallback() {
                @Override
                public void onConntected(SSChannel channel) {
//                    showConnectedSession(channel);
                    start(channel);
                }

                @Override
                public void onError(String s) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showConnectedSession(SSChannel channel) {
        try {
            Session session = channel.getSessionManager().getConnectedSession();
            final TextView textView = new TextView(this);
            textView.setText("connected:" + session.encode());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    root.addView(textView);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(final SSChannel channel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        Session session = channel.getSessionManager().getMySession();
                        Session target = channel.getSessionManager().getConnectedSession();
                        IMMessage message = IMMessage.Builder.createTextMessage(session, target, "", "ss-clientID-demo-tv", UUID.randomUUID().toString());
                        message = channel.getIMChannel().sendSync(message, 5000);

                        count++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("" + count);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
