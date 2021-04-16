package swaiotos.runtime.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import swaiotos.runtime.base.AppletThread;
import swaiotos.runtime.base.R;


public class ToastUtils {

	static Toast mGlobalToast;
	static TextView mGlobalToastTV;
	static TextView mToastTV;

	private static ToastUtils mInstance;
	private  Context mContext;

	public synchronized static ToastUtils getInstance() {
		if (mInstance == null) {
			mInstance = new ToastUtils();
		}
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
	}

	@SuppressLint("ShowToast")
	public Toast getGlobalToast() {
		if (mGlobalToast == null) {
			//把一个布局变成一个View对象
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View toast_layout = inflater.inflate(R.layout.custom_toast, null);
			mGlobalToastTV = toast_layout.findViewById(R.id.toast_tv);
			mGlobalToast = new Toast(mContext);
			//把获取到的View对象作为setView的参数
			mGlobalToast.setView(toast_layout);
			mGlobalToast.setGravity(Gravity.TOP, 0, 0);
			mGlobalToast.setDuration(Toast.LENGTH_SHORT);

			/*try {
				Object mTN ;
				mTN = getField(mGlobalToast, "mTN");
				if (mTN != null) {
					Object mParams = getField(mTN, "mParams");
					if (mParams != null
							&& mParams instanceof WindowManager.LayoutParams) {
						WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
						//显示与隐藏动画
						params.windowAnimations = R.style.toast_animation;
						//Toast可点击
						params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
								| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

						//设置viewgroup宽高
						params.width = WindowManager.LayoutParams.MATCH_PARENT; //设置Toast宽度为屏幕宽度
						params.height = WindowManager.LayoutParams.WRAP_CONTENT; //设置高度
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}*/

		}
		return mGlobalToast;
	}



	// Global，用于频繁调用，而不会一直弹出来。
	public  void showGlobalLong(String str) {
		//兼容Android9.0 当前Toast还未消失时弹出下一个Toast，会导致当前Toast消失，
		// 并且下一个Toast也不会显示，之后短时间内弹出的Toast也不会显示
		if (Build.VERSION.SDK_INT >= 28) {
			showToastOnAndroid9(str, Toast.LENGTH_LONG);
		} else {
			getGlobalToast();
			mGlobalToastTV.setText(str);
			mGlobalToast.setDuration(Toast.LENGTH_LONG);
			mGlobalToast.show();
		}
	}


	public void showGlobalLong(int strResId) {
		if (Build.VERSION.SDK_INT >= 28) {
			showToastOnAndroid9(strResId, Toast.LENGTH_LONG);
		} else {
			getGlobalToast();
			mGlobalToastTV.setText(strResId);
			mGlobalToast.setDuration(Toast.LENGTH_LONG);
			mGlobalToast.show();
		}
	}


	public  void showGlobalShort(final String str) {
		if (Build.VERSION.SDK_INT >= 28) {
			showToastOnAndroid9(str, Toast.LENGTH_SHORT);
		} else {
			AppletThread.UI(new Runnable() {
				@Override
				public void run() {
					getGlobalToast();
					mGlobalToastTV.setText(str);
					mGlobalToast.setDuration(Toast.LENGTH_SHORT);
					mGlobalToast.show();
				}
			});
		}
	}

	public void showGlobalShort(final int strResId) {
		if (Build.VERSION.SDK_INT >= 28) {
			showToastOnAndroid9(strResId, Toast.LENGTH_SHORT);
		} else {
			AppletThread.UI(new Runnable() {
				@Override
				public void run() {
					getGlobalToast();
					mGlobalToastTV.setText(strResId);
					mGlobalToast.setDuration(Toast.LENGTH_SHORT);
					mGlobalToast.show();
				}
			});
		}
	}


	private  void showToastOnAndroid9(int strResId,int during){
		showToastOnAndroid9(mContext.getString(strResId),during);
	}

	Toast lastToast;
	private  void showToastOnAndroid9(final String str, final int during){
		AppletThread.UI(new Runnable() {
			@Override
			public void run() {
				if(lastToast != null) {
					lastToast.cancel();
				}
				Toast toast = new Toast(mContext);
				View inflate = LayoutInflater.from(mContext).inflate(R.layout.custom_toast, null);
				TextView tvToast = inflate.findViewById(R.id.toast_tv);
				tvToast.setText(str);
				toast.setView(inflate);
				toast.setGravity(Gravity.TOP, 0, 0);
				toast.setDuration(during);
				toast.show();
				lastToast = toast;
			}
		});
	}

}
