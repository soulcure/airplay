package com.coocaa.tvpi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.utils.DimensUtils;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.Utils;
import com.coocaa.tvpilib.R;

/**
 * 视频通话部分的titleBar
 * Created by songxing on 2020/7/7
 */
public class CommonTitleBar extends RelativeLayout {

    public enum ClickPosition {
        LEFT,
        RIGHT
    }

    public enum ImagePosition {
        LEFT,
        RIGHT
    }

    public enum TextPosition {
        TITLE,
        SUBTITLE,
        RIGHT_BUTTON
    }

    private TextView tvTitle;
    private TextView tvSubTitle;
    private ImageView ivLeftImageButton;
    private ImageView ivRightImageButton;
    private TextView tvRightTextButton;

    private String title;
    private String subtitle;
    private String rightButtonText;
    private int leftButtonImageResId;
    private int rightButtonImageResId;
    private int titleColor ;

    private OnClickListener onClickListener;

    public CommonTitleBar(Context context) {
        this(context, null, 0);
    }

    public CommonTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomAttrs(context, attrs, defStyleAttr);
        initView();
        setListener();
    }


    private void initCustomAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CommonTitleBar, defStyleAttr, 0);
        title = ta.getString(R.styleable.CommonTitleBar_common_title_title);
        subtitle = ta.getString(R.styleable.CommonTitleBar_common_title_subtitle);
        subtitle = ta.getString(R.styleable.CommonTitleBar_common_title_subtitle);
        rightButtonText = ta.getString(R.styleable.CommonTitleBar_common_title_right_text);
        leftButtonImageResId = ta.getResourceId(R.styleable.CommonTitleBar_common_title_left_image, R.drawable.videocall_back);
        rightButtonImageResId = ta.getResourceId(R.styleable.CommonTitleBar_common_title_right_image, 0);
        titleColor = ta.getColor(R.styleable.CommonTitleBar_common_title_title_color, getResources().getColor(R.color.black));
        ta.recycle();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_common_titlebar, this, true);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        ivLeftImageButton = findViewById(R.id.ivBack);
        ivRightImageButton = findViewById(R.id.ivRightButton);
        tvRightTextButton = findViewById(R.id.tvRightButton);
        setFitsSystemWindows(true);
        updateVisibility();
    }

    private void setListener() {
        ivLeftImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(ClickPosition.LEFT);
                }
            }
        });

        ivRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(ClickPosition.RIGHT);
                }
            }
        });

        tvRightTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(ClickPosition.RIGHT);
                }
            }
        });
    }

    public void setText(TextPosition position, String text) {
        switch (position) {
            case TITLE:
                title = text;
                break;
            case SUBTITLE:
                subtitle = text;
                break;
            case RIGHT_BUTTON:
                rightButtonText = text;
                break;
            default:
                break;
        }
        updateVisibility();
    }

    public void setImageButtonResId(ImagePosition position, int imageResId) {
        switch (position) {
            case LEFT:
                leftButtonImageResId = imageResId;
                break;
            case RIGHT:
                rightButtonImageResId = imageResId;
                break;
            default:
                break;
        }
        updateVisibility();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void updateVisibility() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(VISIBLE);
            RelativeLayout.LayoutParams titleParams = (LayoutParams) tvTitle.getLayoutParams();
            if (!TextUtils.isEmpty(subtitle)) {
                titleParams.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.CENTER_HORIZONTAL);
                tvTitle.setTextSize(15);
            } else {
                titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                tvTitle.setTextSize(18);
            }
            tvTitle.setLayoutParams(titleParams);
            tvTitle.setText(title);
            tvTitle.setTextColor(titleColor);
        } else {
            tvTitle.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(subtitle)) {
            tvSubTitle.setVisibility(VISIBLE);
            tvSubTitle.setText(subtitle);
        } else {
            tvSubTitle.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(rightButtonText)) {
            ivRightImageButton.setVisibility(GONE);
            tvRightTextButton.setVisibility(VISIBLE);
            tvRightTextButton.setText(rightButtonText);
        } else {
            tvRightTextButton.setVisibility(GONE);
        }

        if (leftButtonImageResId != 0) {
            ivLeftImageButton.setVisibility(VISIBLE);
            ivLeftImageButton.setBackgroundResource(leftButtonImageResId);
        } else {
            ivLeftImageButton.setVisibility(GONE);
        }

        //注意右边按钮支持文字和图片，同时设置以图片为主
        if (rightButtonImageResId != 0) {
            tvRightTextButton.setVisibility(GONE);
            ivRightImageButton.setVisibility(VISIBLE);
            ivRightImageButton.setBackgroundResource(rightButtonImageResId);
        } else {
            ivRightImageButton.setVisibility(GONE);
        }
    }

    public void setParentScrollY(int scrollY) {
        float fraction = scrollY >= DimensUtils.dp2Px(getContext(), 200) ? 1f
                : scrollY / (float) DimensUtils.dp2Px(getContext(), 200);
        setBackgroundColor(Utils.changeAlpha(getResources().getColor(R.color.b_1), fraction));
        if (UserInfoCenter.getInstance().isLogin()) {
            this.setVisibility(1 - fraction <= 0.01 ? VISIBLE : GONE);
        }
    }

    public interface OnClickListener {
        void onClick(ClickPosition position);
    }
}
