package com.coocaa.tvpi.module.app.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coocaa.tvpilib.R;

import static com.coocaa.smartscreen.data.app.AppModel.STATE_INSTALLED;
import static com.coocaa.smartscreen.data.app.AppModel.STATE_INSTALLING;
import static com.coocaa.smartscreen.data.app.AppModel.STATE_UNINSTALL;

public class AppStateButton extends FrameLayout {

    private TextView text;
    private ProgressBar progressBar;

    public AppStateButton(Context context) {
        this(context, null, 0);
    }

    public AppStateButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppStateButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_app_state_button, this, true);
        text = findViewById(R.id.tvState);
        progressBar = findViewById(R.id.progressBar);
    }

    public void setState( int state) {
        switch (state) {
            case STATE_UNINSTALL:
                text.setText("安装");
                text.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                break;
            case STATE_INSTALLED:
                text.setText("打开");
                text.setVisibility(VISIBLE);
                progressBar.setVisibility(GONE);
                break;
            case STATE_INSTALLING:
                text.setVisibility(GONE);
                progressBar.setVisibility(VISIBLE);
                break;
            default:
                break;
        }
    }
}
