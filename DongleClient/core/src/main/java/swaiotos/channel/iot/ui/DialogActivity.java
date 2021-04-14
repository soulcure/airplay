package swaiotos.channel.iot.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coocaa.sdk.entity.IMMessage;

import swaiotos.channel.iot.entity.VersionCheck;


public class DialogActivity extends Activity {

    TextView textView;
    TextView cancelBtn;
    TextView confirmButton;


    private IMMessage imMessage;
    private static VersionCheck versionCheck;


    public static void showDialog(Context context, String msg, IMMessage imMessage, VersionCheck vc) {
        if (versionCheck != null)
            return;
        versionCheck = vc;
        Intent intent = new Intent(context, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("msg", msg);
        intent.putExtra("imMessage", imMessage);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DialogView view = new DialogView(this);
        setContentView(view);
        setFinishOnTouchOutside(false);

        textView = view.textView;
        imMessage = getIntent().getParcelableExtra("imMessage");
        String msg = getIntent().getStringExtra("msg");

        if (!TextUtils.isEmpty(msg)) {
            textView.setText(msg);
        }

        cancelBtn = view.cancelBtn;
        confirmButton = view.confirmButton;

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (versionCheck != null) {
                    versionCheck.sendConfirmToTarget(imMessage, IMMessage.TYPE.CONFIRM);
                    versionCheck = null;
                }
                Log.e("yao", "confirmButton--------");
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (versionCheck != null) {
                    versionCheck.sendConfirmToTarget(imMessage, IMMessage.TYPE.CANCEL);
                    versionCheck = null;
                }

                Log.e("yao", "cancelBtn--------");
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        versionCheck = null;
    }
}