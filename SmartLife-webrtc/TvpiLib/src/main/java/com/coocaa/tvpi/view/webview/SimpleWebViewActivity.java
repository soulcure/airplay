package com.coocaa.tvpi.view.webview;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.coocaa.publib.base.BaseActionBarActivity;
import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.common.Constants;
import com.coocaa.tvpi.module.base.UnVirtualInputable;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.TvpiClickUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.google.gson.Gson;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import swaiotos.runtime.h5.core.os.H5RunType;

public class SimpleWebViewActivity extends BaseActivity implements UnVirtualInputable {

	private static final String TAG = SimpleWebViewActivity.class.getSimpleName();

	public static final int SUCCUSS = 0;
	public static final int ERROR = 1;

	private CommonTitleBar titleBar;
	private String mUrl;
	private String mCurUrl;
	protected WebView mWebView;
	private Handler mHandler = new Handler();
	private Context mContext;
	private View mErrShow;
	private ProgressBar mTopLoadBar;
	private ProgressBar mCircleProgress;
	private boolean mWebTitleEnable = true;
	private int mLstProgress = 0;
	private int mErrState = SUCCUSS;
	/**
	 * 是否正在运行，在运行则不能进行点击其他的
	 */
	private boolean IS_RUNNING = false;
//
    public static void start(Context context, String url) {
		Intent starter = new Intent(context, SimpleWebViewActivity.class);
		starter.putExtra(Constants.Cordova.url, url);
		context.startActivity(starter);
    }

    //像小程序一样启动
	public static void startAsApplet(Context context, String url) {
		startAsNewStyle(context, url, H5RunType.RUNTIME_NAV_TOP);
	}

	//像h5一样启动
	public static void startAsH5(Context context, String url) {
		startAsNewStyle(context, url, H5RunType.RUNTIME_NAV_FLOAT);
	}

	private static void startAsNewStyle(Context context, String url, String style) {
		Uri uri = Uri.parse(url);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(uri.getScheme()).authority(uri.getAuthority()).path(uri.getPath());
		Set<String> keySet = uri.getQueryParameterNames();
		boolean hasRuntimeParams = false;
		if(keySet != null && !keySet.isEmpty()) {
			for(String key: keySet) {
				builder.appendQueryParameter(key, uri.getQueryParameter(key));
				if("runtime".equals(key)) {
					hasRuntimeParams = true;
				}
			}
		}
		if(!hasRuntimeParams) {
			Map<String, String> runtimeMap = new HashMap<>();
			runtimeMap.put(H5RunType.RUNTIME_NAV_KEY, style);
			builder.appendQueryParameter("runtime", new Gson().toJson(runtimeMap));
		}
		String newUrl = builder.build().toString();
		TvpiClickUtil.onClick(context, newUrl);
	}

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StatusBarHelper.translucent(this);
		StatusBarHelper.setStatusBarLightMode(this);
		setContentView(R.layout.activity_simple_webview);

		mContext = this;
		titleBar = findViewById(R.id.titleBar);
		titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
			@Override
			public void onClick(CommonTitleBar.ClickPosition position) {
				if(position == CommonTitleBar.ClickPosition.LEFT){
					if (mWebView.canGoBack()) {
						mWebView.goBack();
					} else {
						finish();
					}
				}else {
					finish();
				}
			}
		});
//		http://jira.skyoss.com/browse/ZHP-395
	/*	disableCompatibleFitSystemWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// KIKAT以上版本，android系统有个BUG，会导致输入法遮盖WebView的输入框。
//			AndroidBug5497Workaround.assistActivity(this);
			// 但如果调上面的函数和setCompatibleFitSystemWindow();同时调用，会导致WebView部分被遮盖。

			// 既然不能调用setCompatibleFitSystemWindow()，那就只能通过padding_top的方式来避免上部被遮盖。
			View paddingView = findViewById(R.id.padding_top);
			*//*ViewGroup.LayoutParams params = paddingView.getLayoutParams();
			params.height = DimensUtils.getActionBarHeight(this) + DimensUtils.getStatusBarHeight(this);
			paddingView.setLayoutParams(params);*//*
		} else {
			findViewById(R.id.padding_top).setVisibility(View.GONE);
		}

		setRightButton(getString(R.string.close));*/

