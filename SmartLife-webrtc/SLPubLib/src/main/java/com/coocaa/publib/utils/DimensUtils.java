package com.coocaa.publib.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class DimensUtils {
	private static int sDeviceWidth = -1;
	private static int sDeviceHeight = -1;
	private static int sDeviceRealHeight = -1;

	public static int dp2Px(Context context, float dp) {
		if (context == null)
			return -1;
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	public static int px2Dp(Context context, float px) {
		if (context == null)
			return -1;
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	public static int getActionBarHeight(Context ctx) {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv,
				true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					ctx.getResources().getDisplayMetrics());
		}
		return actionBarHeight;
	}

	public static int getStatusBarHeight(Context ctx) {
		int result = 0;
		int resourceId = ctx.getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = ctx.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static int getDeviceHeight(Context ctx) {
		if (sDeviceHeight == -1) {
			WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);

			sDeviceWidth = dm.widthPixels;
			sDeviceHeight = dm.heightPixels;
		}
		return sDeviceHeight;
	}

	public static int getDeviceRealHeight(Context ctx) {
		if (sDeviceRealHeight == -1) {
			WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getRealMetrics(dm);
			sDeviceRealHeight = dm.heightPixels;
		}
		return sDeviceRealHeight;
	}

	public static int getDeviceWidth(Context ctx) {
		if (sDeviceWidth == -1) {
			WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);

			sDeviceWidth = dm.widthPixels;
			sDeviceHeight = dm.heightPixels;
		}
		return sDeviceWidth;
	}

	/**
	 * sp转px
	 *
	 * @param spValue sp值
	 * @return px值
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
