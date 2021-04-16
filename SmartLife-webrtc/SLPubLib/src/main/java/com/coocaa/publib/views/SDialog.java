package com.coocaa.publib.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coocaa.publib.R;


/**
 * <p>
 * 自定义Dialog
 * </p>
 * 确定、取消双按钮，确定单按钮两种模式，点击外部可以实现关闭弹出窗
 */
public class SDialog extends AbsDialog implements OnClickListener {
	public final static int TYPE_1_BTN = 1;//只显示一个按钮的样式
	public final static int TYPE_2_BTN = 2;//显示两个按钮的样式
	private Context context;

	/**
	 * 标题view所在的布局包含下划线
	 */
	private RelativeLayout titleLayout;
	/**
	 * 标题view
	 */
	private TextView title;

	/**
	 * 内容view所在的SV
	 */
	private ScrollView contentSV;
	/**
	 * 内容view
	 */
	private TextView content;
	/**
	 * 提示view
	 */
	private TextView tips;
	/**
	 * 内容图片
	 */
	private ImageView contentImg;
	/**
	 * 单按钮确认键view
	 */
	private TextView ok;
	/**
	 * 双按钮view
	 */
	private LinearLayout doubleBtn;
	/**
	 * 双按钮左键view
	 */
	private TextView btnLeft;
	/**
	 * 双按钮右键view
	 */
	private TextView btnRight;
	/**
	 * 事件
	 */
	private SDialogListener listener;
	/**
	 * 双按钮响应
	 */
	private SDialog2Listener listener2;
	/**
	 * 标题id
	 */
	private int titleId = 0;
	/**
	 * 标题
	 */
	private String strTitle = null;
	/**
	 * 内容id
	 */
	private int contentId = 0;
	/**
	 * ok按钮内容
	 */
	private String strOk = null;
	/**
	 * 内容
	 */
	private String strContent = null;
	/**
	 * tips
	 */
	private String strTips = null;
	/**
	 * 图片
	 */
	private int imgRes = 0;
	/**
	 * 左按钮id
	 */
	private int btnLeftId = 0;
	/**
	 * 右按钮id
	 */
	private int btnRightId = 0;
	/**
	 * 左按钮String
	 */
	private String btnLeftString = null;
	/**
	 * 右按钮String
	 */
	private String btnRightString = null;

	/**
	 * 类型
	 */
	private int type;
	/***/
	private int lResId;
	/***/
	private int rResId;


	private boolean showTitle = true;
	private boolean clickcancel = true; // 是否点击取消

	/**
	 * 自定义Dialog（确认按钮）
	 *
	 * @param context   主题id
	 * @param titleId   标题id：0为使用默认标题
	 * @param contentId 内容id
	 * @param listener  监听事件
	 */
	public SDialog(Context context, int titleId, int contentId, SDialogListener listener){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_1_BTN;
		this.titleId = titleId;
		this.contentId = contentId;
		this.listener = listener;

	}

	/**
	 * 自定义Dialog(两个按钮)//add wll
	 *
	 * @param context
	 * @param titleId    标题id：0为使用默认标题
	 * @param strContent 内容
	 * @param listener2  监听事件
	 */
	public SDialog(Context context, int titleId, String strContent, SDialog2Listener listener2, int type){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = type;
		this.titleId = titleId;
		this.strContent = strContent;
		this.listener2 = listener2;

	}

	/**
	 * 自定义Dialog(两个按钮)//add wll
	 *
	 * @param context
	 * @param showTitle  标题id：0为使用默认标题
	 * @param strContent 内容
	 * @param listener2  监听事件
	 */
	public SDialog(Context context, int titleId, boolean showTitle, String strContent, SDialog2Listener listener2, int type){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = type;
		this.titleId = titleId;
		this.showTitle = showTitle;
		this.strContent = strContent;
		this.listener2 = listener2;

	}

	/**
	 * 自定义Dialog（确认按钮）//add wll
	 *
	 * @param context
	 * @param titleId   标题id：0为使用默认标题
	 * @param contentId 内容id
	 * @param listener  监听事件
	 */
	public SDialog(Context context, int titleId, int contentId, SDialogListener listener, int type){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = type;
		this.titleId = titleId;
		this.contentId = contentId;
		this.listener = listener;

	}

	/**
	 * 自定义Dialog（确认按钮）
	 *
	 * @param context
	 * @param strTitle   标题内容
	 * @param strContent 提示信息
	 * @param strOk      按钮内容
	 * @param listener
	 */
	public SDialog(Context context, String strTitle, String strContent, String strOk, SDialogListener listener){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_1_BTN;
		this.strTitle = strTitle;
		this.strContent = strContent;
		this.strOk = strOk;
		this.listener = listener;

	}


