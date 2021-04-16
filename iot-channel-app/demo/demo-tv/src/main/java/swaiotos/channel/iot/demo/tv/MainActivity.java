package swaiotos.channel.iot.demo.tv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.SSChannelClient;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public class MainActivity extends SSChannelClient.SSChannelClientActivity {

    private long count = 0;

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView = new TextView(this);
        textView.setTextSize(32);
        setContentView(textView);
        textView.setText("" + count);
    }

    @Override
    protected boolean handleIMMessage(IMMessage message, SSChannel channel) {
        try {
            String content = message.getContent();
            IMMessage reply = IMMessage.Builder.replyTextMessage(message, content + "~" + Math.random() * 100000);
            channel.getIMChannel().send(reply);
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
        return true;
    }
}
