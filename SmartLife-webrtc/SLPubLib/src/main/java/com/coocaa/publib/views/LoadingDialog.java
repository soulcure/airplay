package com.coocaa.publib.views;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.R;

public class LoadingDialog extends Dialog {

	private ImageView mRotateView;
	private TextView mDesc;
	private RotateAnimation mAnim;

	public LoadingDialog(Context context) {
		super(context, R.style.LoadingDialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		init();
	}

	private void init() {
		setContentView(R.layout.common_dialog_loading_layout);
		
		mRotateView = (ImageView) findViewById(R.id.iv_route);
		mRotateView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		mDesc = (TextView) findViewById(R.id.detail_tv);
		mDesc.setText(getContext().getResources().getString(R.string.loading_tip));
	
		initAnim();		
	}

	private void initAnim() {
		// mAnim = new RotateAnimation(360, 0, Animation.RESTART, 0.5f,
		// Animation.RESTART, 0.5f);
		mAnim = new RotateAnimation(0, 360, Animation.RESTART, 0.5f,
				Animation.RESTART, 0.5f);
		mAnim.setDuration(2000);
		mAnim.setRepeatCount(Animation.INFINITE);
		mAnim.setRepeatMode(Animation.RESTART);
		//mAnim.setStartTime(Animation.START_ON_FIRST_FRAME);
	}

	@Override
	public void show() {// 在要用到的地方调用这个方法
		mRotateView.startAnimation(mAnim);
		super.show();
	}

	@Override
	public void dismiss() {
		mAnim.cancel();
		super.dismiss();
	}

	@Override
	public void setTitle(CharSequence title) {
		if (TextUtils.isEmpty(title)) {
			mDesc.setVisibility(View.GONE);
		} else {
			mDesc.setVisibility(View.VISIBLE);
			mDesc.setText(title);
		}
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getContext().getString(titleId));
	}

	public static void dismissDialog(LoadingDialog loadingDialog) {
		if (null == loadingDialog) {
			return;
		}
		loadingDialog.dismiss();
	}
}