	/**
	 * 自定义Dialog(两个按钮)
	 *
	 * @param context
	 * @param titleId    标题id：0为使用默认标题
	 * @param strContent 内容
	 * @param listener2  监听事件
	 */
	public SDialog(Context context, int titleId, String strContent, SDialog2Listener listener2){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.titleId = titleId;
		this.strContent = strContent;
		this.listener2 = listener2;

	}

	/**
	 * 自定义Dialog(两个按钮)
	 *
	 * @param context
	 * @param strTitle   标题
	 * @param strContent 内容
	 * @param btnLeftId  左按钮id
	 * @param btnRightId 右按钮id
	 * @param listener2  监听事件
	 * @param showTitle
	 */
	public SDialog(Context context, String strTitle, String strContent, int btnLeftId, int btnRightId,
                   SDialog2Listener listener2, boolean showTitle){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTitle = strTitle;
		this.strContent = strContent;
		this.listener2 = listener2;
		this.btnLeftId = btnLeftId;
		this.btnRightId = btnRightId;
		this.showTitle = showTitle;
	}

	/**
	 * 自定义Dialog(两个按钮)
	 *
	 * @param context
	 * @param strTitle   标题
	 * @param strContent 内容
	 * @param btnLeftString  左按钮String
	 * @param btnRightString 右按钮String
	 * @param listener2  监听事件
	 * @param showTitle
	 */
	public SDialog(Context context, String strTitle, String strContent, String btnLeftString, String btnRightString,
				   SDialog2Listener listener2, boolean showTitle){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTitle = strTitle;
		this.strContent = strContent;
		this.listener2 = listener2;
		this.btnLeftString = btnLeftString;
		this.btnRightString = btnRightString;
		this.showTitle = showTitle;
	}

	/**
	 * 自定义Dialog(两个按钮)
	 *
	 * @param context
	 * @param strTitle   标题
	 * @param strContent 内容
	 * @param btnLeftId  左按钮id
	 * @param btnRightId 右按钮id
	 * @param listener2  监听事件
	 */
	public SDialog(Context context, String strTitle, String strContent, int btnLeftId, int btnRightId,
                   SDialog2Listener listener2){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTitle = strTitle;
		this.strContent = strContent;
		this.listener2 = listener2;
		this.btnLeftId = btnLeftId;
		this.btnRightId = btnRightId;
	}

	/**
	 * 自定义Dialog(两个按钮)
	 *
	 * @param context
	 * @param strTitle   标题
	 * @param strContent 内容
	 * @param btnLeftString  左按钮String
	 * @param btnRightString 右按钮String
	 * @param listener2  监听事件
	 */
	public SDialog(Context context, String strTitle, String strContent, String btnLeftString, String btnRightString,
                   SDialog2Listener listener2){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTitle = strTitle;
		this.strContent = strContent;
		this.btnLeftString = btnLeftString;
		this.btnRightString = btnRightString;
		this.btnRightId = btnRightId;
		this.listener2 = listener2;
	}

	public SDialog(Context context, String strContent, int imgRes, int btnLeftId, int btnRightId,
                   SDialog2Listener listener2, int lResId, int rResId){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strContent = strContent;
		this.imgRes = imgRes;
		this.btnLeftId = btnLeftId;
		this.btnRightId = btnRightId;
		this.listener2 = listener2;

		this.lResId = lResId;
		this.rResId = rResId;
	}

	public SDialog(Context context, String strContent, int imgRes, String btnLeftString, String btnRightString,
                   SDialog2Listener listener2, int lResId, int rResId){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strContent = strContent;
		this.imgRes = imgRes;
		this.btnLeftString = btnLeftString;
		this.btnRightString = btnRightString;
		this.listener2 = listener2;

		this.lResId = lResId;
		this.rResId = rResId;
	}

	/**
	 * 一个按钮tips
	 * @param context
	 * @param strTips
	 * @param strOk 传null显示 确定 默认值
	 * @param listener
     */
	public SDialog(Context context, String strTips, String strOk, SDialogListener listener){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_1_BTN;
		this.strTips = strTips;
		this.strOk = strOk;
		this.listener = listener;
	}

	/**
	 * 两个按钮tips
	 * @param context
	 * @param strTips
	 * @param btnLeftId 传0显示 取消 默认值
	 * @param btnRightId 传0显示 确定 默认值
     * @param listener2
     */
	public SDialog(Context context, String strTips, int btnLeftId, int btnRightId,
                   SDialog2Listener listener2){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTips = strTips;
		this.btnLeftId = btnLeftId;
		this.btnRightId = btnRightId;
		this.listener2 = listener2;
	}

