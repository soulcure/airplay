package com.coocaa.tvpi.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.container.BaseFooter;

/**
 * Created by Administrator on 2016/3/21.
 */
public class CustomFooter extends BaseFooter {
    private Context context;
    private int rotationSrc;
    private TextView footerTitle;
    private ProgressBar footerProgressbar;

    public CustomFooter(Context context) {
        this(context, R.drawable.progressbar_yellow_circle);
    }

    public CustomFooter(Context context, int rotationSrc) {
        this.context = context;
        this.rotationSrc = rotationSrc;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.custom_footer, viewGroup, true);
        footerTitle = (TextView) view.findViewById(R.id.custom_footer_title);
        footerProgressbar = (ProgressBar) view.findViewById(R.id.custom_footer_progressbar);
        footerProgressbar.setIndeterminateDrawable(ContextCompat.getDrawable(context, rotationSrc));
        return view;
    }

    @Override
    public void onPreDrag(View rootView) {
    }

    @Override
    public void onDropAnim(View rootView, int dy) {
    }

    @Override
    public void onLimitDes(View rootView, boolean upORdown) {
        if (upORdown) {
            footerTitle.setText("松开载入更多");
        } else {
            footerTitle.setText("查看更多");
        }
    }

    @Override
    public void onStartAnim() {
        footerTitle.setVisibility(View.INVISIBLE);
        footerProgressbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishAnim() {
        footerTitle.setText("查看更多");
        footerTitle.setVisibility(View.VISIBLE);
        footerProgressbar.setVisibility(View.INVISIBLE);
    }
}