//		MyApplication.setLogScreenOrientation(SimpleWebViewActivity.this);

		String title = getIntent().getStringExtra(Constants.Cordova.title);
		String url = getIntent().getStringExtra(Constants.Cordova.url);
		int titleResId = getIntent().getIntExtra(Constants.Cordova.titleResId, -1);
		int urlResId = getIntent().getIntExtra(Constants.Cordova.urlResId, -1);

		if (!TextUtils.isEmpty(title)) {
			titleBar.setText(CommonTitleBar.TextPosition.TITLE,title);
			mWebTitleEnable = false;
		}
		if (titleResId != -1) {
			titleBar.setText(CommonTitleBar.TextPosition.TITLE,getResources().getString(titleResId));
			mWebTitleEnable = false;
		}

		//mUrl 没被初始化，才从Intent中取值。
		if (TextUtils.isEmpty(mUrl)) {
			if (!TextUtils.isEmpty(url)) {
				mUrl = url;
			} else if (urlResId != -1) {
				mUrl = getString(urlResId);
			}
		}

		mTopLoadBar = (ProgressBar) findViewById(R.id.progress_bar);
		mCircleProgress = (ProgressBar) findViewById(R.id.circle_progress);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setWebViewClient(mClient);
		mWebView.setWebChromeClient(mChromeClient);
		mWebView.requestFocus();

		WebSettings webSettings = mWebView.getSettings();
		/*webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(true);
		webSettings.setBuiltInZoomControls(false);*/

		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setUseWideViewPort(true);//设置webview推荐使用的窗口
		webSettings.setLoadWithOverviewMode(true);//设置webview加载的页面的模式
		webSettings.setDisplayZoomControls(false);//隐藏webview缩放按钮
		webSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
		webSettings.setAllowFileAccess(true); // 允许访问文件
		webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
		webSettings.setSupportZoom(true); // 支持缩放
		webSettings.setDomStorageEnabled(true);//localStorage、sessionStorage
		webSettings.setSaveFormData(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		initPlugs();

		if (!TextUtils.isEmpty(mUrl)) {
			loadUrl(mUrl);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(TAG); // 统计页面
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(TAG); // 统计页面
	}

	public void setDomTitleEnable(boolean enable) {
		mWebTitleEnable = enable;
	}

	public void loadUrl(String url) {
		Log.d(TAG, "loadUrl: " + mUrl);

		mCurUrl = url;
		mUrl = url;

		mWebView.loadUrl(mUrl);
		startUpdateProgress();
	}

	public void initPlugs() {
		// 对于页面销毁重建，需要重新设置WebView 和重新 addJavascriptInterface。

		/*List<Class<? extends NativePlugin>> pluginClasses = new ArrayList<>();
		pluginClasses.add(NativeContainer.class);
		pluginClasses.add(NativeBusiness.class);
		pluginClasses.add(NativeEnvironment.class);
		pluginClasses.add(NativeMedia.class);


		final FragmentManager fragMng = getSupportFragmentManager();
		for (int i = 0; i < pluginClasses.size(); ++ i) {
			final Class<? extends NativePlugin> pluginClz = pluginClasses.get(i);
			final String tag = NativePlugin.getPluginTag(pluginClz);

			// 先判断插件有没有创建过，如果创建了，就只要再绑定就行。
			Fragment pluginFrag = fragMng.findFragmentByTag(tag);
			NativePlugin plugin;
			if (pluginFrag instanceof  NativePlugin) {
				plugin = (NativePlugin) pluginFrag;// Plugin已经创建过了。
			} else {
				plugin = NativePlugin.newInstance(pluginClz, mUrl);
			}
			if (plugin == null) {
				continue;
			}

			plugin.attachTo(mWebView);
		}*/
	}

	@Override
	public boolean onLoadingCancelled(DialogInterface dialog) {
		dialog.dismiss();
		mWebView.stopLoading();
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			finish();
		}
		return true;
	}

	@Override
	public boolean onBackClicked() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		}else {
			ActivityManager activityManager = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
			List<ActivityManager.RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
//			Logger.e(TAG, "tasksInfo:"+tasksInfo.size());
			if(tasksInfo.size() <= 1) {
				super.onBackClicked();
			} else {
				finish();
			}
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopUpdateProgress();

		//清空所有Cookie
		CookieSyncManager.createInstance(this);  //Create a singleton CookieSyncManager within a context
		CookieManager cookieManager = CookieManager.getInstance(); // the singleton CookieManager instance
		cookieManager.removeAllCookie();// Removes all cookies.
		CookieSyncManager.getInstance().sync(); // forces sync manager to sync now

		mWebView.setWebChromeClient(null);
		mWebView.setWebViewClient(null);
		mWebView.getSettings().setJavaScriptEnabled(false);
		mWebView.clearCache(true);
	}

	private void startUpdateProgress() {
		//showLoading();
		if (mTopLoadBar.getVisibility() == View.VISIBLE) {
			return ;
		}

		mTopLoadBar.setVisibility(View.VISIBLE);
		mCircleProgress.setVisibility(View.VISIBLE);
		mTopLoadBar.setProgress(0);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				int curProgress = mTopLoadBar.getProgress();
				int targetProgress = 10;
				if (curProgress > mLstProgress * 10) {
					targetProgress = 2;
				}

				boolean seized = false;
				if (targetProgress + curProgress >= 950 &&
						mTopLoadBar.getVisibility() == View.VISIBLE ) {
					mTopLoadBar.setProgress(950); //卡住不动。
					seized = true;
				} else {
					mTopLoadBar.incrementProgressBy(targetProgress);
				}

				if (mTopLoadBar.getVisibility() == View.VISIBLE && curProgress < 1000) {
					if (seized) {
						mHandler.postDelayed(this, 200);
					} else {
						mHandler.postDelayed(this, 10);
					}
				}
			}
		}, 10);
	}

	private void stopUpdateProgress() {
		//dismissLoading();
		mTopLoadBar.setProgress(1000);
		mTopLoadBar.setVisibility(View.GONE);
		mCircleProgress.setVisibility(View.GONE);
		mTopLoadBar.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
	}

	private WebChromeClient mChromeClient = new WebChromeClient() {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			Log.d(TAG, "mChromeClient.onReceivedTitle: " + title);
			if (!mWebTitleEnable) {
				return ;
			}
			if (mErrState != SUCCUSS) {
				return ;
			}
			if (TextUtils.isEmpty(title)) {
				return ;
			}
			if (title.contains(".zhijianyaokong.com/")) {
				return ;
			}
			if (title.contains(".doubimeizhi.com/")) {
				return ;
			}

			titleBar.setText(CommonTitleBar.TextPosition.TITLE,  title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			Log.d(TAG, "mChromeClient.onProgressChanged: " + newProgress);
			mLstProgress = newProgress;
		}
	};

	private WebViewClient mClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url,
								  android.graphics.Bitmap favicon) {
			Log.d(TAG, "onPageStarted.url: " + url);
			startUpdateProgress();

			mErrState = SUCCUSS;
			if (mErrShow != null) {
				mErrShow.setVisibility(View.GONE);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "onPageFinished." + url);
			stopUpdateProgress();

			if (mErrState != SUCCUSS) {
				// 加载没成功。
				if (mErrShow == null) {
					ViewStub stub = (ViewStub) findViewById(R.id.error_show);
					mErrShow = stub.inflate();
					mErrShow.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if (!TextUtils.isEmpty(mCurUrl)) {
								mWebView.reload();
								// mWebView.loadUrl(mCurUrl);
							}
						}
					});
					mErrShow.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							onBackClicked();
						}
					});
				}
				mErrShow.setVisibility(View.VISIBLE);
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
            //对页面的html值进行打印出来，对错误码进行判断
            view.loadUrl("javascript:window.NativeEnvironment.showSource('<head>'+" + "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
			Log.d(TAG, "onReceivedError." + failingUrl);
			Log.d(TAG, "onReceivedError." + errorCode + ", " + description);
			stopUpdateProgress();

			mErrState = ERROR;
			view.stopLoading();
			view.setVisibility(View.GONE);
			//about:blank
			//view.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
			view.loadUrl("javascript:document.body.innerHTML=\"\"");
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			/*// 判断是否是支付的url，如果是则进行支付
			if (!TextUtils.isEmpty(url) && url.indexOf(ConstantsUrl.URL_APPLIANCE_CLEAR_PAY) != -1) {
				retWechatPayForAndroid(url);
				return true;
			}*/

			Log.e(TAG, "访问的url地址：" + url);
			if (url.startsWith("weixin://wap/pay?")) {
				Log.d(TAG, "shouldOverrideUrlLoading: 启动微信");
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			} else if (parseScheme(url)) {
				Log.d(TAG, "shouldOverrideUrlLoading: 启动支付宝");
				try {
					Intent intent;
					intent = Intent.parseUri(url,
							Intent.URI_INTENT_SCHEME);
					intent.addCategory("android.intent.category.BROWSABLE");
					intent.setComponent(null);
					// intent.setSelector(null);
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}/* else {
				view.loadUrl(url);
			}*/

			/*WebView.HitTestResult hitTest = view.getHitTestResult();
			int hitType = -1;
			if (hitTest != null) {
				hitType = hitTest.getType();
			}
			Log.d(TAG, "shouldOverrideUrlLoading." + url + ", hitType: " + hitType);
			if (hitType == 0) {
				Log.d(TAG, "is 302 redirect~");
				mCurUrl = url;
				return false;
			}*/

			view.loadUrl(url);
			mCurUrl = url;

			//重新load
			IS_RUNNING = false;
			return true;
		}
	};

	public boolean parseScheme(String url) {
		if (url.contains("platformapi/startapp")) {
			return true;
		} else if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
				&& (url.contains("platformapi") && url.contains("startapp"))) {
			return true;
		} else if (url.contains("alipay")) {
			return true;
		} else {
			return false;
		}
	}
}
