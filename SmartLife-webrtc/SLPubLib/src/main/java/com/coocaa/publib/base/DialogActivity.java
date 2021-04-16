package com.coocaa.publib.base;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.coocaa.publib.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


/**
 * @ClassName DialogActivity
 * @Description 改版连接设备的DialogFragment为透明Activity
 */
public class DialogActivity extends AppCompatActivity {

    protected RelativeLayout contentRl;
    private View backgroundView;
    private ObjectAnimator fadeIn;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.dialog_enter, R.anim.dialog_out);
        setContentView(R.layout.dialog_activity);

        backgroundView = findViewById(R.id.dialog_activity_background);
        View blankView = findViewById(R.id.dialog_activity_blank_rl);
        contentRl = findViewById(R.id.dialog_activity_content);

        backgroundView.setVisibility(View.VISIBLE);
        fadeIn = ObjectAnimator.ofFloat(backgroundView, "alpha", 0f, 1f);
        fadeIn.setDuration(300);

        blankView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundView.setVisibility(View.GONE);
                finish();
            }
        });

        backgroundView.postDelayed(new Runnable() {
            @Override
            public void run() {
                fadeIn.start();
            }
        },200);
    }

    @Override
    public void finish() {
        super.finish();
        backgroundView.setVisibility(View.GONE);
        overridePendingTransition(R.anim.push_left_in, R.anim.dialog_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fadeIn.cancel();
    }
}
