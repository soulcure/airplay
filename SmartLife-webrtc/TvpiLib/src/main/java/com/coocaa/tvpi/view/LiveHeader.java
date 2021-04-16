package com.coocaa.tvpi.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.coocaa.tvpilib.R;
import com.liaoinstan.springview.container.BaseHeader;

/**
 * @ClassName CustomHeader2
 * @Description 新年loading动画
 * @User heni
 * @Date 2019/1/22
 */
public class LiveHeader extends BaseHeader {
    private Context context;
    private ProgressBar progressBar;
    private int rotationSrc;

    public LiveHeader(Context context){
        this.context = context;
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.live_header, viewGroup, true);
        progressBar = (ProgressBar) view.findViewById(R.id.load_progress);
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
    }

    @Override
    public void onStartAnim() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishAnim() {
        progressBar.setVisibility(View.INVISIBLE);
    }

}