	/**
	 * 两个按钮tips
	 * @param context
	 * @param strTips
	 * @param btnLeftString 传null显示 取消 默认值
	 * @param btnRightString 传null显示 确定 默认值
     * @param listener2
     */
	public SDialog(Context context, String strTips, String btnLeftString, String btnRightString,
                   SDialog2Listener listener2){
		super(context, R.style.dialog_style_dim_3);
		this.context = context;
		this.type = TYPE_2_BTN;
		this.strTips = strTips;
		this.btnLeftString = btnLeftString;
		this.btnRightString = btnRightString;
		this.listener2 = listener2;
	}


	public TextView getBtnRight(){
		return btnRight;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.t_dialog);
		findViews();
		setListener();
		fillContent();
	}

	private void findViews(){
		init();
		titleLayout = (RelativeLayout) findViewById(R.id.dialog_title_layout);
		title = (TextView) findViewById(R.id.dialog_title);
		contentSV = (ScrollView) findViewById(R.id.dialog_content_sv);
		content = (TextView) findViewById(R.id.dialog_content);
		tips = (TextView) findViewById(R.id.dialog_tips);
		contentImg = (ImageView) findViewById(R.id.dialog_img);
		ok = (TextView) findViewById(R.id.ok);
		doubleBtn = (LinearLayout) findViewById(R.id.doubleBtn);
		btnLeft = (TextView) findViewById(R.id.btnL);
		btnRight = (TextView) findViewById(R.id.btnR);
	}

	private void setListener(){
		ok.setOnClickListener(this);
		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);
	}

	private void fillContent(){
		switch(type){
			case TYPE_1_BTN:
				this.setCanceledOnTouchOutside(false);
				doubleBtn.setVisibility(View.GONE);
				break;
			case TYPE_2_BTN:
				ok.setVisibility(View.GONE);
				break;
			default:
				break;
		}
		if(showTitle){
			if(0 != titleId)
				title.setText(titleId);
			if(null != strTitle){
				title.setText(strTitle);
			}
			else{
				titleLayout.setVisibility(View.GONE);
			}
		}
		else{
			titleLayout.setVisibility(View.GONE);
		}

		if(0 != contentId)
			content.setText(contentId);
		if(null != strContent){
			int lenContent = strContent.length();
			if(lenContent > 50)
				content.setGravity(Gravity.LEFT);
			// 内容大于100的，字体要调小
			if(lenContent > 100){
				float density = context.getResources().getDisplayMetrics().scaledDensity;
				float size = context.getResources().getDimension(R.dimen.t_font_size_l) / density;
				content.setTextSize(size);
			}
			content.setText(strContent);
		}
		if (null != strTips) {
			tips.setText(strTips);
			titleLayout.setVisibility(View.GONE);
			contentSV.setVisibility(View.GONE);
			tips.setVisibility(View.VISIBLE);
		}
		if(0 != imgRes){
			contentImg.setImageResource(imgRes);
		}
		if(0 != btnLeftId)
			btnLeft.setText(btnLeftId);
		if(0 != btnRightId)
			btnRight.setText(btnRightId);
		if (!TextUtils.isEmpty(btnLeftString))
			btnLeft.setText(btnLeftString);
		if (!TextUtils.isEmpty(btnRightString))
			btnRight.setText(btnRightString);

		if(null != strOk){
			ok.setText(strOk);
		}

		if(0 != lResId){
			btnLeft.setTextColor(getContext().getResources().getColor(lResId));
		}
		if(0 != rResId){
			btnRight.setTextColor(getContext().getResources().getColor(rResId));
		}
	}

	public void setClickCancel(boolean b){
		clickcancel = b;
	}

	@Override
	public void onClick(View v){
		if(clickcancel){
			dismiss();
		}
		if(null != listener)
			listener.onOK();
		if(null != listener2){
			if(R.id.btnL == v.getId()){
				listener2.onClick(true, v);
			}
			else{
				listener2.onClick(false, v);
			}
		}
	}

	public void setMessage(String msg){
		if(msg != null){
			content.setText(msg);
		}
	}

	/**
	 * 为内容文本设置背景图
	 */
	public void setContextImg(int resId){
		if(0 != resId){
			content.setBackgroundResource(resId);
		}
	}

	/**
	 * 设置左右按钮的颜色
	 * <p/>
	 * resLId - 左边按钮色值
	 * resRId - 右边按钮色值
	 */
	public void setBtnsClr(int resLId, int resRId){
		btnLeft.setTextColor(resLId);
		btnRight.setTextColor(resRId);
	}

	public interface SDialogListener{
		void onOK();
	}

	public interface SDialog2Listener{
		/**
		 * 响应点击事件
		 *
		 * @param l    true：左按钮 false：右按钮
		 * @param view
		 */
		void onClick(boolean l, View view);
	}

	//add by liujunhui

	/**
	 * 一个按钮，按钮文字可定制
	 * @param activity
	 * @param titleResID
	 * @param contentID
	 * @param okbtnLabel
	 * @param listener
	 */
	private SDialog(Activity activity, int titleResID, int contentID, String okbtnLabel, SDialogListener listener){
		super(activity, R.style.dialog_style_dim_3);
		this.context = activity;
		this.type = TYPE_1_BTN;
		this.titleId = titleResID;
		this.contentId = contentID;
		this.strOk = okbtnLabel;
		this.listener = listener;
	}
	//SDialog建造者
	public static class SDialogBuild{
		public int titleResID = 0;
		public int contentResID = 0;
		public DialogInterface.OnClickListener mPositiveButtonOnClickListener;
		public int okBtnLabelID = 0;
		public DialogInterface.OnClickListener mNegativeButtonOnClickListener;
		public int cancelBtnLabelID = 0;
		private Activity activity;
		private Dialog dialog;
		public SDialogBuild(Activity activity){
			this.activity = activity;
		}



		public SDialogBuild setTitle(int resTitleID){
			this.titleResID = resTitleID;
			return this;
		}

		public SDialogBuild setContent(int resContentID){
			this.contentResID = resContentID;
			return this;
		}

		public SDialogBuild setPositiveButton(int resLabelID, DialogInterface.OnClickListener mPositiveButtonOnClickListener){
			this.okBtnLabelID = resLabelID;
			this.mPositiveButtonOnClickListener = mPositiveButtonOnClickListener;
			return this;
		}

		public SDialogBuild setNegativeButton(int resLabelID, DialogInterface.OnClickListener mNegativeButtonOnClickListener){
			this.cancelBtnLabelID = resLabelID;
			this.mNegativeButtonOnClickListener = mNegativeButtonOnClickListener;
			return this;
		}
		public SDialog create(){
			int type;
			if(okBtnLabelID > 0  && cancelBtnLabelID > 0){
				type = TYPE_2_BTN;
			}
			else if(okBtnLabelID <= 0  && cancelBtnLabelID <= 0){//SDialog 不支持
				throw new IllegalStateException("SDialog not support");
			}
			else if(okBtnLabelID <= 0  && cancelBtnLabelID > 0){
				throw new IllegalStateException("please choose position button, SDialog not support");
			}
			else{
				type = TYPE_1_BTN;
			}
			final SDialog sDialog;
			if(type == TYPE_1_BTN){
				sDialog = new SDialog(activity, titleResID, contentResID, activity.getString(okBtnLabelID), new SDialogListener(){
					@Override
					public void onOK(){
						if(mPositiveButtonOnClickListener != null){
							mPositiveButtonOnClickListener.onClick(dialog, 0);
						}
					}
				});
			}
			else{
				if(titleResID <= 0){
					sDialog = new SDialog( activity, "", activity.getString(contentResID), okBtnLabelID, cancelBtnLabelID,
							new SDialog2Listener(){
								@Override
								public void onClick(boolean l, View view){
									if(l){
										if(mPositiveButtonOnClickListener != null){
											mPositiveButtonOnClickListener.onClick(dialog, 0);
										}
									}
									else{
										if(mNegativeButtonOnClickListener != null){
											mNegativeButtonOnClickListener.onClick(dialog, 0);
										}
									}
								}
							}, false);
				}
				else{
					sDialog = new SDialog( activity, activity.getString(titleResID), activity.getString(contentResID), okBtnLabelID, cancelBtnLabelID,
							new SDialog2Listener(){
								@Override
								public void onClick(boolean l, View view){
									if(l){
										if(mPositiveButtonOnClickListener != null){
											mPositiveButtonOnClickListener.onClick(dialog, 0);
										}
									}
									else{
										if(mNegativeButtonOnClickListener != null){
											mNegativeButtonOnClickListener.onClick(dialog, 0);
										}
									}
								}
							}, !TextUtils.isEmpty(activity.getString(titleResID)));
				}

			}
			dialog = sDialog;
			return sDialog;
		}


	}

}
