package swaiotos.channel.iot.tv.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import swaiotos.channel.iot.tv.R;

/**
 * @ProjectName: panel_update
 * @Package: swaiotos.panel_update.view
 * @ClassName: ShangshabanChangeTextSpaceView
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/5/2 12:07
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/5/2 12:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

/**
 * 作者：
 * 自定义可以调节字间距的TextView,使用的时候，要在JAVA代码中调用settext方法设置文字，调用setSpacing设置
 *  字间距（float类型）
 */
public class ChangeTextSpaceView extends TextView {
    private float spacing;
    private CharSequence originalText = "";


    public ChangeTextSpaceView(Context context) {
        super(context);
    }

    public ChangeTextSpaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeTextSpaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        spacing = context.getResources().getDimension(R.dimen.px_6);
    }

    public float getSpacing() {
        return this.spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        applySpacing();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        applySpacing();
    }

    @Override
    public CharSequence getText() {
        return originalText;
    }

    private void applySpacing() {
        if (this == null || this.originalText == null) return;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < originalText.length(); i++) {
            builder.append(originalText.charAt(i));
            if (i + 1 < originalText.length()) {
                builder.append("\u00A0");
            }
        }
        SpannableString finalText = new SpannableString(builder.toString());
        if (builder.toString().length() > 1) {
            for (int i = 1; i < builder.toString().length(); i += 2) {
                finalText.setSpan(new ScaleXSpan((spacing + 1) / 10), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
    }
}
