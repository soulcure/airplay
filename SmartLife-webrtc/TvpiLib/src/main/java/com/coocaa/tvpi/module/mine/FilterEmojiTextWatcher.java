package com.coocaa.tvpi.module.mine;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用表情过滤器
 */
public class FilterEmojiTextWatcher implements TextWatcher {

    private Context mContext;

    public FilterEmojiTextWatcher(Context mContext){
        this.mContext = mContext;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if(count - before >= 1){
            CharSequence input = charSequence.subSequence(start + before, start + count);
            if(isEmoji(input.toString())){
                ((SpannableStringBuilder)charSequence).delete(start + before, start + count);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {}

    /**
     * 正则判断emoji表情
     * @param input
     * @return
     */
    private boolean isEmoji(String input){
        Pattern p = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\ud83e\udc00-\ud83e\udfff]" +
                "|[\u2100-\u32ff]|[\u0030-\u007f][\u20d0-\u20ff]|[\u0080-\u00ff]");
        Matcher m = p.matcher(input);
        return m.find();
    }
}
